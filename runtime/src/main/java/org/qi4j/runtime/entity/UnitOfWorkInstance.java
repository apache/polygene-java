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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.Identity;
import org.qi4j.entity.LoadingPolicy;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.UnitOfWorkException;
import org.qi4j.entity.UnitOfWorkSynchronization;
import static org.qi4j.entity.UnitOfWorkSynchronization.UnitOfWorkStatus.COMPLETED;
import static org.qi4j.entity.UnitOfWorkSynchronization.UnitOfWorkStatus.DISCARDED;
import org.qi4j.object.ObjectBuilderFactory;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilderFactory;
import org.qi4j.runtime.query.QueryBuilderFactoryImpl;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStateInstance;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.structure.Module;

public final class UnitOfWorkInstance
    implements UnitOfWork
{
    public static ThreadLocal<Stack<UnitOfWork>> current;

    private HashMap<Class<? extends EntityComposite>, Map<String, EntityComposite>> cache;

    private boolean open;

    private boolean paused;

    private ModuleInstance moduleInstance;

    /**
     * Lazy query builder factory.
     */
    private QueryBuilderFactory queryBuilderFactory;

    private LoadingPolicy loadingPolicy;

    private List<UnitOfWorkSynchronization> synchronizations;

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

    public UnitOfWorkInstance( ModuleInstance moduleInstance )
    {
        this.moduleInstance = moduleInstance;
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

    public <T> EntityBuilder<T> newEntityBuilder( Class<T> mixinType )
    {
        return newEntityBuilder( null, mixinType );
    }

    public <T> EntityBuilder<T> newEntityBuilder( String identity, Class<T> mixinType )
    {
        checkOpen();

        ModuleInstance realModuleInstance = moduleInstance.findModuleForEntity( mixinType );
        EntityBuilder<T> builder = realModuleInstance.entities().newEntityBuilder( mixinType, this );
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

        EntityInstance compositeInstance = EntityInstance.getEntityInstance( entityComposite );
        compositeInstance.remove();
    }

    public <T> T find( String identity, Class<T> mixinType )
        throws EntityCompositeNotFoundException
    {
        checkOpen();

        final ModuleInstance realModule = moduleInstance.findModuleForEntity( mixinType );
        EntityModel entityModel = realModule.entities().model().getEntityModelFor( mixinType );

        // TODO: Argument check.

        try
        {
            EntityComposite entity = getCachedEntity( identity, mixinType );
            if( entity == null )
            {   // Not yet in cache
                EntityInstance entityInstance = realModule.entities().loadEntityInstance( identity, entityModel, this );
                Map<String, EntityComposite> entityCache = getEntityCache( entityModel.type() );
                entity = entityInstance.proxy();
                entityCache.put( identity, entity );
            }
            else
            {
                EntityInstance entityInstance = EntityInstance.getEntityInstance( entity );
                if( entityInstance.isReference() )
                {
                    // Check that state exists
                    entityInstance.load();
                }
                else
                {
                    // Check if it has been removed
                    if( entityInstance.status() == EntityStatus.REMOVED )
                    {
                        throw new EntityCompositeNotFoundException( identity, entityModel.type().getName() );
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

        ModuleInstance moduleInstance = this.moduleInstance.findModuleForEntity( mixinType );
        EntityModel entityModel = moduleInstance.entities().model().getEntityModelFor( mixinType );

        EntityComposite entity = getCachedEntity( identity, entityModel.type() );
        if( entity == null )
        {
            // Create entity moduleInstance
            EntityInstance compositeInstance = this.moduleInstance.entities().getEntityInstance( identity, entityModel, this );
            entity = compositeInstance.proxy();
            Map<String, EntityComposite> entityCache = getEntityCache( entityModel.type() );
            entityCache.put( identity, entity );
        }
        else
        {
            // Check if it has been removed
            EntityInstance entityInstance = EntityInstance.getEntityInstance( entity );
            if( entityInstance.status() == EntityStatus.REMOVED )
            {
                throw new EntityCompositeNotFoundException( identity, entityModel.type().getName() );
            }
        }

        return mixinType.cast( entity );
    }

    public <T> T dereference( T entity )
        throws EntityCompositeNotFoundException
    {
        EntityComposite entityComposite = (EntityComposite) entity;
        EntityInstance compositeInstance = EntityInstance.getEntityInstance( entityComposite );
        String id = compositeInstance.identity().identity();
        Class<? extends EntityComposite> type = compositeInstance.type();
        return (T) getReference( id, type );
    }

    public void refresh( Object entity )
        throws UnitOfWorkException
    {
        checkOpen();

        EntityComposite entityComposite = (EntityComposite) entity;
        EntityInstance entityInstance = EntityInstance.getEntityInstance( entityComposite );
        if( !entityInstance.isReference() )
        {
            EntityStatus entityStatus = entityInstance.status();
            if( entityStatus == EntityStatus.REMOVED )
            {
                throw new EntityCompositeNotFoundException( entityInstance.identity().identity(), entityInstance.type().getName() );
            }
            else if( entityStatus == EntityStatus.NEW )
            {
                return; // Don't try to refresh newly created state
            }

            // Refresh the state
            try
            {

                entityInstance.refresh();
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
        return moduleInstance.compositeBuilderFactory();
    }

    public ObjectBuilderFactory objectBuilderFactory()
    {
        return moduleInstance.objectBuilderFactory();
    }

    public LoadingPolicy loadingPolicy()
    {
        return loadingPolicy;
    }

    public void setLoadingPolicy( LoadingPolicy loadingPolicy )
    {
        this.loadingPolicy = loadingPolicy;
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

        // Check synchronizations
        if( synchronizations != null )
        {
            for( UnitOfWorkSynchronization synchronization : synchronizations )
            {
                synchronization.beforeCompletion();
            }
        }

        // Create complete lists
        Map<EntityStore, StoreCompletion> storeCompletion = new HashMap<EntityStore, StoreCompletion>();
        for( Map.Entry<Class<? extends EntityComposite>, Map<String, EntityComposite>> entry : cache.entrySet() )
        {
            Map<String, EntityComposite> entities = entry.getValue();
            for( EntityComposite entityInstance : entities.values() )
            {
                EntityInstance instance = EntityInstance.getEntityInstance( entityInstance );

                EntityStore store = instance.store();
                StoreCompletion storeCompletionList = storeCompletion.get( store );
                if( storeCompletionList == null )
                {
                    storeCompletionList = new StoreCompletion();
                    storeCompletion.put( store, storeCompletionList );
                }

                if( instance.status() == EntityStatus.LOADED )
                {
                    storeCompletionList.getUpdatedState().add( instance.state() );
                }
                else if( instance.status() == EntityStatus.NEW )
                {
                    storeCompletionList.getNewState().add( instance.state() );
                }
                if( instance.status() == EntityStatus.REMOVED )
                {
                    storeCompletionList.getRemovedState().add( instance.identity() );
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
                committers.add( entityStore.prepare( completion.getNewState(), completion.getUpdatedState(), completion.getRemovedState(), moduleInstance ) );
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

        // Call synchronizations
        notifySynchronizations( COMPLETED );
    }

    public void discard()
    {
        checkOpen();
        open = false;
        cache.clear();

        current.get().pop();

        // Call synchronizations
        notifySynchronizations( DISCARDED );
    }

    public boolean isOpen()
    {
        return open;
    }

    public UnitOfWork newUnitOfWork()
    {
        checkOpen();

        return new UnitOfWorkInstance( moduleInstance );
    }

    public ModuleInstance module()
    {
        checkOpen();

        return moduleInstance;
    }

    public void registerUnitOfWorkSynchronization( UnitOfWorkSynchronization synchronization )
    {
        if( synchronizations == null )
        {
            synchronizations = new ArrayList<UnitOfWorkSynchronization>();
        }

        synchronizations.add( synchronization );
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

    private void notifySynchronizations( UnitOfWorkSynchronization.UnitOfWorkStatus status )
    {
        if( synchronizations != null )
        {
            for( UnitOfWorkSynchronization synchronization : synchronizations )
            {
                synchronization.afterCompletion( status );
            }
        }
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

    private EntityState getCachedState( QualifiedIdentity entityId )
    {
        String type = entityId.type();
        Class compositeType = moduleInstance.findClassForName( type );
        Map<String, EntityComposite> entityCache = cache.get( compositeType );
        if( entityCache == null )
        {
            return null;
        }

        EntityComposite composite = entityCache.get( entityId.identity() );
        if( composite != null )
        {
            return EntityInstance.getEntityInstance( composite ).state();
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

    private class UnitOfWorkStore
        implements EntityStore
    {
        Map<String, Class<? extends EntityComposite>> entityTypes = new HashMap<String, Class<? extends EntityComposite>>();

        public EntityState newEntityState( CompositeDescriptor compositeDescriptor, QualifiedIdentity identity ) throws EntityStoreException
        {
            if( entityTypes.get( identity.type() ) == null )
            {
                entityTypes.put( identity.type(), (Class<? extends EntityComposite>) compositeDescriptor.type() );
            }

            UnitOfWorkEntityState entityState = new UnitOfWorkEntityState( 0, identity, EntityStatus.NEW, new HashMap<String, Object>(), new HashMap<String, QualifiedIdentity>(), new HashMap<String, Collection<QualifiedIdentity>>(), null );
            return entityState;
        }

        public EntityState getEntityState( CompositeDescriptor compositeDescriptor, QualifiedIdentity identity ) throws EntityStoreException
        {
            if( entityTypes.get( identity.type() ) == null )
            {
                entityTypes.put( identity.type(), (Class<? extends EntityComposite>) compositeDescriptor.type() );
            }

            EntityState parentState = getCachedState( identity );
            if( parentState == null )
            {
                // Force load into parent
                parentState = EntityInstance.getEntityInstance( find( identity.identity(), compositeDescriptor.type() ) ).state();
            }

            UnitOfWorkEntityState unitOfWorkEntityState = new UnitOfWorkEntityState( parentState.getEntityVersion(),
                                                                                     identity,
                                                                                     EntityStatus.LOADED,
                                                                                     new HashMap<String, Object>(),
                                                                                     new HashMap<String, QualifiedIdentity>(),
                                                                                     new HashMap<String, Collection<QualifiedIdentity>>(),
                                                                                     parentState );
            return unitOfWorkEntityState;
        }

        public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates, Module module ) throws EntityStoreException
        {
            // Create new entity and transfer state
            for( EntityState newState : newStates )
            {
                UnitOfWorkEntityState uowState = (UnitOfWorkEntityState) newState;
                Class<? extends EntityComposite> type = entityTypes.get( uowState.getIdentity().type() );
                EntityComposite entityInstance = newEntityBuilder( uowState.getIdentity().identity(), type ).newInstance();

                EntityState parentState = EntityInstance.getEntityInstance( entityInstance ).state();
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
                    Collection<QualifiedIdentity> collection = parentState.getManyAssociation( manyAssociationName );
                    Collection<QualifiedIdentity> newCollection = uowState.getManyAssociation( manyAssociationName );

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
                        Collection<QualifiedIdentity> collection = parentState.getManyAssociation( manyAssociationName );
                        Collection<QualifiedIdentity> newCollection = uowState.getManyAssociation( manyAssociationName );

                        // TODO This can be soooo much more optimized by comparing the collections and matching them
                        // up by doing individual add/removes
                        collection.clear();
                        collection.addAll( newCollection );
                    }

                    Class<? extends EntityComposite> type = entityTypes.get( uowState.getIdentity().type() );
                    EntityComposite instance = find( uowState.getIdentity().identity(), type );
                    EntityInstance.getEntityInstance( instance ).refresh( parentState );
                }
            }

            // Remove entities
            for( QualifiedIdentity removedState : removedStates )
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

        public Iterator<EntityState> iterator()
        {
            return null;
        }
    }

    private static class UnitOfWorkEntityState
        extends EntityStateInstance
    {
        private EntityState parentState;
        private long entityVersion;

        private UnitOfWorkEntityState( long entityVersion,
                                       QualifiedIdentity identity,
                                       EntityStatus status,
                                       Map<String, Object> properties,
                                       Map<String, QualifiedIdentity> associations,
                                       Map<String, Collection<QualifiedIdentity>> manyAssociations,
                                       EntityState parentState )
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

        public QualifiedIdentity getAssociation( String qualifiedName )
        {
            if( associations.containsKey( qualifiedName ) )
            {
                return associations.get( qualifiedName );
            }

            return parentState == null ? null : parentState.getAssociation( qualifiedName );
        }

        public Collection<QualifiedIdentity> getManyAssociation( String qualifiedName )
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
        List<QualifiedIdentity> removedState;

        private StoreCompletion()
        {
            this.newState = new ArrayList<EntityState>();
            this.updatedState = new ArrayList<EntityState>();
            this.removedState = new ArrayList<QualifiedIdentity>();
        }

        public List<EntityState> getNewState()
        {
            return newState;
        }

        public List<EntityState> getUpdatedState()
        {
            return updatedState;
        }

        public List<QualifiedIdentity> getRemovedState()
        {
            return removedState;
        }
    }
}
