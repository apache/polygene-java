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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.association.ListAssociation;
import org.qi4j.association.ManyAssociation;
import org.qi4j.association.SetAssociation;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.Identity;
import org.qi4j.entity.IdentityGenerator;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.UnitOfWorkException;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.property.PropertyInfo;
import org.qi4j.queryobsolete.Query;
import org.qi4j.queryobsolete.QueryBuilderFactory;
import org.qi4j.queryobsolete.QueryBuilderFactoryImpl;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.EntityCompositeInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.association.AssociationBinding;
import org.qi4j.spi.association.AssociationInstance;
import org.qi4j.spi.association.ListAssociationInstance;
import org.qi4j.spi.association.SetAssociationInstance;
import org.qi4j.spi.composite.AssociationModel;
import org.qi4j.spi.composite.AssociationResolution;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.StoreException;
import org.qi4j.spi.property.ImmutablePropertyInstance;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyInstance;
import org.qi4j.spi.property.PropertyModel;

public final class UnitOfWorkInstance
    implements UnitOfWork
{
    private HashMap<Class<? extends EntityComposite>, Map<String, EntityComposite>> cache;

    private boolean open;
    private ModuleInstance moduleInstance;
    StateServices stateServices;

    public UnitOfWorkInstance( ModuleInstance moduleInstance, StateServices stateServices )
    {
        this.moduleInstance = moduleInstance;
        this.stateServices = stateServices;
        this.open = true;
        cache = new HashMap<Class<? extends EntityComposite>, Map<String, EntityComposite>>();
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
//                throw new UnitOfWorkException("No store for composite type "+compositeType.getName());

        CompositeBuilder<T> builder = new EntityCompositeBuilderFactory( moduleInstance, this, store ).newCompositeBuilder( compositeType );
        if( identity != null )
        {
            builder.propertiesFor( Identity.class ).identity().set( identity );
        }
        return builder;
    }

    public void remove( EntityComposite entity )
    {
        checkOpen();

        EntityCompositeInstance compositeInstance = EntityCompositeInstance.getEntityCompositeInstance( entity );
        compositeInstance.getState().remove();
    }

    public <T extends EntityComposite> T find( String identity, Class<T> compositeType )
        throws EntityCompositeNotFoundException
    {
        checkOpen();

        // TODO: Argument check.

        try
        {
            EntityComposite entity = getCachedEntity( identity, compositeType );
            if( entity == null )
            {   // Not yet in cache

                // Get the state from the store
                EntityStore store = stateServices.getEntityStore( compositeType );
                CompositeContext compositeContext = moduleInstance.getModuleContext().getCompositeContext( compositeType );
                CompositeBinding compositeBinding = compositeContext.getCompositeBinding();
                EntityState state = null;
                try
                {
                    state = store.getEntityState( this, identity, compositeBinding );
                }
                catch( EntityNotFoundException e )
                {
                    throw new EntityCompositeNotFoundException( "Entity does not exist", identity, compositeType );
                }

                // Create entity instance
                entity = (EntityComposite) compositeContext.newEntityCompositeInstance( moduleInstance, this, store, identity ).getProxy();
                EntityCompositeInstance compositeInstance = EntityCompositeInstance.getEntityCompositeInstance( entity );
                compositeContext.newEntityMixins( moduleInstance, compositeInstance, state );
                Map<String, EntityComposite> entityCache = getEntityCache( compositeType );
                entityCache.put( identity, entity );
            }
            else
            {
                if( entity.isReference() )
                {
                    // Check that state exists
                    EntityStore store = stateServices.getEntityStore( compositeType );
                    CompositeBinding compositeBinding = moduleInstance.getModuleContext().getCompositeContext( compositeType ).getCompositeBinding();
                    EntityState state = store.getEntityState( this, identity, compositeBinding );
                    EntityCompositeInstance entityCompositeInstance = EntityCompositeInstance.getEntityCompositeInstance( entity );
                    entityCompositeInstance.setState( state );
                }
                else
                {
                    // Check if it has been removed
                    EntityCompositeInstance handler = EntityCompositeInstance.getEntityCompositeInstance( entity );
                    EntityState entityState = handler.getState();
                    if( entityState.getStatus() == EntityStatus.REMOVED )
                    {
                        throw new EntityCompositeNotFoundException( "Entity has been removed", identity, compositeType );
                    }
                }
            }

            return compositeType.cast( entity );
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

        EntityComposite entity = getCachedEntity( identity, compositeType );
        if( entity == null )
        {
            // Create entity instance
            EntityStore store = stateServices.getEntityStore( compositeType );
            CompositeContext compositeContext = moduleInstance.getModuleContext().getCompositeContext( compositeType );
            entity = (EntityComposite) compositeContext.newEntityCompositeInstance( moduleInstance, this, store, identity ).getProxy();
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
                throw new EntityCompositeNotFoundException( "Entity has been removed", identity, compositeType );
            }
        }

        return compositeType.cast( entity );
    }

    public <T> T getReference( T entity ) throws EntityCompositeNotFoundException
    {
        EntityComposite entityComposite = (EntityComposite) entity;
        return (T) getReference( entityComposite.identity().get(), (Class<? extends EntityComposite>) entityComposite.getCompositeType() );
    }

    public void refresh( EntityComposite entity )
        throws UnitOfWorkException
    {
        checkOpen();

        EntityCompositeInstance entityInstance = EntityCompositeInstance.getEntityCompositeInstance( entity );
        if( !entityInstance.isReference() )
        {
            EntityStatus entityStatus = entityInstance.getState().getStatus();
            if( entityStatus == EntityStatus.REMOVED )
            {
                throw new EntityCompositeNotFoundException( "Entity has been removed", entityInstance.getIdentity(), entity.getCompositeType() );
            }
            else if( entityStatus == EntityStatus.NEW )
            {
                return; // Don't try to refresh newly created state
            }

            // Refresh the state
            try
            {
                EntityState state = entityInstance.getStore().getEntityState( this, entity.identity().get(), entityInstance.getContext().getCompositeBinding() );
                entityInstance.setState( state );
                entityInstance.setMixins( null );
            }
            catch( StoreException e )
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

    public CompositeBuilderFactory getCompositeBuilderFactory()
    {
        return moduleInstance.getStructureContext().getCompositeBuilderFactory();
    }

    public ObjectBuilderFactory getObjectBuilderFactory()
    {
        return moduleInstance.getStructureContext().getObjectBuilderFactory();
    }

    public QueryBuilderFactory getQueryBuilderFactory()
    {
        checkOpen();

        return new QueryBuilderFactoryImpl( new QueryableUnitOfWork( this ) );
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
        Map<EntityStore, List<EntityState>> storeCompletions = new HashMap<EntityStore, List<EntityState>>();
        for( Map.Entry<Class<? extends EntityComposite>, Map<String, EntityComposite>> entry : cache.entrySet() )
        {
            EntityStore store = stateServices.getEntityStore( entry.getKey() );
            List<EntityState> storeCompletionList = storeCompletions.get( store );
            if( storeCompletionList == null )
            {
                storeCompletionList = new ArrayList<EntityState>();
                storeCompletions.put( store, storeCompletionList );
            }

            Map<String, EntityComposite> entities = entry.getValue();
            for( EntityComposite entityInstance : entities.values() )
            {
                EntityState state = EntityCompositeInstance.getEntityCompositeInstance( entityInstance ).getState();
                storeCompletionList.add( state );
            }
        }

        // Commit complete lists
        List<StateCommitter> committers = new ArrayList<StateCommitter>();
        for( Map.Entry<EntityStore, List<EntityState>> entityStoreListEntry : storeCompletions.entrySet() )
        {
            EntityStore entityStore = entityStoreListEntry.getKey();
            List<EntityState> stateList = entityStoreListEntry.getValue();

            try
            {
                committers.add( entityStore.prepare( this, stateList ) );
            }
            catch( StoreException e )
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

    public UnitOfWork newUnitOfWork()
    {
        return new UnitOfWorkInstance( moduleInstance, new UnitOfWorkStateServices() );
    }

    void createEntity( EntityComposite instance )
    {
        Class<? extends EntityComposite> compositeType = (Class<? extends EntityComposite>) instance.getCompositeType();
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
        public EntityState newEntityState( String identity, CompositeBinding compositeBinding ) throws StoreException
        {
            Map<Method, Property> properties = new HashMap<Method, Property>();
            Iterable<PropertyBinding> propertyBindings = compositeBinding.getPropertyBindings();
            for( PropertyBinding propertyBinding : propertyBindings )
            {
                PropertyResolution propertyResolution = propertyBinding.getPropertyResolution();
                PropertyModel propertyModel = propertyResolution.getPropertyModel();
                Method accessor = propertyModel.getAccessor();

                Class<?> type = accessor.getReturnType();
                if( ImmutableProperty.class.isAssignableFrom( type ) )
                {
                    properties.put( accessor, new ImmutablePropertyInstance<Object>( propertyBinding ) );
                }
                else
                {
                    properties.put( accessor, new PropertyInstance<Object>( propertyBinding ) );
                }
            }

            Map<Method, AbstractAssociation> associations = new HashMap<Method, AbstractAssociation>();
            Iterable<AssociationBinding> associationBindings = compositeBinding.getAssociationBindings();
            for( AssociationBinding associationBinding : associationBindings )
            {
                AssociationResolution associationResolution = associationBinding.getAssociationResolution();
                AssociationModel associationModel = associationResolution.getAssociationModel();
                Method accessor = associationModel.getAccessor();
                Class<?> type = accessor.getReturnType();
                if( ListAssociation.class.isAssignableFrom( type ) )
                {
                    ListAssociationInstance<Object> listInstance =
                        new ListAssociationInstance<Object>( new ArrayList<Object>(), associationBinding );
                    associations.put( accessor, listInstance );
                }
                else if( ManyAssociation.class.isAssignableFrom( type ) )
                {
                    SetAssociationInstance setInstance = new SetAssociationInstance<Object>( new HashSet<Object>(), associationBinding );
                    associations.put( accessor, setInstance );
                }
                else
                {
                    AssociationInstance<Object> instance = new AssociationInstance<Object>( associationBinding, null );
                    associations.put( accessor, instance );
                }
            }

            return new UnitOfWorkEntityState( identity, properties, associations );
        }

        public EntityState getEntityState( UnitOfWork unitOfWork, String identity, CompositeBinding compositeBinding ) throws StoreException
        {
            Class<? extends EntityComposite> entityType = (Class<? extends EntityComposite>) compositeBinding.getCompositeResolution().getCompositeModel().getCompositeType();
            EntityComposite parentEntity = getReference( identity, entityType );
            UnitOfWorkEntityState unitOfWorkEntityState = new UnitOfWorkEntityState( identity, parentEntity );
            return unitOfWorkEntityState;
        }

        public StateCommitter prepare( UnitOfWork unitOfWork, Iterable<EntityState> states ) throws StoreException
        {
            for( EntityState stateUnitOfWork : states )
            {
                UnitOfWorkEntityState uowState = (UnitOfWorkEntityState) stateUnitOfWork;
                Collection<Property> properties = uowState.getProperties().values();
                for( Property property : properties )
                {
                    // If property in nested unitOfWork has been updated then copy value to original
                    if( property instanceof UnitOfWorkPropertyInstance )
                    {
                        UnitOfWorkPropertyInstance propertyInstance = (UnitOfWorkPropertyInstance) property;
                        if( propertyInstance.isUpdated() )
                        {
                            Property original = (Property) propertyInstance.getPropertyInfo();
                            original.set( propertyInstance.get() );
                        }
                    }
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

    private class UnitOfWorkEntityState
        implements EntityState
    {
        EntityStatus status;
        EntityComposite parentEntity;
        Map<Method, Property> properties = new HashMap<Method, Property>();
        Map<Method, AbstractAssociation> associations = new HashMap<Method, AbstractAssociation>();
        private String identity;

        private UnitOfWorkEntityState( String identity, Map<Method, Property> properties, Map<Method, AbstractAssociation> associations )
        {
            this.identity = identity;
            this.properties = properties;
            this.associations = associations;
        }

        public UnitOfWorkEntityState( String identity, EntityComposite parentEntity )
        {
            this.identity = identity;
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

        public void remove()
        {
            status = EntityStatus.REMOVED;
        }

        public EntityStatus getStatus()
        {
            return status;
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
                        property = new UnitOfWorkPropertyInstance( original, original.get() );
                    }
                    properties.put( propertyMethod, property );
                }
                return property;
            }
            catch( StoreException e )
            {
                // Could not load state for this entity
                throw new UnitOfWorkException( e );
            }
        }

        public AbstractAssociation getAssociation( Method associationMethod )
        {
            try
            {
                AbstractAssociation association = associations.get( associationMethod );
                if( association == null )
                {
                    EntityCompositeInstance compositeInstance = EntityCompositeInstance.getEntityCompositeInstance( parentEntity );
                    EntityState state = compositeInstance.loadState();
                    AbstractAssociation original = state.getAssociation( associationMethod );
                    if( original instanceof ListAssociation )
                    {
                        List associationList = new ArrayList();
                        ListAssociation originalList = (ListAssociation) original;
                        for( Object entity : originalList )
                        {
                            associationList.add( entity );
                        }
                        association = new ListAssociationInstance( associationList, original );
                    }
                    else if( original instanceof SetAssociation )
                    {
                        Set associationSet = new HashSet();
                        SetAssociation originalSet = (SetAssociation) original;
                        for( Object entity : originalSet )
                        {
                            associationSet.add( entity );
                        }
                        association = new SetAssociationInstance( associationSet, original );
                    }
                    associations.put( associationMethod, association );
                }
                return association;
            }
            catch( StoreException e )
            {
                // Could not load state for this entity
                throw new UnitOfWorkException( e );
            }
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

    private class UnitOfWorkPropertyInstance
        extends PropertyInstance
    {
        boolean updated = false;

        private UnitOfWorkPropertyInstance( PropertyInfo aPropertyInfo, Object aValue )
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

}
