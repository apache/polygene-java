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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.EntitySession;
import org.qi4j.entity.EntitySessionException;
import org.qi4j.entity.Identity;
import org.qi4j.entity.IdentityGenerator;
import org.qi4j.entity.SessionCompletionException;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilderFactory;
import org.qi4j.query.QueryBuilderFactoryImpl;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.EntityCompositeInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ServiceMap;
import org.qi4j.service.ServiceInstanceProviderException;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;

public final class EntitySessionInstance
    implements EntitySession
{
    private HashMap<Class<? extends EntityComposite>, Map<String, EntityEntry>> cache;

    private boolean open;
    private ModuleInstance moduleInstance;
    private ServiceMap<EntityStore> entityStores;
    private ServiceMap<IdentityGenerator> identityGenerators;

    public EntitySessionInstance( ModuleInstance moduleInstance )
    {
        this.moduleInstance = moduleInstance;
        this.open = true;
        cache = new HashMap<Class<? extends EntityComposite>, Map<String, EntityEntry>>();

        entityStores = new ServiceMap<EntityStore>( moduleInstance, EntityStore.class );
        identityGenerators = new ServiceMap<IdentityGenerator>( moduleInstance, IdentityGenerator.class );
    }

    public EntitySession newEntitySession()
    {
        //TODO: Auto-generated, need attention.
        return null;
    }

    public <T extends EntityComposite> CompositeBuilder<T> newEntityBuilder( String identity, Class<T> compositeType )
    {
        checkOpen();

            EntityStore store = entityStores.getService( compositeType );

            CompositeBuilder<T> builder = new EntityCompositeBuilderFactory( moduleInstance, this, store ).newCompositeBuilder( compositeType );

            if( identity == null )
            {
                identity = identityGenerators.getService( compositeType ).generate( compositeType );
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

                EntityStore store = entityStores.getService( compositeType );
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
                    EntityStore store = entityStores.getService( compositeType );
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
                EntityStore store = entityStores.getService( entry.getKey() );
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
        entityStores.release();
        identityGenerators.release();

        open = false;
    }

    public void discard()
    {
        checkOpen();

        cache.clear();
        entityStores.release();
        identityGenerators.release();

        open = false;
    }

    public boolean isOpen()
    {
        return open;
    }

    public URL toURL( Identity identity )
    {
        checkOpen();

        return null;  //To change body of implemented methods use File | Settings | File Templates.
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

    enum EntityStatus
    {
        CREATED, CACHED, REMOVED
    }
}
