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
import java.util.ArrayList;
import java.util.Collection;
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
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.property.PropertyInfo;
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
                EntityStore store = stateServices.getEntityStore( compositeType );
                CompositeContext compositeContext = moduleInstance.getModuleContext().getCompositeContext( compositeType );
                CompositeBinding compositeBinding = compositeContext.getCompositeBinding();
                EntityState state = store.getEntityInstance( this, identity, compositeBinding );
                entity = new EntityEntry( EntityStatus.CACHED, (EntityComposite) compositeContext.newEntityCompositeInstance( moduleInstance, this, store, identity ).getProxy() );
                EntityCompositeInstance compositeInstance = EntityCompositeInstance.getEntityCompositeInstance( entity.getInstance() );
                compositeContext.newEntityMixins( moduleInstance, compositeInstance, state );
                Map<String, EntityEntry> entityCache = getEntityCache( compositeType );
                entityCache.put( identity, entity );
            }
            else if( entity.getStatus() != EntityStatus.REMOVED )
            {
                if( entity.getInstance().isReference() )
                {
                    EntityStore store = stateServices.getEntityStore( compositeType );
                    CompositeBinding compositeBinding = moduleInstance.getModuleContext().getCompositeContext( compositeType ).getCompositeBinding();
                    EntityState state = store.getEntityInstance( this, identity, compositeBinding );
                    EntityCompositeInstance handler = EntityCompositeInstance.getEntityCompositeInstance( entity.getInstance() );
                    handler.setState( state );
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
            EntityStore store = stateServices.getEntityStore( compositeType );
            CompositeContext compositeContext = moduleInstance.getModuleContext().getCompositeContext( compositeType );
            entity = new EntityEntry( EntityStatus.CACHED, (EntityComposite) compositeContext.newEntityCompositeInstance( moduleInstance, this, store, identity ).getProxy() );
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
        if( !handler.isReference() )
        {
            handler.getState().refresh();
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

        // Create complete lists
        Map<EntityStore<EntityState>, List<EntityState>> storeCompletions = new HashMap<EntityStore<EntityState>, List<EntityState>>();
        for( Map.Entry<Class<? extends EntityComposite>, Map<String, EntityEntry>> entry : cache.entrySet() )
        {
            EntityStore<EntityState> store = stateServices.getEntityStore( entry.getKey() );
            List<EntityState> storeCompletionList = storeCompletions.get( store );
            if( storeCompletionList == null )
            {
                storeCompletionList = new ArrayList<EntityState>();
                storeCompletions.put( store, storeCompletionList );
            }

            Map<String, EntityEntry> entities = entry.getValue();
            for( EntityEntry entityEntry : entities.values() )
            {
                EntityComposite composite = entityEntry.getInstance();
                EntityState state = EntityCompositeInstance.getEntityCompositeInstance( composite ).getState();
                storeCompletionList.add( state );
            }
        }

        // Commit complete lists
        for( Map.Entry<EntityStore<EntityState>, List<EntityState>> entityStoreListEntry : storeCompletions.entrySet() )
        {
            EntityStore<EntityState> entityStore = entityStoreListEntry.getKey();
            List<EntityState> stateList = entityStoreListEntry.getValue();

            try
            {
                entityStore.complete( this, stateList );
            }
            catch( StoreException e )
            {
                e.printStackTrace();
            }
        }

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
        private EntitySessionStore store = new EntitySessionStore();

        public EntityStore getEntityStore( Class<? extends EntityComposite> compositeType )
        {
            return store;
        }

        public IdentityGenerator getIdentityGenerator( Class<? extends EntityComposite> compositeType )
        {
            return stateServices.getIdentityGenerator( compositeType );
        }
    }

    private class EntitySessionStore
        implements EntityStore<EntitySessionState>
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

        public EntitySessionState newEntityInstance( EntitySession session, String identity, CompositeBinding compositeBinding, Map propertyValues ) throws StoreException
        {

            return null;
        }

        public EntitySessionState getEntityInstance( EntitySession session, String identity, CompositeBinding compositeBinding ) throws StoreException
        {
            Class<? extends EntityComposite> entityType = (Class<? extends EntityComposite>) compositeBinding.getCompositeResolution().getCompositeModel().getCompositeClass();
            EntityComposite parentEntity = getReference( identity, entityType );
            EntitySessionState entitySessionState = new EntitySessionState( parentEntity );
            return entitySessionState;
        }

        public void complete( EntitySession session, Iterable<EntitySessionState> states ) throws StoreException
        {
            for( EntitySessionState state : states )
            {
                EntityComposite instance = state.getParentEntity();
                Collection<Property> properties = state.getProperties().values();
                for( Property property : properties )
                {
                    // If property in nested session has been updated then copy value to original
                    if( property instanceof EntitySessionPropertyInstance )
                    {
                        EntitySessionPropertyInstance propertyInstance = (EntitySessionPropertyInstance) property;
                        if( propertyInstance.isUpdated() )
                        {
                            Property original = (Property) propertyInstance.getPropertyInfo();
                            original.set( propertyInstance.get() );
                        }
                    }
                }
            }
        }
    }

    private class EntitySessionState
        implements EntityState
    {
        EntityComposite parentEntity;
        Map<Method, Property> properties = new HashMap<Method, Property>();
        Map<Method, AbstractAssociation> associations = new HashMap<Method, AbstractAssociation>();

        private EntitySessionState( EntityComposite parentEntity )
        {
            this.parentEntity = parentEntity;
        }

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
            try
            {
                Property property = properties.get( propertyMethod );
                if( property == null )
                {
                    EntityCompositeInstance compositeInstance = EntityCompositeInstance.getEntityCompositeInstance( parentEntity );
                    EntityState state = compositeInstance.loadState();
                    Property original = state.getProperty( propertyMethod );
                    if( original instanceof ImmutableProperty )
                    {
                        property = original;
                    }
                    else
                    {
                        property = new EntitySessionPropertyInstance( original, original.get() );
                    }
                    properties.put( propertyMethod, property );
                }
                return property;
            }
            catch( StoreException e )
            {
                // Could not load state for this entity
                throw new EntitySessionException( e );
            }
        }

        public AbstractAssociation getAssociation( Method associationMethod )
        {
            return null;
        }

        private EntityComposite getParentEntity()
        {
            return parentEntity;
        }

        private Map<Method, Property> getProperties()
        {
            return properties;
        }

        private Map<Method, AbstractAssociation> getAssociations()
        {
            return associations;
        }
    }

    private class EntitySessionPropertyInstance
        extends PropertyInstance
    {
        boolean updated = false;

        private EntitySessionPropertyInstance( PropertyInfo aPropertyInfo, Object aValue )
            throws IllegalArgumentException
        {
            super( aPropertyInfo, aValue );
        }

        @Override public Object set( Object aNewValue )
        {
            updated = true;
            return super.set( aNewValue );
        }

        public boolean isUpdated()
        {
            return updated;
        }
    }

    enum EntityStatus
    {
        CREATED, CACHED, REMOVED
    }
}
