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
import org.qi4j.api.composite.CompositeBuilderFactory;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.EntityCompositeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.StateChangeListener;
import org.qi4j.api.unitofwork.StateChangeVoter;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import static org.qi4j.api.unitofwork.UnitOfWorkCallback.UnitOfWorkStatus.COMPLETED;
import static org.qi4j.api.unitofwork.UnitOfWorkCallback.UnitOfWorkStatus.DISCARDED;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.StateUsage;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.query.QueryBuilderFactoryImpl;
import org.qi4j.runtime.structure.EntitiesInstance;
import org.qi4j.runtime.structure.EntitiesModel;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.entity.ConcurrentEntityStateModificationException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.UnknownEntityTypeException;
import org.qi4j.spi.entity.helpers.DefaultEntityState;

public final class UnitOfWorkInstance
    implements UnitOfWork
{
    public static final ThreadLocal<Stack<UnitOfWorkInstance>> current;

    private final HashMap<Class<? extends EntityComposite>, Map<String, EntityComposite>> cache;

    private boolean open;

    private boolean paused;

    private final ModuleInstance moduleInstance;

    /**
     * Lazy query builder factory.
     */
    private QueryBuilderFactory queryBuilderFactory;

    private Usecase usecase;

    private List<UnitOfWorkCallback> callbacks;
    private UnitOfWorkStore unitOfWorkStore;
    private List<StateChangeListener> stateChangeListeners;
    private List<StateChangeVoter> stateChangeVoters;
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

    public UnitOfWorkInstance( ModuleInstance moduleInstance, Usecase usecase )
    {
        this.moduleInstance = moduleInstance;
        this.open = true;
        cache = new HashMap<Class<? extends EntityComposite>, Map<String, EntityComposite>>();
        current.get().push( this );
        paused = false;
        this.usecase = usecase;
    }

    // Nested unit of work
    public UnitOfWorkInstance( ModuleInstance moduleInstance, Usecase nestedUsecase, UnitOfWorkStore unitOfWorkStore )
    {
        this( moduleInstance, nestedUsecase );
        this.unitOfWorkStore = unitOfWorkStore;
    }

    public <T> T newEntity( Class<T> compositeType )
        throws NoSuchEntityException
    {
        return newEntityBuilder( compositeType ).newInstance();
    }

    public <T> T newEntity( String identity, Class<T> compositeType )
        throws NoSuchEntityException
    {
        return newEntityBuilder( identity, compositeType ).newInstance();
    }

    public <T> EntityBuilder<T> newEntityBuilder( Class<T> mixinType )
        throws NoSuchEntityException
    {
        return newEntityBuilder( null, mixinType );
    }

    public <T> EntityBuilder<T> newEntityBuilder( String identity, Class<T> mixinType )
        throws NoSuchEntityException
    {
        checkOpen();

        ModuleInstance realModuleInstance = moduleInstance.findModuleForEntity( mixinType );
        if( realModuleInstance == null )
        {
            throw new EntityCompositeNotFoundException( mixinType.getName() );
        }

        EntityBuilder<T> builder = realModuleInstance.entities().newEntityBuilder( mixinType, this, unitOfWorkStore );
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
        throws EntityCompositeNotFoundException, NoSuchEntityException
    {
        checkOpen();

        final ModuleInstance realModule = moduleInstance.findModuleForEntity( mixinType );
        if( realModule == null )
        {
            throw new EntityCompositeNotFoundException( mixinType.getName() );
        }
        EntitiesInstance entitiesInstance = realModule.entities();
        EntitiesModel entitiesModel = entitiesInstance.model();
        EntityModel entityModel = entitiesModel.getEntityModelFor( mixinType );

        // TODO: Argument check.

        try
        {
            EntityComposite entity = getCachedEntity( identity, mixinType );
            if( entity == null )
            {   // Not yet in cache
                EntityInstance entityInstance = realModule.entities().loadEntityInstance( identity, entityModel, this, unitOfWorkStore );
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
                        throw new NoSuchEntityException( identity, entityModel.type().getName() );
                    }
                }
            }

            return mixinType.cast( entity );
        }
        catch( EntityNotFoundException e )
        {
            throw new NoSuchEntityException( identity, entityModel.type().getName() );
        }
    }

    public <T> T getReference( String identity, Class<T> mixinType )
        throws EntityCompositeNotFoundException, NoSuchEntityException
    {
        checkOpen();

        ModuleInstance entityModuleInstance = this.moduleInstance.findModuleForEntity( mixinType );
        if( entityModuleInstance == null )
        {
            throw new EntityCompositeNotFoundException( mixinType.getName());
        }

        EntityModel entityModel = entityModuleInstance.entities().model().getEntityModelFor( mixinType );

        EntityComposite entity = getCachedEntity( identity, entityModel.type() );
        if( entity == null )
        {
            // Create entity moduleInstance
            EntityInstance compositeInstance = entityModuleInstance.entities().getEntityInstance( identity, entityModel, this, unitOfWorkStore );
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
                throw new NoSuchEntityException( identity, entityModel.type().getName() );
            }
        }

        return mixinType.cast( entity );
    }

    public <T> T dereference( T entity )
        throws EntityCompositeNotFoundException
    {
        EntityComposite entityComposite = (EntityComposite) entity;
        EntityInstance compositeInstance = EntityInstance.getEntityInstance( entityComposite );
        String id = compositeInstance.qualifiedIdentity().identity();
        Class<? extends EntityComposite> type = compositeInstance.type();
        return (T) getReference( id, type );
    }

    public void refresh( Object entity )
        throws UnitOfWorkException, NoSuchEntityException
    {
        checkOpen();

        EntityComposite entityComposite = (EntityComposite) entity;
        EntityInstance entityInstance = EntityInstance.getEntityInstance( entityComposite );
        if( !entityInstance.isReference() )
        {
            EntityStatus entityStatus = entityInstance.status();
            if( entityStatus == EntityStatus.REMOVED )
            {
                throw new NoSuchEntityException( entityInstance.qualifiedIdentity().identity(), entityInstance.type().getName() );
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
                throw new NoSuchEntityException( entityInstance.qualifiedIdentity().identity(), entityInstance.type().getName() );
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
                try
                {
                    refresh( entity );
                }
                catch( NoSuchEntityException e )
                {
                    // Ignore
                }
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

    public UnitOfWorkFactory unitOfWorkFactory()
    {
        return moduleInstance.unitOfWorkFactory();
    }

    public Usecase usecase()
    {
        return usecase;
    }

    public MetaInfo metaInfo()
    {
        if (metaInfo == null)
            metaInfo = new MetaInfo();
        return metaInfo;
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
            ServiceFinder finder = moduleInstance.serviceFinder();
            queryBuilderFactory = new QueryBuilderFactoryImpl( this, finder );
        }
        return queryBuilderFactory;
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

    private void complete( boolean completeAndContinue )
        throws UnitOfWorkCompletionException
    {
        checkOpen();

        // Copy list so that it cannot be modified during completion
        List<UnitOfWorkCallback> currentCallbacks = callbacks == null ? null : new ArrayList<UnitOfWorkCallback>( callbacks );

        // Check callbacks
        notifyBeforeCompletion( currentCallbacks );

        Map<EntityStore, StoreCompletion> storeCompletion = createCompleteLists();

        List<StateCommitter> committers = commitCompleteLists( storeCompletion );

        // Commit all changes
        for( StateCommitter committer : committers )
        {
            committer.commit();
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
        cache.clear();

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

    public ModuleInstance module()
    {
        checkOpen();

        return moduleInstance;
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
        if (callbacks != null )
            callbacks.remove( callback );
    }

    public void addStateChangeVoter( StateChangeVoter voter )
    {
        if (stateChangeVoters == null)
            stateChangeVoters = new ArrayList();
        stateChangeVoters.add( voter );
    }

    public void removeStateChangeVoter( StateChangeVoter voter )
    {
        if (stateChangeVoters != null)
            stateChangeVoters.remove( voter );
    }

    public Iterable<StateChangeVoter> stateChangeVoters()
    {
        return stateChangeVoters;
    }

    public void addStateChangeListener( StateChangeListener listener )
    {
        if (stateChangeListeners == null)
            stateChangeListeners = new ArrayList();
        stateChangeListeners.add( listener );
    }

    public void removeStateChangeListener( StateChangeListener listener )
    {
        if (stateChangeListeners != null)
            stateChangeListeners.remove( listener );
    }

    void createEntity( EntityComposite instance )
    {
        Class<? extends EntityComposite> compositeType = (Class<? extends EntityComposite>) instance.type();
        Map<String, EntityComposite> entityCache = getEntityCache( compositeType );
        entityCache.put( instance.identity().get(), instance );
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
                    Collection<QualifiedIdentity> modifiedEntityIdentities = mee.modifiedEntities();
                    Collection<EntityComposite> modifiedEntities = new ArrayList<EntityComposite>();
                    for( QualifiedIdentity modifiedEntityIdentity : modifiedEntityIdentities )
                    {
                        modifiedEntities.add( getCachedEntity( modifiedEntityIdentity ) );
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
                    EntityState entityState = instance.entityState();
                    if( entityState != null )
                    {
                        storeCompletionList.getUpdatedState().add( entityState );
                    }
                }
                else if( instance.status() == EntityStatus.NEW )
                {
                    storeCompletionList.getNewState().add( instance.entityState() );
                }
                if( instance.status() == EntityStatus.REMOVED )
                {
                    storeCompletionList.getRemovedState().add( instance.qualifiedIdentity() );
                }
            }
        }
        return storeCompletion;
    }

    private void continueWithState()
    {
        // Update state status for continue
        for( Map.Entry<Class<? extends EntityComposite>, Map<String, EntityComposite>> entry : cache.entrySet() )
        {
            Map<String, EntityComposite> entities = entry.getValue();
            Iterator<EntityComposite> entityIterator = entities.values().iterator();
            while( entityIterator.hasNext() )
            {
                EntityComposite entityInstance = entityIterator.next();
                EntityInstance instance = EntityInstance.getEntityInstance( entityInstance );

                EntityStore store = instance.store();

                if( instance.status() == EntityStatus.NEW )
                {
                    instance.entityState().markAsLoaded();
                }
                if( instance.status() == EntityStatus.REMOVED )
                {
                    entityIterator.remove();
                }
            }
        }
    }

    private Map<String, EntityComposite> getEntityCache( Class<? extends EntityComposite> compositeType )
    {
        Map<String, EntityComposite> entityCache = cache.get( compositeType );

        if( entityCache == null )
        {
            entityCache = new HashMap<String, EntityComposite>();
            cache.put( compositeType, entityCache );
        }

        return entityCache;
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
                    if (instance.proxy() instanceof UnitOfWorkCallback)
                    {
                        UnitOfWorkCallback callback = UnitOfWorkCallback.class.cast(instance.proxy());
                        callback.beforeCompletion();
                    }
                }
            }.execute();
        }
        catch( UnitOfWorkCompletionException e )
        {
            throw e;
        } catch (Exception e)
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
                    if (instance.proxy() instanceof UnitOfWorkCallback)
                    {
                        UnitOfWorkCallback callback = UnitOfWorkCallback.class.cast(instance.proxy());
                        callback.afterCompletion(status);
                    }
                }
            }.execute();
        }
        catch (Exception e)
        {
            // Ignore
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

    private EntityComposite getCachedEntity( QualifiedIdentity entityId )
    {
        String type = entityId.type();
        Class compositeType = moduleInstance.findClassForName( type );
        Map<String, EntityComposite> entityCache = cache.get( compositeType );
        if( entityCache == null )
        {
            return null;
        }

        EntityComposite composite = entityCache.get( entityId.identity() );
        return composite;
    }

    private void checkOpen()
    {
        if( !isOpen() )
        {
            throw new UnitOfWorkException( "Unit of work has been closed" );
        }
    }

    public Iterable<StateChangeListener> stateChangeListeners()
    {
        return stateChangeListeners;
    }

    public UnitOfWorkStore newEntityStore()
    {
        return new UnitOfWorkStore();
    }

    abstract class ForEachEntity
    {
        public void execute() throws Exception
        {
            for( Map.Entry<Class<? extends EntityComposite>, Map<String, EntityComposite>> entry : cache.entrySet() )
            {
                Map<String, EntityComposite> entities = entry.getValue();
                for( EntityComposite entityComposite : entities.values() )
                {
                    EntityComposite entityInstance = entityComposite;
                    EntityInstance instance = EntityInstance.getEntityInstance( entityInstance );
                    execute( instance );
                }
            }
        }

        protected abstract void execute(EntityInstance instance) throws Exception;
    }

    private class UnitOfWorkStore
        implements EntityStore
    {
        final Map<String, EntityType> entityTypes = new HashMap<String, EntityType>();

        public void registerEntityType( EntityType entityType )
        {
            entityTypes.put( entityType.type(), entityType );
        }

        public EntityType getEntityType( String aEntityType )
        {
            return entityTypes.get( aEntityType );
        }

        public EntityState newEntityState( QualifiedIdentity identity ) throws EntityStoreException
        {
            EntityType entityType = entityTypes.get( identity.type() );
            if( entityType == null )
            {
                throw new UnknownEntityTypeException( identity.type() );
            }

            UnitOfWorkEntityState entityState = new UnitOfWorkEntityState( 0, System.currentTimeMillis(), identity, EntityStatus.NEW, entityType,
                                                                           new HashMap<String, Object>(), new HashMap<String, QualifiedIdentity>(), new HashMap<String, Collection<QualifiedIdentity>>(), null );
            return entityState;
        }

        public EntityState getEntityState( QualifiedIdentity identity ) throws EntityStoreException
        {
            EntityType entityType = entityTypes.get( identity.type() );
            if( entityType == null )
            {
                throw new UnknownEntityTypeException( identity.type() );
            }

            EntityState parentState = getCachedState( identity );
            if( parentState == null )
            {
                // Force load into parent
                try
                {
                    Class<? extends EntityComposite> entityClass = (Class<? extends EntityComposite>) moduleInstance.classLoader().loadClass( identity.type() );
                    parentState = EntityInstance.getEntityInstance( find( identity.identity(), entityClass ) ).entityState();
                }
                catch( ClassNotFoundException e )
                {
                    throw new EntityStoreException( e );
                }
            }

            UnitOfWorkEntityState unitOfWorkEntityState = new UnitOfWorkEntityState( parentState.version(),
                                                                                     parentState.lastModified(),
                                                                                     identity,
                                                                                     EntityStatus.LOADED,
                                                                                     entityType,
                                                                                     new HashMap<String, Object>(),
                                                                                     new HashMap<String, QualifiedIdentity>(),
                                                                                     new HashMap<String, Collection<QualifiedIdentity>>(),
                                                                                     parentState );
            return unitOfWorkEntityState;
        }

        public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates ) throws EntityStoreException
        {
            // Create new entity and transfer state
            for( EntityState newState : newStates )
            {
                UnitOfWorkEntityState uowState = (UnitOfWorkEntityState) newState;
                Class<? extends EntityComposite> type = null;
                try
                {
                    type = (Class<? extends EntityComposite>) moduleInstance.classLoader().loadClass( uowState.qualifiedIdentity().type() );
                }
                catch( ClassNotFoundException e )
                {
                    throw new EntityStoreException( e );
                }
                EntityComposite entityInstance = newEntityBuilder( uowState.qualifiedIdentity().identity(), type ).newInstance();

                EntityState parentState = EntityInstance.getEntityInstance( entityInstance ).entityState();
                Iterable<String> propertyNames = uowState.propertyNames();
                for( String propertyName : propertyNames )
                {
                    parentState.setProperty( propertyName, uowState.getProperty( propertyName ) );
                }
                Iterable<String> associationNames = uowState.associationNames();
                for( String associationName : associationNames )
                {
                    parentState.setAssociation( associationName, uowState.getAssociation( associationName ) );
                }
                Iterable<String> manyAssociationNames = uowState.manyAssociationNames();
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
                    Class<? extends EntityComposite> type = null;
                    try
                    {
                        type = (Class<? extends EntityComposite>) moduleInstance.classLoader().loadClass( uowState.qualifiedIdentity().type() );
                    }
                    catch( ClassNotFoundException e )
                    {
                        throw new EntityStoreException( e );
                    }
                    EntityComposite instance = find( uowState.qualifiedIdentity().identity(), type );
                    EntityInstance entityInstance = EntityInstance.getEntityInstance( instance );

                    EntityState parentState = uowState.getParentState();
                    Iterable<String> propertyNames = uowState.propertyNames();
                    for( String propertyName : propertyNames )
                    {
                        Object value = uowState.getProperty( propertyName );
                        parentState.setProperty( propertyName, value );
                    }
                    Iterable<String> associationNames = uowState.associationNames();
                    for( String associationName : associationNames )
                    {
                        QualifiedIdentity value = uowState.getAssociation( associationName );
                        parentState.setAssociation( associationName, value );
                    }
                    Iterable<String> manyAssociationNames = uowState.manyAssociationNames();
                    for( String manyAssociationName : manyAssociationNames )
                    {
                        Collection<QualifiedIdentity> collection = parentState.getManyAssociation( manyAssociationName );
                        Collection<QualifiedIdentity> newCollection = uowState.getManyAssociation( manyAssociationName );

                        // TODO This can be soooo much more optimized by comparing the collections and matching them
                        // up by doing individual add/removes
                        collection.clear();
                        collection.addAll( newCollection );
                    }

                    entityInstance.state().refresh( parentState );
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
        extends DefaultEntityState
    {
        private final EntityState parentState;

        private UnitOfWorkEntityState( long entityVersion, long lastModified,
                                       QualifiedIdentity identity,
                                       EntityStatus status,
                                       EntityType entityType,
                                       Map<String, Object> properties,
                                       Map<String, QualifiedIdentity> associations,
                                       Map<String, Collection<QualifiedIdentity>> manyAssociations,
                                       EntityState parentState )
        {
            super( entityVersion, lastModified, identity, status, entityType, properties, associations, manyAssociations );
            this.parentState = parentState;
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
        final List<EntityState> newState;
        final List<EntityState> updatedState;
        final List<QualifiedIdentity> removedState;

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
