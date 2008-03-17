/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime.entity;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.EntitySession;
import org.qi4j.entity.EntitySessionException;
import org.qi4j.entity.Identity;
import org.qi4j.entity.IdentityGenerator;
import org.qi4j.entity.SessionCompletionException;
import org.qi4j.property.Property;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilderFactory;
import org.qi4j.query.QueryBuilderFactoryImpl;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.EntityCompositeInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StoreException;
import org.qi4j.spi.property.PropertyInstance;

public final class EntitySessionInstance
    implements EntitySession
{
    private HashMap<Class<? extends EntityComposite>, Map<String, EntityEntry>> cache;

    private boolean open;
    private ModuleInstance moduleInstance;
    private StateServices stateServices;

    public EntitySessionInstance( ModuleInstance moduleInstance, StateServices stateServices )
    {
        this.moduleInstance = moduleInstance;
        this.stateServices = stateServices;
        this.open = true;
        cache = new HashMap<Class<? extends EntityComposite>, Map<String, EntityEntry>>();
    }

    public <T extends EntityComposite> CompositeBuilder<T> newEntityBuilder( Class<T> compositeType )
    {
        return newEntityBuilder( null, compositeType );
    }

    public <T extends EntityComposite> CompositeBuilder<T> newEntityBuilder( String identity, Class<T> compositeType )
    {
        checkOpen();

        EntityStore store = stateServices.getEntityStore( compositeType );

//            if (store == null)
//                throw new EntitySessionException("No store for composite type "+compositeType.getName());

        CompositeBuilder<T> builder = new EntityCompositeBuilderFactory( moduleInstance, this, store ).newCompositeBuilder( compositeType );

        if( identity == null )
        {
            identity = stateServices.getIdentityGenerator( compositeType ).generate( compositeType );
        }

        builder.propertiesFor( Identity.class ).identity().set( identity );

        return builder;
    }

    public void remove( EntityComposite entity )
    {
        checkOpen();

        EntityEntry entry = new EntityEntry( EntityStatus.REMOVED, entity );
        Class compositeType = entity.getCompositeType();
        Map<String, EntityEntry> entityCache = getEntityCache( compositeType );
        entityCache.put( entity.identity().get(), entry );
        EntityCompositeInstance.getEntityCompositeInstance( entity ).setMixins( null );
    }

    public <T extends EntityComposite> T find( String identity, Class<T> compositeType )
        throws EntityCompositeNotFoundException
    {
        checkOpen();

        // TODO: Argument check.

        try
        {
            EntityEntry entity = getCachedEntity( identity, compositeType );
            if( entity == null )
            {
                CompositeBuilder<T> builder = moduleInstance.getStructureContext().getCompositeBuilderFactory().newCompositeBuilder( compositeType );
                builder.propertiesFor( Identity.class ).identity().set( identity );
                entity = new EntityEntry( EntityStatus.CACHED, builder.newInstance() );

                EntityStore store = stateServices.getEntityStore( compositeType );
                CompositeContext compositeContext = moduleInstance.getModuleContext().getCompositeContext( compositeType );
                CompositeBinding compositeBinding = compositeContext.getCompositeBinding();
                EntityState state = store.getEntityInstance( this, identity, compositeBinding );
                EntityCompositeInstance compositeInstance = EntityCompositeInstance.getEntityCompositeInstance( entity.getInstance() );
                compositeContext.newEntityMixins( moduleInstance, compositeInstance, state );
                compositeInstance.setState( state );
                Map<String, EntityEntry> entityCache = getEntityCache( compositeType );
                entityCache.put( identity, entity );
            }
            else if( entity.getStatus() != EntityStatus.REMOVED )
            {
                if( entity.getInstance().isReference() )
                {
                    EntityStore store = stateServices.getEntityStore( compositeType );
                    CompositeBinding compositeBinding = moduleInstance.getModuleContext().getCompositeContext( compositeType ).getCompositeBinding();
                    EntityState holder = store.getEntityInstance( this, identity, compositeBinding );
                    EntityCompositeInstance handler = EntityCompositeInstance.getEntityCompositeInstance( entity.getInstance() );
                    handler.setState( holder );
                }
            }
            else
            {
                throw new EntityCompositeNotFoundException( "Entity has been removed", identity, compositeType );
            }
            return compositeType.cast( entity.getInstance() );
        }
        catch( Exception e )
        {
            throw new EntityStorageException( "Storage unable to access entity " + identity, e );
        }
    }

    public <T extends EntityComposite> T getReference( String identity, Class<T> compositeType )
        throws EntityCompositeNotFoundException
    {
        checkOpen();

        EntityEntry entity = getCachedEntity( identity, compositeType );
        if( entity == null )
        {
            CompositeBuilder<T> builder = moduleInstance.getStructureContext().getCompositeBuilderFactory().newCompositeBuilder( compositeType );
            builder.propertiesFor( Identity.class ).identity().set( identity );
            entity = new EntityEntry( EntityStatus.CACHED, builder.newInstance() );
            Map<String, EntityEntry> entityCache = getEntityCache( compositeType );
            entityCache.put( identity, entity );
        }
        else if( entity.getStatus() == EntityStatus.REMOVED )
        {
            throw new EntityCompositeNotFoundException( "Entity has been removed", identity, compositeType );
        }
        return compositeType.cast( entity.getInstance() );
    }

    public <T> T getReference( T entity ) throws EntityCompositeNotFoundException
    {
        EntityComposite entityComposite = (EntityComposite) entity;
        return (T) getReference( entityComposite.identity().get(), (Class<? extends EntityComposite>) entityComposite.getCompositeType() );
    }

    public void refresh( EntityComposite entity )
    {
        checkOpen();

        EntityCompositeInstance handler = EntityCompositeInstance.getEntityCompositeInstance( entity );
        EntityState state = handler.getState();
        if( state != null )
        {
            state.refresh();
        }
    }

    public void refresh()
    {
        // Refresh the entire session
        for( Map<String, EntityEntry> map : cache.values() )
        {
            for( EntityEntry entityEntry : map.values() )
            {
                if( entityEntry.getStatus() == EntityStatus.CACHED )
                {
                    refresh( entityEntry.getInstance() );
                }
            }
        }
    }

    public void clear()
    {
        checkOpen();

        cache.clear();
    }

    public boolean contains( EntityComposite entity )
    {
        checkOpen();

        return getCachedEntity( entity.identity().get(), entity.getCompositeType() ) != null;
    }

    public QueryBuilderFactory getQueryBuilderFactory()
    {
        checkOpen();

        return new QueryBuilderFactoryImpl( new QueryableEntitySession( this ) );
    }

    public Query getNamedQuery( String name )
    {
        checkOpen();

        return null;
    }

    public Query newQuery( String expression, Class compositeType )
    {
        checkOpen();

        return null;
    }

    public void complete()
        throws SessionCompletionException
    {
        checkOpen();

        // Create new entities
        for( Map.Entry<Class<? extends EntityComposite>, Map<String, EntityEntry>> entry : cache.entrySet() )
        {
            EntityStore store = stateServices.getEntityStore( entry.getKey() );
            Map<String, EntityEntry> entities = entry.getValue();
            for( EntityEntry entityEntry : entities.values() )
            {
                if( entityEntry.getStatus() == EntityStatus.CREATED )
                {
                    // TODO
                }
                else if( entityEntry.getStatus() == EntityStatus.REMOVED )
                {
                    // TODO
//                        store.delete( entityEntry.getInstance().identity().get(), entityEntry.getInstance().getCompositeType() );
                }
            }
        }
//        catch( StoreException e )
//        {
//            throw new SessionCompletionException( "Could not complete session", e );
//        }


        cache.clear();

        open = false;
    }

    public void discard()
    {
        checkOpen();

        cache.clear();

        open = false;
    }

    public boolean isOpen()
    {
        return open;
    }

    public EntitySession newEntitySession()
    {
        return new EntitySessionInstance( moduleInstance, new EntitySessionStateServices() );
        // TODO This needs to be fixed
        // This session should be used as store
    }

    void createEntity( EntityComposite instance )
    {
        Class<? extends EntityComposite> compositeType = (Class<? extends EntityComposite>) instance.getCompositeType();
        Map<String, EntityEntry> entityCache = cache.get( compositeType );

        if( entityCache == null )
        {
            entityCache = new HashMap<String, EntityEntry>();
            cache.put( compositeType, entityCache );
        }

        entityCache.put( instance.identity().get(), new EntityEntry( EntityStatus.CREATED, instance ) );
    }

    Map<String, EntityEntry> getEntityCache( Class<? extends EntityComposite> compositeType )
    {
        Map<String, EntityEntry> entityCache = cache.get( compositeType );

        if( entityCache == null )
        {
            entityCache = new HashMap<String, EntityEntry>();
            cache.put( compositeType, entityCache );
        }

        return entityCache;
    }

    private EntityEntry getCachedEntity( String identity, Class compositeType )
    {
        Map<String, EntityEntry> entityCache = cache.get( compositeType );

        if( entityCache == null )
        {
            return null;
        }

        return entityCache.get( identity );
    }

    private void checkOpen()
    {
        if( !isOpen() )
        {
            throw new EntitySessionException( "Session has been closed" );
        }
    }

    public static class EntityEntry
    {
        private EntityStatus status;
        private EntityComposite instance;

        public EntityEntry( EntityStatus status, EntityComposite instance )
        {
            this.status = status;
            this.instance = instance;
        }

        public EntityStatus getStatus()
        {
            return status;
        }

        public EntityComposite getInstance()
        {
            return instance;
        }
    }

    private class EntitySessionStateServices
        implements StateServices
    {

        public EntityStore getEntityStore( Class<? extends EntityComposite> compositeType )
        {
            return new EntitySessionStore();
        }

        public IdentityGenerator getIdentityGenerator( Class<? extends EntityComposite> compositeType )
        {
            return stateServices.getIdentityGenerator( compositeType );
        }
    }

    private class EntitySessionStore
        implements EntityStore
    {

        public boolean exists( String identity, CompositeBinding compositeBinding ) throws StoreException
        {
            Class<? extends EntityComposite> entityType = (Class<? extends EntityComposite>) compositeBinding.getCompositeResolution().getCompositeModel().getCompositeClass();
            if( !getReference( identity, entityType ).isReference() )
            {
                return true;
            }

            try
            {
                find( identity, entityType );
                return true;
            }
            catch( EntityCompositeNotFoundException e )
            {
                return false;
            }
        }

        public EntityState newEntityInstance( EntitySession session, String identity, CompositeBinding compositeBinding, Map propertyValues ) throws StoreException
        {

            return null;
        }

        public EntityState getEntityInstance( EntitySession session, String identity, CompositeBinding compositeBinding ) throws StoreException
        {
            Class<? extends EntityComposite> entityType = (Class<? extends EntityComposite>) compositeBinding.getCompositeResolution().getCompositeModel().getCompositeClass();
            getReference( identity, entityType );

            return null;
        }

        public void complete( EntitySession session, List states ) throws StoreException
        {
        }
    }

    private class EntitySessionState
        implements EntityState
    {
        EntityComposite parentEntity;
        Map<Method, Property> properties = new HashMap<Method, Property>();
        Map<Method, AbstractAssociation> associations = new HashMap<Method, AbstractAssociation>();

        public String getIdentity()
        {
            return parentEntity.identity().get();
        }

        public CompositeBinding getCompositeBinding()
        {
            return EntityCompositeInstance.getEntityCompositeInstance( parentEntity ).getState().getCompositeBinding();
        }

        public void refresh()
        {
            properties.clear();
            associations.clear();
        }

        public boolean delete() throws StoreException
        {
            remove( parentEntity );
            return true;
        }

        public Property getProperty( Method propertyMethod )
        {
            Property property = properties.get( propertyMethod );
            if( property == null )
            {
                property = new PropertyInstance( property, property.get() );
                properties.put( propertyMethod, property );
            }
            return property;
        }

        public AbstractAssociation getAssociation( Method associationMethod )
        {
            return null;
        }
    }

    enum EntityStatus
    {
        CREATED, CACHED, REMOVED
    }
}
