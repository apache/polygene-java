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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.InvalidApplicationException;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.Identity;
import org.qi4j.entity.IdentityGenerator;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.UnitOfWorkException;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilderFactory;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.EntityCompositeInstance;
import org.qi4j.runtime.query.QueryBuilderFactoryImpl;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStateInstance;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.serialization.EntityId;
import org.qi4j.spi.structure.CompositeDescriptor;
import org.qi4j.spi.structure.ModuleBinding;

public final class UnitOfWorkInstance
    implements UnitOfWork
{
    static ThreadLocal<Stack<UnitOfWork>> current;

    private HashMap<Class<? extends EntityComposite>, Map<String, EntityComposite>> cache;

    private boolean open;

    private boolean paused;

    private ModuleInstance moduleInstance;
    StateServices stateServices;

    /**
     * Lazy query builder factory.
     */
    private QueryBuilderFactory queryBuilderFactory;

    static
    {
        current = new ThreadLocal<Stack<UnitOfWork>>()
        {
            protected Stack<UnitOfWork> initialValue()
            {
                return new Stack<UnitOfWork>();
            }
        };
    }

    public UnitOfWorkInstance( ModuleInstance moduleInstance, StateServices stateServices )
    {
        this.moduleInstance = moduleInstance;
        this.stateServices = stateServices;
        this.open = true;
        cache = new HashMap<Class<? extends EntityComposite>, Map<String, EntityComposite>>();
        current.get().push( this );
        paused = false;
    }

    public <T> T newEntity( Class<T> compositeType )
    {
        return newEntityBuilder( compositeType ).newInstance();
    }

    public <T> T newEntity( String identity, Class<T> compositeType )
    {
        return newEntityBuilder( identity, compositeType ).newInstance();
    }

    public <T> CompositeBuilder<T> newEntityBuilder( Class<T> mixinType )
    {
        return newEntityBuilder( null, mixinType );
    }

    public <T> CompositeBuilder<T> newEntityBuilder( String identity, Class<T> mixinType )
    {
        checkOpen();

        // Translate mixin type to actual Composite type
        Class<? extends T> compositeType = (Class<? extends T>) moduleInstance.lookupCompositeType( mixinType );

        if( !EntityComposite.class.isAssignableFrom( compositeType ) )
        {
            throw new UnitOfWorkException( "Trying to create builder for non-Entity type " + compositeType.getName() );
        }

        EntityStore store = stateServices.getEntityStore( (Class<? extends EntityComposite>) compositeType );

//            if (store == null)
//                throw new UnitOfWorkException("No store for composite type "+compositeType.getName());

        CompositeBuilder<T> builder = (CompositeBuilder<T>) new EntityCompositeBuilderFactory( moduleInstance, this, store ).newCompositeBuilder( compositeType );
        if( identity != null )
        {
            builder.stateFor( Identity.class ).identity().set( identity );
        }
        return builder;
    }

    public void remove( Object entity )
    {
        checkOpen();

        EntityComposite entityComposite = (EntityComposite) entity;

        EntityCompositeInstance compositeInstance = EntityCompositeInstance.getEntityCompositeInstance( entityComposite );
        compositeInstance.loadState().remove();
    }

    public <T> T find( String identity, Class<T> mixinType )
        throws EntityCompositeNotFoundException
    {
        checkOpen();

        Class<? extends EntityComposite> compositeType = (Class<? extends EntityComposite>) moduleInstance.lookupCompositeType( mixinType );

        // TODO: Argument check.

        try
        {
            EntityComposite entity = getCachedEntity( identity, mixinType );
            if( entity == null )
            {   // Not yet in cache

                // Get the state from the store
                EntityStore store = stateServices.getEntityStore( compositeType );
                if( store == null )
                {
                    throw new UnitOfWorkException( "No store found for type " + compositeType );
                }

                CompositeContext compositeContext = moduleInstance.getModuleContext().getCompositeContext( compositeType );

                if( compositeContext == null )
                {
                    throw new InvalidApplicationException(
                        "Trying to create unregistered composite of type [" + compositeType.getName() + "] in module [" +
                        moduleInstance.getModuleContext().getModuleBinding().getModuleResolution().getModuleModel().getName() + "]."
                    );
                }

                EntityState state = null;
                try
                {
                    state = store.getEntityState( compositeContext.getCompositeResolution().getCompositeDescriptor(), new EntityId( identity, compositeType.getName() ) );
                }
                catch( EntityNotFoundException e )
                {
                    throw new EntityCompositeNotFoundException( identity, compositeType );
                }

                // Create entity instance
                entity = (EntityComposite) compositeContext.newEntityCompositeInstance( this, store, identity ).getProxy();
                EntityCompositeInstance compositeInstance = EntityCompositeInstance.getEntityCompositeInstance( entity );
                compositeContext.newEntityMixins( this, compositeInstance, state );
                Map<String, EntityComposite> entityCache = getEntityCache( compositeType );
                entityCache.put( identity, entity );
            }
            else
            {
                EntityCompositeInstance entityCompositeInstance = EntityCompositeInstance.getEntityCompositeInstance( entity );
                if( entity.isReference() )
                {
                    // Check that state exists
                    EntityStore store = stateServices.getEntityStore( compositeType );
                    EntityState state = store.getEntityState( entityCompositeInstance.getContext().getCompositeResolution().getCompositeDescriptor(), new EntityId( identity, compositeType.getName() ) );
                    entityCompositeInstance.setState( state );
                }
                else
                {
                    // Check if it has been removed
                    EntityState entityState = entityCompositeInstance.getState();
                    if( entityState.getStatus() == EntityStatus.REMOVED )
                    {
                        throw new EntityCompositeNotFoundException( identity, compositeType );
                    }
                }
            }

            return mixinType.cast( entity );
        }
        catch( EntityStoreException e )
        {
            throw new EntityStorageException( "Storage unable to access entity " + identity, e );
        }
    }

    public <T> T getReference( String identity, Class<T> mixinType )
        throws EntityCompositeNotFoundException
    {
        checkOpen();

        Class<? extends EntityComposite> compositeType = (Class<? extends EntityComposite>) moduleInstance.lookupCompositeType( mixinType );

        EntityComposite entity = getCachedEntity( identity, compositeType );
        if( entity == null )
        {
            // Create entity instance
            EntityStore store = stateServices.getEntityStore( compositeType );
            CompositeContext compositeContext = moduleInstance.getModuleContext().getCompositeContext( compositeType );
            entity = (EntityComposite) compositeContext.newEntityCompositeInstance( this, store, identity ).getProxy();
            Map<String, EntityComposite> entityCache = getEntityCache( compositeType );
            entityCache.put( identity, entity );
        }
        else
        {
            // Check if it has been removed
            EntityCompositeInstance handler = EntityCompositeInstance.getEntityCompositeInstance( entity );
            EntityState entityState = handler.getState();
            if( entityState != null && entityState.getStatus() == EntityStatus.REMOVED )
            {
                throw new EntityCompositeNotFoundException( identity, compositeType );
            }
        }

        return mixinType.cast( entity );
    }

    public <T> T dereference( T entity )
        throws EntityCompositeNotFoundException
    {
        EntityComposite entityComposite = (EntityComposite) entity;
        EntityCompositeInstance compositeInstance = EntityCompositeInstance.getEntityCompositeInstance( entityComposite );
        String id = compositeInstance.getIdentity();
        Class<? extends EntityComposite> type = (Class<? extends EntityComposite>) compositeInstance.getContext().getCompositeModel().getCompositeType();
        return (T) getReference( id, type );
    }

    public void refresh( Object entity )
        throws UnitOfWorkException
    {
        checkOpen();

        EntityComposite entityComposite = (EntityComposite) entity;
        EntityCompositeInstance entityInstance = EntityCompositeInstance.getEntityCompositeInstance( entityComposite );
        if( !entityInstance.isReference() )
        {
            EntityStatus entityStatus = entityInstance.getState().getStatus();
            if( entityStatus == EntityStatus.REMOVED )
            {
                throw new EntityCompositeNotFoundException( entityInstance.getIdentity(), entityInstance.getContext().getCompositeModel().getCompositeType() );
            }
            else if( entityStatus == EntityStatus.NEW )
            {
                return; // Don't try to refresh newly created state
            }

            // Refresh the state
            try
            {
                EntityId identity = new EntityId( entityInstance.getIdentity(),
                                                  entityInstance.getContext().getCompositeModel().getCompositeType().getName() );
                EntityState state = entityInstance.getStore().getEntityState( entityInstance.getContext().getCompositeResolution().getCompositeDescriptor(), identity );
                entityInstance.refresh( state );
                entityInstance.setMixins( null );
            }
            catch( EntityStoreException e )
            {
                throw new UnitOfWorkException( e );
            }
        }
    }

    public void refresh()
        throws UnitOfWorkException
    {
        // Refresh the entire unit of work
        for( Map<String, EntityComposite> map : cache.values() )
        {
            for( EntityComposite entity : map.values() )
            {
                refresh( entity );
            }
        }
    }

    public void reset()
    {
        checkOpen();

        cache.clear();
    }

    public boolean contains( Object entity )
    {
        checkOpen();

        EntityComposite entityComposite = (EntityComposite) entity;
        return getCachedEntity( entityComposite.identity().get(), entityComposite.type() ) != null;
    }

    public CompositeBuilderFactory compositeBuilderFactory()
    {
        return moduleInstance.getStructureContext().getCompositeBuilderFactory();
    }

    public ObjectBuilderFactory objectBuilderFactory()
    {
        return moduleInstance.getStructureContext().getObjectBuilderFactory();
    }

    public void pause()
    {
        if( !paused )
        {
            paused = true;
            current.get().pop();
        }
    }

    public void resume()
    {
        if( paused )
        {
            paused = false;
            current.get().push( this );
        }
    }

    public QueryBuilderFactory queryBuilderFactory()
    {
        checkOpen();
        if( queryBuilderFactory == null )
        {
            queryBuilderFactory = new QueryBuilderFactoryImpl( this );
        }
        return queryBuilderFactory;
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
        throws UnitOfWorkCompletionException
    {
        checkOpen();

        // Create complete lists
        Map<EntityStore, StoreCompletion> storeCompletion = new HashMap<EntityStore, StoreCompletion>();
        for( Map.Entry<Class<? extends EntityComposite>, Map<String, EntityComposite>> entry : cache.entrySet() )
        {
            EntityStore store = stateServices.getEntityStore( entry.getKey() );
            StoreCompletion storeCompletionList = storeCompletion.get( store );
            if( storeCompletionList == null )
            {
                storeCompletionList = new StoreCompletion();
                storeCompletion.put( store, storeCompletionList );
            }

            Map<String, EntityComposite> entities = entry.getValue();
            for( EntityComposite entityInstance : entities.values() )
            {
                EntityState state = EntityCompositeInstance.getEntityCompositeInstance( entityInstance ).getState();
                if( state.getStatus() == EntityStatus.NEW )
                {
                    storeCompletionList.getNewState().add( state );
                }
                else if( state.getStatus() == EntityStatus.LOADED )
                {
                    storeCompletionList.getUpdatedState().add( state );
                }
                if( state.getStatus() == EntityStatus.REMOVED )
                {
                    storeCompletionList.getRemovedState().add( state.getIdentity() );
                }
            }
        }

        // Commit complete lists
        List<StateCommitter> committers = new ArrayList<StateCommitter>();
        for( Map.Entry<EntityStore, StoreCompletion> entityStoreListEntry : storeCompletion.entrySet() )
        {
            EntityStore entityStore = entityStoreListEntry.getKey();
            StoreCompletion completion = entityStoreListEntry.getValue();

            try
            {
                committers.add( entityStore.prepare( completion.getNewState(), completion.getUpdatedState(), completion.getRemovedState(), moduleInstance.getModuleContext().getModuleBinding() ) );
            }
            catch( EntityStoreException e )
            {
                // Cancel all previously prepared stores
                for( StateCommitter committer : committers )
                {
                    committer.cancel();
                }

                throw new UnitOfWorkCompletionException( e );
            }
        }

        // Commit all changes
        for( StateCommitter committer : committers )
        {
            committer.commit();
        }

        cache.clear();
        open = false;
        current.get().pop();
    }

    public void discard()
    {
        checkOpen();
        open = false;
        cache.clear();
        current.get().pop();
    }

    public boolean isOpen()
    {
        return open;
    }

    public UnitOfWork newUnitOfWork()
    {
        checkOpen();

        return new UnitOfWorkInstance( moduleInstance, new UnitOfWorkStateServices() );
    }

    public ModuleInstance getModuleInstance()
    {
        checkOpen();

        return moduleInstance;
    }

    void createEntity( EntityComposite instance )
    {
        Class<? extends EntityComposite> compositeType = (Class<? extends EntityComposite>) instance.type();
        Map<String, EntityComposite> entityCache = getEntityCache( compositeType );
        entityCache.put( instance.identity().get(), instance );
    }

    Map<String, EntityComposite> getEntityCache( Class<? extends EntityComposite> compositeType )
    {
        Map<String, EntityComposite> entityCache = cache.get( compositeType );

        if( entityCache == null )
        {
            entityCache = new HashMap<String, EntityComposite>();
            cache.put( compositeType, entityCache );
        }

        return entityCache;
    }

    private EntityComposite getCachedEntity( String identity, Class compositeType )
    {
        Map<String, EntityComposite> entityCache = cache.get( compositeType );

        if( entityCache == null )
        {
            return null;
        }

        return entityCache.get( identity );
    }

    private EntityState getCachedState( EntityId entityId )
    {
        String type = entityId.getCompositeType();
        Class compositeType = moduleInstance.getModuleContext().getModuleBinding().lookupClass( type );
        Map<String, EntityComposite> entityCache = cache.get( compositeType );
        if( entityCache == null )
        {
            return null;
        }

        EntityComposite composite = entityCache.get( entityId.getIdentity() );
        if( composite != null )
        {
            return EntityCompositeInstance.getEntityCompositeInstance( composite ).getState();
        }
        else
        {
            return null;
        }
    }

    private void checkOpen()
    {
        if( !isOpen() )
        {
            throw new UnitOfWorkException( "Unit of work has been closed" );
        }
    }

    private class UnitOfWorkStateServices
        implements StateServices
    {
        private UnitOfWorkStore store = new UnitOfWorkStore();

        public EntityStore getEntityStore( Class<? extends EntityComposite> compositeType )
        {
            return store;
        }

        public IdentityGenerator getIdentityGenerator( Class<? extends EntityComposite> compositeType )
        {
            return stateServices.getIdentityGenerator( compositeType );
        }
    }

    private class UnitOfWorkStore
        implements EntityStore
    {
        public EntityState newEntityState( CompositeDescriptor compositeDescriptor, EntityId identity ) throws EntityStoreException
        {
            UnitOfWorkEntityState entityState = new UnitOfWorkEntityState( 0, identity, EntityStatus.NEW, new HashMap<String, Object>(), new HashMap<String, EntityId>(), new HashMap<String, Collection<EntityId>>(), null );
            return entityState;
        }

        public EntityState getEntityState( CompositeDescriptor compositeDescriptor, EntityId identity ) throws EntityStoreException
        {
            EntityState parentState = getCachedState( identity );
            if( parentState == null )
            {
                // Force load into parent
                Class<? extends EntityComposite> entityType = (Class<? extends EntityComposite>) moduleInstance.getModuleContext().getModuleBinding().lookupClass( identity.getCompositeType() );
                parentState = EntityCompositeInstance.getEntityCompositeInstance( find( identity.getIdentity(), entityType ) ).getState();
            }
            UnitOfWorkEntityState unitOfWorkEntityState = new UnitOfWorkEntityState( parentState.getEntityVersion(),
                                                                                     identity,
                                                                                     EntityStatus.LOADED,
                                                                                     new HashMap<String, Object>(),
                                                                                     new HashMap<String, EntityId>(),
                                                                                     new HashMap<String, Collection<EntityId>>(),
                                                                                     parentState );
            return unitOfWorkEntityState;
        }

        public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<EntityId> removedStates, ModuleBinding moduleBinding ) throws EntityStoreException
        {
            // Create new entity and transfer state
            for( EntityState newState : newStates )
            {
                UnitOfWorkEntityState uowState = (UnitOfWorkEntityState) newState;
                EntityComposite entityInstance = newEntityBuilder( uowState.getIdentity().getIdentity(), (Class<? extends EntityComposite>) moduleInstance.getModuleContext().getModuleBinding().lookupClass( uowState.getIdentity().getCompositeType() ) ).newInstance();

                EntityState parentState = EntityCompositeInstance.getEntityCompositeInstance( entityInstance ).getState();
                Iterable<String> propertyNames = uowState.getPropertyNames();
                for( String propertyName : propertyNames )
                {
                    parentState.setProperty( propertyName, uowState.getProperty( propertyName ) );
                }
                Iterable<String> associationNames = uowState.getAssociationNames();
                for( String associationName : associationNames )
                {
                    parentState.setAssociation( associationName, uowState.getAssociation( associationName ) );
                }
                Iterable<String> manyAssociationNames = uowState.getManyAssociationNames();
                for( String manyAssociationName : manyAssociationNames )
                {
                    Collection<EntityId> collection = parentState.getManyAssociation( manyAssociationName );
                    Collection<EntityId> newCollection = uowState.getManyAssociation( manyAssociationName );

                    // TODO This can be soooo much more optimized by comparing the collections and matching them
                    // up by doing individual add/removes
                    collection.clear();
                    collection.addAll( newCollection );
                }
            }

            // Copy state back to already loaded entities
            for( EntityState loadedState : loadedStates )
            {
                UnitOfWorkEntityState uowState = (UnitOfWorkEntityState) loadedState;
                if( uowState.isChanged() )
                {
                    EntityState parentState = uowState.getParentState();
                    Iterable<String> propertyNames = uowState.getPropertyNames();
                    for( String propertyName : propertyNames )
                    {
                        parentState.setProperty( propertyName, uowState.getProperty( propertyName ) );
                    }
                    Iterable<String> associationNames = uowState.getAssociationNames();
                    for( String associationName : associationNames )
                    {
                        parentState.setAssociation( associationName, uowState.getAssociation( associationName ) );
                    }
                    Iterable<String> manyAssociationNames = uowState.getManyAssociationNames();
                    for( String manyAssociationName : manyAssociationNames )
                    {
                        Collection<EntityId> collection = parentState.getManyAssociation( manyAssociationName );
                        Collection<EntityId> newCollection = uowState.getManyAssociation( manyAssociationName );

                        // TODO This can be soooo much more optimized by comparing the collections and matching them
                        // up by doing individual add/removes
                        collection.clear();
                        collection.addAll( newCollection );
                    }

                    Class<? extends EntityComposite> entityType = (Class<? extends EntityComposite>) moduleInstance.getModuleContext().getModuleBinding().lookupClass( uowState.getIdentity().getCompositeType() );
                    EntityComposite instance = find( uowState.getIdentity().getIdentity(), entityType );
                    EntityCompositeInstance.getEntityCompositeInstance( instance ).refresh( parentState );
                }
            }

            // Remove entities
            for( EntityId removedState : removedStates )
            {
                EntityState parentState = getCachedState( removedState );
                if( parentState != null )
                {
                    parentState.remove(); // Mark for deletion when parent unit completes
                }
            }

            return new StateCommitter()
            {
                public void commit()
                {
                }

                public void cancel()
                {
                }
            };
        }
    }

    private static class UnitOfWorkEntityState
        extends EntityStateInstance
    {
        private EntityState parentState;
        private long entityVersion;

        private UnitOfWorkEntityState( long entityVersion, EntityId identity, EntityStatus status, Map<String, Object> properties, Map<String, EntityId> associations, Map<String, Collection<EntityId>> manyAssociations, EntityState parentState )
        {
            super( entityVersion, identity, status, properties, associations, manyAssociations );
            this.parentState = parentState;
            this.entityVersion = entityVersion;
        }

        public long getEntityVersion()
        {
            return entityVersion;
        }

        public Object getProperty( String qualifiedName )
        {
            if( properties.containsKey( qualifiedName ) )
            {
                return properties.get( qualifiedName );
            }

            // Get from parent state
            return parentState == null ? null : parentState.getProperty( qualifiedName );
        }

        public EntityId getAssociation( String qualifiedName )
        {
            if( associations.containsKey( qualifiedName ) )
            {
                return associations.get( qualifiedName );
            }

            return parentState == null ? null : parentState.getAssociation( qualifiedName );
        }

        public Collection<EntityId> getManyAssociation( String qualifiedName )
        {
            if( manyAssociations.containsKey( qualifiedName ) )
            {
                return manyAssociations.get( qualifiedName );
            }

            return parentState == null ? null : parentState.getManyAssociation( qualifiedName );
        }

        public EntityState getParentState()
        {
            return parentState;
        }

        public boolean isChanged()
        {
            return properties.size() > 0 || associations.size() > 0 || manyAssociations.size() > 0;
        }
    }

    private static class StoreCompletion
    {
        List<EntityState> newState;
        List<EntityState> updatedState;
        List<EntityId> removedState;

        private StoreCompletion()
        {
            this.newState = new ArrayList<EntityState>();
            this.updatedState = new ArrayList<EntityState>();
            this.removedState = new ArrayList<EntityId>();
        }

        public List<EntityState> getNewState()
        {
            return newState;
        }

        public List<EntityState> getUpdatedState()
        {
            return updatedState;
        }

        public List<EntityId> getRemovedState()
        {
            return removedState;
        }
    }
}
