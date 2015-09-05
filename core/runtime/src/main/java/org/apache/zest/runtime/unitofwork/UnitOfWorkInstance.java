/*
 * Copyright (c) 2007-2013, Niclas Hedhman. All Rights Reserved.
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
package org.apache.zest.runtime.unitofwork;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.metrics.MetricsCounter;
import org.apache.zest.api.metrics.MetricsCounterFactory;
import org.apache.zest.api.metrics.MetricsProvider;
import org.apache.zest.api.metrics.MetricsTimer;
import org.apache.zest.api.metrics.MetricsTimerFactory;
import org.apache.zest.api.unitofwork.ConcurrentEntityModificationException;
import org.apache.zest.api.unitofwork.EntityTypeNotFoundException;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWorkCallback;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.unitofwork.UnitOfWorkException;
import org.apache.zest.api.unitofwork.UnitOfWorkOptions;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.runtime.entity.EntityInstance;
import org.apache.zest.runtime.entity.EntityModel;
import org.apache.zest.runtime.structure.ModuleUnitOfWork;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entity.EntityStatus;
import org.apache.zest.spi.entitystore.ConcurrentEntityStateModificationException;
import org.apache.zest.spi.entitystore.EntityNotFoundException;
import org.apache.zest.spi.entitystore.EntityStore;
import org.apache.zest.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.zest.spi.entitystore.StateCommitter;
import org.apache.zest.spi.metrics.DefaultMetric;
import org.apache.zest.spi.module.ModelModule;
import org.apache.zest.spi.module.ModuleSpi;

import static org.apache.zest.api.unitofwork.UnitOfWorkCallback.UnitOfWorkStatus.COMPLETED;
import static org.apache.zest.api.unitofwork.UnitOfWorkCallback.UnitOfWorkStatus.DISCARDED;

public final class UnitOfWorkInstance
{
    private static final ThreadLocal<Stack<UnitOfWorkInstance>> current = new ThreadLocal<Stack<UnitOfWorkInstance>>()
    {
        @Override
        protected Stack<UnitOfWorkInstance> initialValue()
        {
            return new Stack<>();
        }
    };
    private MetricsTimer.Context metricsTimer;

    public static Stack<UnitOfWorkInstance> getCurrent()
    {
        return current.get();
    }

    private long currentTime;
    private MetricsProvider metrics;
    final HashMap<EntityReference, EntityInstance> instanceCache;
    final HashMap<EntityStore, EntityStoreUnitOfWork> storeUnitOfWork;

    private boolean open;

    private boolean paused;

    /**
     * Lazy query builder factory.
     */
    private Usecase usecase;

    private MetaInfo metaInfo;

    private List<UnitOfWorkCallback> callbacks;

    public UnitOfWorkInstance( Usecase usecase, long currentTime, MetricsProvider metrics )
    {
        this.currentTime = currentTime;
        this.open = true;
        instanceCache = new HashMap<>();
        storeUnitOfWork = new HashMap<>();
        getCurrent().push( this );
        paused = false;
        this.usecase = usecase;
        startCapture( metrics );
    }

    public long currentTime()
    {
        return currentTime;
    }

    public EntityStoreUnitOfWork getEntityStoreUnitOfWork( EntityStore store )
    {
        EntityStoreUnitOfWork uow = storeUnitOfWork.get( store );
        if( uow == null )
        {
            uow = store.newUnitOfWork( usecase, currentTime );
            storeUnitOfWork.put( store, uow );
        }
        return uow;
    }

    public <T> T get( EntityReference identity,
                      ModuleUnitOfWork uow,
                      Iterable<ModelModule<EntityDescriptor>> potentialModels,
                      Class<T> mixinType
    )
        throws EntityTypeNotFoundException, NoSuchEntityException
    {
        checkOpen();

        EntityInstance entityInstance = instanceCache.get( identity );
        if( entityInstance == null )
        {   // Not yet in cache

            // Check if this is a root UoW, or if no parent UoW knows about this entity
            EntityState entityState = null;
            EntityModel model = null;
            ModuleSpi module = null;
            // Figure out what EntityStore to use
            for( ModelModule<EntityDescriptor> potentialModel : potentialModels )
            {
                EntityStore store = potentialModel.module().entityStore();
                EntityStoreUnitOfWork storeUow = getEntityStoreUnitOfWork( store );
                try
                {
                    entityState = storeUow.entityStateOf( potentialModel.module(), identity );
                }
                catch( EntityNotFoundException e )
                {
                    continue;
                }

                // Get the selected model
                model = (EntityModel) entityState.entityDescriptor();
                module = potentialModel.module();
            }

            // Check if model was found
            if( model == null )
            {
                // Check if state was found
                if( entityState == null )
                {
                    throw new NoSuchEntityException( identity, mixinType, usecase );
                }
                else
                {
                    throw new EntityTypeNotFoundException( mixinType.getName(),
                                                           module.name(),
                                                           module.findVisibleEntityTypes()
                                                               .map( ModelModule.toStringFunction )
                    );
                }
            }

            // Create instance
            entityInstance = new EntityInstance( uow, module, model, entityState );

            instanceCache.put( identity, entityInstance );
        }
        else
        {
            // Check if it has been removed
            if( entityInstance.status() == EntityStatus.REMOVED )
            {
                throw new NoSuchEntityException( identity, mixinType, usecase );
            }
        }

        return entityInstance.proxy();
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
            getCurrent().pop();

            UnitOfWorkOptions unitOfWorkOptions = metaInfo().get( UnitOfWorkOptions.class );
            if( unitOfWorkOptions == null )
            {
                unitOfWorkOptions = usecase().metaInfo( UnitOfWorkOptions.class );
            }

            if( unitOfWorkOptions != null )
            {
                if( unitOfWorkOptions.isPruneOnPause() )
                {
                    List<EntityReference> prunedInstances = null;
                    for( EntityInstance entityInstance : instanceCache.values() )
                    {
                        if( entityInstance.status() == EntityStatus.LOADED )
                        {
                            if( prunedInstances == null )
                            {
                                prunedInstances = new ArrayList<>();
                            }
                            prunedInstances.add( entityInstance.identity() );
                        }
                    }
                    if( prunedInstances != null )
                    {
                        prunedInstances.forEach( instanceCache::remove );
                    }
                }
            }
        }
        else
        {
            throw new UnitOfWorkException( "Unit of work is not active" );
        }
    }

    public void resume()
    {
        if( paused )
        {
            paused = false;
            getCurrent().push( this );
        }
        else
        {
            throw new UnitOfWorkException( "Unit of work has not been paused" );
        }
    }

    public void complete()
        throws UnitOfWorkCompletionException
    {
        checkOpen();

        // Copy list so that it cannot be modified during completion
        List<UnitOfWorkCallback> currentCallbacks = callbacks == null ? null : new ArrayList<>( callbacks );

        // Commit state to EntityStores
        List<StateCommitter> committers = applyChanges();

        // Check callbacks
        notifyBeforeCompletion( currentCallbacks );

        // Commit all changes
        committers.forEach( StateCommitter::commit );

        close();

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
        List<UnitOfWorkCallback> currentCallbacks = callbacks == null ? null : new ArrayList<>( callbacks );

        // Call callbacks
        notifyAfterCompletion( currentCallbacks, DISCARDED );
        storeUnitOfWork.values().forEach( EntityStoreUnitOfWork::discard );
        callbacks = currentCallbacks;
    }

    private void close()
    {
        checkOpen();

        if( !isPaused() )
        {
            getCurrent().pop();
        }
        endCapture();
        open = false;
    }

    public boolean isOpen()
    {
        return open;
    }

    public void addUnitOfWorkCallback( UnitOfWorkCallback callback )
    {
        if( callbacks == null )
        {
            callbacks = new ArrayList<>();
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

    public void addEntity( EntityInstance instance )
    {
        instanceCache.put( instance.identity(), instance );
    }

    private List<StateCommitter> applyChanges()
        throws UnitOfWorkCompletionException
    {
        List<StateCommitter> committers = new ArrayList<>();
        for( EntityStoreUnitOfWork entityStoreUnitOfWork : storeUnitOfWork.values() )
        {
            try
            {
                StateCommitter committer = entityStoreUnitOfWork.applyChanges();
                committers.add( committer );
            }
            catch( Exception e )
            {
                // Cancel all previously prepared stores
                committers.forEach( StateCommitter::cancel );

                if( e instanceof ConcurrentEntityStateModificationException )
                {
                    // If we cancelled due to concurrent modification, then create the proper exception for it!
                    ConcurrentEntityStateModificationException mee = (ConcurrentEntityStateModificationException) e;
                    Collection<EntityReference> modifiedEntityIdentities = mee.modifiedEntities();
                    Collection<EntityComposite> modifiedEntities = new ArrayList<>();
                    for( EntityReference modifiedEntityIdentity : modifiedEntityIdentities )
                    {
                        instanceCache.values().stream()
                            .filter( instance -> instance.identity().equals( modifiedEntityIdentity ) )
                            .forEach( instance -> modifiedEntities.add( instance.<EntityComposite>proxy() ) );
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
            for( EntityInstance instance : instanceCache.values() )
            {
                boolean isCallback = instance.proxy() instanceof UnitOfWorkCallback;
                boolean isNotRemoved = !instance.status().equals( EntityStatus.REMOVED );
                if( isCallback && isNotRemoved )
                {
                    UnitOfWorkCallback callback = UnitOfWorkCallback.class.cast( instance.proxy() );
                    callback.beforeCompletion();
                }
            }
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

    private void notifyAfterCompletion( List<UnitOfWorkCallback> callbacks,
                                        final UnitOfWorkCallback.UnitOfWorkStatus status
    )
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
            for( EntityInstance instance : instanceCache.values() )
            {
                boolean isCallback = instance.proxy() instanceof UnitOfWorkCallback;
                boolean isNotRemoved = !instance.status().equals( EntityStatus.REMOVED );
                if( isCallback && isNotRemoved )
                {
                    UnitOfWorkCallback callback = UnitOfWorkCallback.class.cast( instance.proxy() );
                    callback.afterCompletion( status );
                }
            }
        }
        catch( Exception e )
        {
            // Ignore
        }
    }

    public void checkOpen()
    {
        if( !isOpen() )
        {
            throw new UnitOfWorkException( "Unit of work has been closed" );
        }
    }

    public boolean isPaused()
    {
        return paused;
    }

    @Override
    public String toString()
    {
        return "UnitOfWork " + hashCode() + "(" + usecase + "): entities:" + instanceCache.size();
    }

    public void remove( EntityReference entityReference )
    {
        instanceCache.remove( entityReference );
    }

    private void incrementCount()
    {
        MetricsCounter counter = getCounter();
        counter.increment();
    }

    private void decrementCount()
    {
        MetricsCounter counter = getCounter();
        counter.decrement();
    }

    private MetricsCounter getCounter()
    {
        if( metrics != null )
        {
            MetricsCounterFactory metricsFactory = metrics.createFactory( MetricsCounterFactory.class );
            return metricsFactory.createCounter( getClass(), "UnitOfWork Counter" );
        }
        return new DefaultMetric();
    }

    private void endCapture()
    {
        decrementCount();
        metricsTimer.stop();
    }

    private void startCapture( MetricsProvider metrics )
    {
        this.metrics = metrics;
        incrementCount();
        startTimer( metrics );
    }

    private void startTimer( MetricsProvider metrics )
    {
        MetricsTimerFactory metricsFactory = metrics.createFactory( MetricsTimerFactory.class );
        String name = "UnitOfWork Timer";
        MetricsTimer timer = metricsFactory.createTimer( getClass(), name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS );
        metricsTimer = timer.start();
    }
}
