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
import org.qi4j.composite.CompositeBuilderFactory;
import static org.qi4j.composite.PropertyValue.property;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.EntitySession;
import org.qi4j.entity.EntitySessionException;
import org.qi4j.entity.Identity;
import org.qi4j.entity.IdentityGenerator;
import org.qi4j.entity.property.PropertyContainer;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilderFactory;
import org.qi4j.query.QueryBuilderFactoryImpl;
import org.qi4j.runtime.composite.EntityCompositeInstance;
import org.qi4j.runtime.entity.property.EntitySessionPropertyContainer;
import org.qi4j.spi.entity.EntityStateHolder;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StoreException;

public class EntitySessionImpl
    implements EntitySession
{
    private HashMap<Class<? extends EntityComposite>, Map<String, EntityEntry>> cache;

    private boolean open;
    private EntityStore store;
    private CompositeBuilderFactory builderFactory;
    private IdentityGenerator identityGenerator;
    private PropertyContainer<Object> propertyContainer;

    public EntitySessionImpl( CompositeBuilderFactory builderFactory, IdentityGenerator identityGenerator, EntityStore store )
    {
        this.identityGenerator = identityGenerator;
        this.builderFactory = builderFactory;
        this.open = true;
        this.store = store;
        cache = new HashMap<Class<? extends EntityComposite>, Map<String, EntityEntry>>();
        propertyContainer = new EntitySessionPropertyContainer<Object>( null, this );
    }

    public <T extends EntityComposite> CompositeBuilder<T> newEntityBuilder( String identity, Class<T> compositeType )
    {
        checkOpen();

        CompositeBuilder<T> builder = builderFactory.newCompositeBuilder( compositeType );

        if( identity == null )
        {
            identity = identityGenerator.generate( compositeType );
        }

        builder.properties( Identity.class, property( "identity", identity ) );

        // Wrap it
        builder = new EntitySessionCompositeBuilder( builder, this );

        return builder;
    }

    public <T extends EntityComposite> T attach( T entity )
    {
        checkOpen();

        return null;
    }

    public void remove( EntityComposite entity )
    {
        checkOpen();

        EntityEntry entry = new EntityEntry( EntityStatus.REMOVED, entity );
        Class compositeType = entry.getClass().getInterfaces()[ 0 ];
        Map<String, EntityEntry> entityCache = getEntityCache( compositeType );
        entityCache.put( entity.getIdentity(), entry );
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
                CompositeBuilder<T> builder = builderFactory.newCompositeBuilder( compositeType );
                builder.properties( Identity.class, property( "identity", identity ) );
                entity = new EntityEntry( EntityStatus.CACHED, builder.newInstance() );
                EntityStateHolder holder = store.getEntityInstance( identity, compositeType );
                EntityCompositeInstance handler = EntityCompositeInstance.getEntityCompositeInstance( entity.getInstance() );
                handler.setEntityStateHolder( holder );
                Map<String, EntityEntry> entityCache = getEntityCache( compositeType );
                entityCache.put( identity, entity );
            }
            else if( entity.getStatus() != EntityStatus.REMOVED )
            {
                if( entity.getInstance().isReference() )
                {
                    EntityStateHolder holder = store.getEntityInstance( identity, compositeType );
                    EntityCompositeInstance handler = EntityCompositeInstance.getEntityCompositeInstance( entity.getInstance() );
                    handler.setEntityStateHolder( holder );
                }
            }
            else
            {
                throw new EntityCompositeNotFoundException( "Entity has been removed", identity, compositeType );
            }
            return compositeType.cast( entity.getInstance() );
        }
        catch( StoreException e )
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
            CompositeBuilder<T> builder = builderFactory.newCompositeBuilder( compositeType );
            builder.properties( Identity.class, property( "identity", identity ) );
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
        EntityStateHolder stateHolder = handler.getHolder();
        if( stateHolder != null )
        {
            stateHolder.refresh();
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

        return getCachedEntity( entity.getIdentity(), getCompositeType( entity ) ) != null;
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
    {
        checkOpen();

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

    public URL toURL( Identity identity )
    {
        checkOpen();

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PropertyContainer getPropertyContainer()
    {
        return propertyContainer;
    }

    void createEntity( EntityComposite instance )
    {
        Class compositeType = getCompositeType( instance );
        Map<String, EntityEntry> entityCache = cache.get( compositeType );

        if( entityCache == null )
        {
            entityCache = new HashMap<String, EntityEntry>();
            cache.put( compositeType, entityCache );
        }

        entityCache.put( instance.getIdentity(), new EntityEntry( EntityStatus.CREATED, instance ) );
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

    private Class getCompositeType( EntityComposite entity )
    {
        return entity.getClass().getInterfaces()[ 0 ];
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
        EntityStatus status;
        EntityComposite instance;

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
