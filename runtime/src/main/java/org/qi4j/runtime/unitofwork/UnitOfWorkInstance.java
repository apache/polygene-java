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
package org.qi4j.runtime.unitofwork;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import static org.qi4j.api.unitofwork.UnitOfWorkCallback.UnitOfWorkStatus.COMPLETED;
import static org.qi4j.api.unitofwork.UnitOfWorkCallback.UnitOfWorkStatus.DISCARDED;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkException;
import org.qi4j.api.usecase.StateUsage;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.query.QueryBuilderFactoryImpl;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.ConcurrentEntityStateModificationException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.StateCommitter;

public final class UnitOfWorkInstance
{
    public static final ThreadLocal<Stack<UnitOfWorkInstance>> current;

    final HashMap<String, EntityStateStore> stateCache;

    private boolean open;

    private boolean paused;

    /**
     * Lazy query builder factory.
     */
    private Usecase usecase;

    private List<UnitOfWorkCallback> callbacks;
    private UnitOfWorkStore unitOfWorkStore;
    private MetaInfo metaInfo;

    static
    {
        current = new ThreadLocal<Stack<UnitOfWorkInstance>>()
        {
            protected Stack<UnitOfWorkInstance> initialValue()
            {
                return new Stack<UnitOfWorkInstance>();
            }
        };
        QueryBuilderFactoryImpl.initialize();
    }

    public UnitOfWorkInstance( Usecase usecase )
    {
        this.open = true;
        stateCache = new HashMap<String, EntityStateStore>();
        current.get().push( this );
        paused = false;
        this.usecase = usecase;
    }

    // Nested unit of work
    public UnitOfWorkInstance( Usecase nestedUsecase, UnitOfWorkStore unitOfWorkStore )
    {
        this( nestedUsecase );
        this.unitOfWorkStore = unitOfWorkStore;
    }

    public <T> EntityBuilder<T> newEntityBuilder( String identity, EntityModel model, EntityStore store, ModuleInstance moduleInstance, ModuleUnitOfWork moduleUnitOfWork )
        throws NoSuchEntityException
    {
        checkOpen();

        EntityBuilder<T> builder;

        if( identity != null )
        {
            builder = new EntityBuilderInstance<T>( moduleInstance, model, moduleUnitOfWork, store, identity );
        }
        else
        {
            builder = new EntityBuilderInstance<T>( moduleInstance, model, moduleUnitOfWork, store, moduleInstance.entities().identityGenerator() );
        }
        return builder;
    }

    public EntityInstance get( EntityReference identity, ModuleUnitOfWork uow, EntityModel model, ModuleInstance module)
        throws EntityTypeNotFoundException, NoSuchEntityException
    {
        checkOpen();

        EntityStateStore entityStateStore = stateCache.get( identity.toString() );
        EntityInstance entityInstance;
        if( entityStateStore == null )
        {   // Not yet in cache

            entityStateStore = getEffectiveEntityStateStore( identity, model.entityType() );
            EntityStore entityStore;

            // Check if this is a root UoW, or if no parent UoW knows about this entity
            if (unitOfWorkStore == null || entityStateStore == null)
                entityStore = module.entities().entityStore();
            else
            {
                entityStore = unitOfWorkStore;
            }

            EntityState entityState = null;
            try
            {
                entityState = entityStore.getEntityState(identity);
            } catch (EntityNotFoundException e)
            {
                throw new NoSuchEntityException(e.identity());
            }
            entityInstance = new EntityInstance( uow, module, model, identity, entityState );

            if( entityStateStore == null )
            {
                entityStateStore = new EntityStateStore();
                entityStateStore.state = entityInstance.entityState();
                entityStateStore.store = entityStore;
            }

            entityStateStore.instance = entityInstance;

            stateCache.put( identity.toString(), entityStateStore );
        }
        else
        {
            entityInstance = entityStateStore.instance;
            // Check if it has been removed
            if( entityInstance.status() == EntityStatus.REMOVED )
            {
                throw new NoSuchEntityException( identity);
            }
        }

        return entityInstance;
    }

    public void refresh( Object entity )
        throws UnitOfWorkException, NoSuchEntityException
    {
        checkOpen();

        EntityComposite entityComposite = (EntityComposite) entity;
        EntityInstance entityInstance = EntityInstance.getEntityInstance( entityComposite );

        EntityStatus entityStatus = entityInstance.status();
        if( entityStatus == EntityStatus.REMOVED )
        {
            throw new NoSuchEntityException( entityInstance.identity());
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
        catch( EntityNotFoundException e )
        {
            throw new NoSuchEntityException( entityInstance.identity());
        }
        catch( EntityStoreException e )
        {
            throw new UnitOfWorkException( e );
        }
    }

    public void refresh()
        throws UnitOfWorkException
    {
        // Refresh the entire unit of work
        for( EntityStateStore entityStateStore : stateCache.values() )
        {
            try
            {
                if( entityStateStore.instance != null )
                {
                    refresh( entityStateStore.instance.proxy() );
                }
            }
            catch( NoSuchEntityException e )
            {
                // Ignore
            }
        }
    }

    public Usecase usecase()
    {
        return usecase;
    }

    public MetaInfo metaInfo()
    {
        if( metaInfo == null )
        {
            metaInfo = new MetaInfo();
        }
        return metaInfo;
    }

    public void pause()
    {
        if( !paused )
        {
            paused = true;
            current.get().pop();
        } else
        {
            throw new UnitOfWorkException( "Unit of work is not active" );
        }
    }

    public void resume()
    {
        if( paused )
        {
            paused = false;
            current.get().push( this );
        } else
        {
            throw new UnitOfWorkException( "Unit of work has not been paused" );
        }
    }

    public void complete()
        throws UnitOfWorkCompletionException
    {
        complete( false );
    }

    public void apply()
        throws UnitOfWorkCompletionException, ConcurrentEntityModificationException
    {
        complete( true );
    }

    public EntityState refresh(String identity)
    {
        if (unitOfWorkStore != null)
        {
            unitOfWorkStore.refresh(identity);
        }

        EntityStateStore ess = stateCache.get( identity );
        if (ess != null)
        {
            // Refresh state
            return ess.state = ess.store.getEntityState( new EntityReference(identity) );
        } else
        {
            return null; // TODO Can this happen?
        }
    }

    private void complete( boolean completeAndContinue )
        throws UnitOfWorkCompletionException
    {
        checkOpen();

        // Copy list so that it cannot be modified during completion
        List<UnitOfWorkCallback> currentCallbacks = callbacks == null ? null : new ArrayList<UnitOfWorkCallback>( callbacks );

        // Check callbacks
        notifyBeforeCompletion( currentCallbacks );

        if( unitOfWorkStore != null )
        {
            // Merge state with underlying UoW
            unitOfWorkStore.mergeWith( stateCache );
        }
        else
        {
            // Commit state to EntityStores
            Map<EntityStore, StoreCompletion> storeCompletion = createCompleteLists();

            List<StateCommitter> committers = commitCompleteLists( storeCompletion );

            // Commit all changes
            for( StateCommitter committer : committers )
            {
                committer.commit();
            }
        }

        if( completeAndContinue )
        {
            continueWithState();
        }
        else
        {
            close();
        }

        // Call callbacks
        notifyAfterCompletion( currentCallbacks, COMPLETED );

        callbacks = currentCallbacks;
    }

    public void discard()
    {
        if( !isOpen() )
        {
            return;
        }
        close();

        // Copy list so that it cannot be modified during completion
        List<UnitOfWorkCallback> currentCallbacks = callbacks == null ? null : new ArrayList<UnitOfWorkCallback>( callbacks );

        // Call callbacks
        notifyAfterCompletion( currentCallbacks, DISCARDED );

        callbacks = currentCallbacks;
    }

    private void close()
    {
        checkOpen();
        current.get().pop();
        open = false;
        stateCache.clear();

        // Turn off recording for the state usage
        StateUsage stateUsage = usecase.metaInfo().get( StateUsage.class );
        if( stateUsage != null && stateUsage.isRecording() )
        {
            stateUsage.setRecording( false );
        }
    }

    public boolean isOpen()
    {
        return open;
    }

    public void addUnitOfWorkCallback( UnitOfWorkCallback callback )
    {
        if( callbacks == null )
        {
            callbacks = new ArrayList<UnitOfWorkCallback>();
        }

        callbacks.add( callback );
    }

    public void removeUnitOfWorkCallback( UnitOfWorkCallback callback )
    {
        if( callbacks != null )
        {
            callbacks.remove( callback );
        }
    }

    public void createEntity( EntityInstance instance, EntityStore entityStore )
    {
        String qid = instance.identity().toString();
        EntityStateStore entityStateStore = new EntityStateStore();
        entityStateStore.instance = instance;
        entityStateStore.state = instance.entityState();
        entityStateStore.store = entityStore;
        stateCache.put( qid, entityStateStore );
    }

    private List<StateCommitter> commitCompleteLists( Map<EntityStore, StoreCompletion> storeCompletion )
        throws UnitOfWorkCompletionException
    {
        List<StateCommitter> committers = new ArrayList<StateCommitter>();
        for( Map.Entry<EntityStore, StoreCompletion> entityStoreListEntry : storeCompletion.entrySet() )
        {
            EntityStore entityStore = entityStoreListEntry.getKey();
            StoreCompletion completion = entityStoreListEntry.getValue();

            try
            {
                committers.add( entityStore.prepare( completion.getNewState(), completion.getUpdatedState(), completion.getRemovedState() ) );
            }
            catch( Exception e )
            {
                // Cancel all previously prepared stores
                for( StateCommitter committer : committers )
                {
                    committer.cancel();
                }

                if( e instanceof ConcurrentEntityStateModificationException )
                {
                    // If we cancelled due to concurrent modification, then create the proper exception for it!
                    ConcurrentEntityStateModificationException mee = (ConcurrentEntityStateModificationException) e;
                    Collection<EntityReference> modifiedEntityIdentities = mee.modifiedEntities();
                    Collection<EntityComposite> modifiedEntities = new ArrayList<EntityComposite>();
                    for( EntityReference modifiedEntityIdentity : modifiedEntityIdentities )
                    {
                        EntityStateStore entityStateStore = stateCache.get( modifiedEntityIdentity.toString() );
                        modifiedEntities.add( entityStateStore.instance.<EntityComposite>proxy() );
                    }
                    throw new ConcurrentEntityModificationException( modifiedEntities );
                }
                else
                {
                    throw new UnitOfWorkCompletionException( e );
                }
            }
        }
        return committers;
    }

    private Map<EntityStore, StoreCompletion> createCompleteLists()
    {
        Map<EntityStore, StoreCompletion> storeCompletion = new HashMap<EntityStore, StoreCompletion>();
        for( EntityStateStore entityStateStore : stateCache.values() )
        {
            EntityStore store = entityStateStore.store;
            StoreCompletion storeCompletionList = storeCompletion.get( store );
            if( storeCompletionList == null )
            {
                storeCompletionList = new StoreCompletion();
                storeCompletion.put( store, storeCompletionList );
            }
            EntityState entityState = entityStateStore.state;
            if (entityState != null)
            {
                if( entityState.status() == EntityStatus.LOADED )
                {
                    storeCompletionList.getUpdatedState().add( entityState );
                }
                else if( entityState.status() == EntityStatus.NEW )
                {
                    storeCompletionList.getNewState().add( entityState );
                }
                else
                {
                    storeCompletionList.getRemovedState().add( entityState.identity() );
                }
            }
        }

        return storeCompletion;
    }

    private void continueWithState()
    {
        Iterator<EntityStateStore> stateStores = stateCache.values().iterator();
        while( stateStores.hasNext() )
        {
            EntityStateStore entityStateStore = stateStores.next();
            EntityState entityState = entityStateStore.state;
            if( entityState.status() != EntityStatus.REMOVED )
            {
                entityState.hasBeenApplied();
            }
            else
            {
                stateStores.remove();
            }
        }
    }

    private void notifyBeforeCompletion( List<UnitOfWorkCallback> callbacks )
        throws UnitOfWorkCompletionException
    {
        // Notify explicitly registered callbacks
        if( callbacks != null )
        {
            for( UnitOfWorkCallback callback : callbacks )
            {
                callback.beforeCompletion();
            }
        }

        // Notify entities
        try
        {
            new ForEachEntity()
            {
                protected void execute( EntityInstance instance )
                    throws Exception
                {
                    if( instance.<Object>proxy() instanceof UnitOfWorkCallback )
                    {
                        UnitOfWorkCallback callback = UnitOfWorkCallback.class.cast( instance.proxy() );
                        callback.beforeCompletion();
                    }
                }
            }.execute();
        }
        catch( UnitOfWorkCompletionException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            throw new UnitOfWorkCompletionException( e );
        }
    }

    private void notifyAfterCompletion( List<UnitOfWorkCallback> callbacks, final UnitOfWorkCallback.UnitOfWorkStatus status )
    {
        if( callbacks != null )
        {
            for( UnitOfWorkCallback callback : callbacks )
            {
                try
                {
                    callback.afterCompletion( status );
                }
                catch( Exception e )
                {
                    // Ignore
                }
            }
        }

        // Notify entities
        try
        {
            new ForEachEntity()
            {
                protected void execute( EntityInstance instance )
                    throws Exception
                {
                    if( instance.<Object>proxy() instanceof UnitOfWorkCallback )
                    {
                        UnitOfWorkCallback callback = UnitOfWorkCallback.class.cast( instance.proxy() );
                        callback.afterCompletion( status );
                    }
                }
            }.execute();
        }
        catch( Exception e )
        {
            // Ignore
        }

    }

    EntityState getCachedState( String entityId )
    {
        EntityComposite composite = getCachedEntity( entityId );
        if( composite != null )
        {
            return EntityInstance.getEntityInstance( composite ).entityState();
        }
        else
        {
            return null;
        }
    }

    private EntityComposite getCachedEntity( String entityId )
    {
        EntityStateStore entityStateStore = stateCache.get( entityId );
        return entityStateStore == null ? null : entityStateStore.instance.<EntityComposite>proxy();
    }

    EntityStateStore getEffectiveEntityStateStore( EntityReference identity, EntityType entityType )
    {
        EntityStateStore entityStateStore = stateCache.get( identity.toString() );
        if( entityStateStore != null )
        {
            return entityStateStore;
        }

        if( unitOfWorkStore != null )
        {
            return unitOfWorkStore.getEffectiveEntityStateStore( identity, entityType );
        }
        else
        {
            return null;
        }
    }

    public void checkOpen()
    {
        if( !isOpen() )
        {
            throw new UnitOfWorkException( "Unit of work has been closed" );
        }
    }

    public EntityState getEntityState( EntityReference identity, EntityModel entity )
        throws EntityStoreException
    {
        checkOpen();

        EntityStateStore ess = stateCache.get( identity.toString() );
        if (ess == null || ess.state == null)
        {
            ess.state = ess.store.getEntityState(identity);
            if (!ess.state.hasEntityTypeReference(entity.entityType().reference()))
                ess.state.addEntityTypeReference(entity.entityType().reference());

        }
        return ess.state;
    }

    public boolean isPaused()
    {
        return paused;
    }

    @Override public String toString()
    {
        return "UnitOfWork "+hashCode()+"("+usecase+"): entities:"+stateCache.size();
    }

    public void remove(EntityReference entityReference)
    {
        stateCache.remove(entityReference.identity());
    }

    abstract class ForEachEntity
    {
        public void execute() throws Exception
        {
            for( EntityStateStore entry : stateCache.values() )
            {
                execute( entry.instance );
            }
        }

        protected abstract void execute( EntityInstance instance ) throws Exception;
    }

    private static class StoreCompletion
    {
        final List<EntityState> newState;
        final List<EntityState> updatedState;
        final List<EntityReference> removedState;

        private StoreCompletion()
        {
            this.newState = new ArrayList<EntityState>();
            this.updatedState = new ArrayList<EntityState>();
            this.removedState = new ArrayList<EntityReference>();
        }

        public List<EntityState> getNewState()
        {
            return newState;
        }

        public List<EntityState> getUpdatedState()
        {
            return updatedState;
        }

        public List<EntityReference> getRemovedState()
        {
            return removedState;
        }
    }
}
