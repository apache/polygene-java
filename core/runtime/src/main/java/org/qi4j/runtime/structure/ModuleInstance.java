/*
 * Copyright (c) 2008-2012, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008-2012, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.runtime.structure;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivationEvent;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.api.composite.NoSuchTransientException;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.metrics.MetricsProvider;
import org.qi4j.api.object.NoSuchObjectException;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.NoSuchServiceException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.api.value.NoSuchValueException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.api.value.ValueSerializationException;
import org.qi4j.functional.Function;
import org.qi4j.functional.Function2;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;
import org.qi4j.runtime.activation.ActivationDelegate;
import org.qi4j.runtime.activation.ActivationEventListenerSupport;
import org.qi4j.runtime.composite.TransientBuilderInstance;
import org.qi4j.runtime.composite.TransientModel;
import org.qi4j.runtime.composite.TransientStateInstance;
import org.qi4j.runtime.composite.TransientsModel;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.entity.EntitiesModel;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.object.ObjectsModel;
import org.qi4j.runtime.property.PropertyInstance;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.runtime.query.QueryBuilderFactoryImpl;
import org.qi4j.runtime.service.ImportedServicesInstance;
import org.qi4j.runtime.service.ImportedServicesModel;
import org.qi4j.runtime.service.ServicesInstance;
import org.qi4j.runtime.service.ServicesModel;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.runtime.value.ValueBuilderInstance;
import org.qi4j.runtime.value.ValueBuilderWithPrototype;
import org.qi4j.runtime.value.ValueBuilderWithState;
import org.qi4j.runtime.value.ValueInstance;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.runtime.value.ValueStateModel;
import org.qi4j.runtime.value.ValuesModel;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.metrics.MetricsProviderAdapter;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerialization;

import static org.qi4j.api.util.Classes.*;
import static org.qi4j.functional.Iterables.*;

/**
 * Instance of a Qi4j Module. Contains the various composites for this Module.
 */
public class ModuleInstance
    implements Module, Activation
{

    // Constructor parameters
    private final ModuleModel model;
    private final LayerInstance layer;
    private final TransientsModel transients;
    private final ValuesModel values;
    private final ObjectsModel objects;
    private final EntitiesModel entities;
    private final ServicesInstance services;
    private final ImportedServicesInstance importedServices;
    // Eager instance objects
    private final ActivationDelegate activation;
    private final ActivationEventListenerSupport activationEventSupport;
    private final TypeLookup typeLookup;
    private final QueryBuilderFactory queryBuilderFactory;
    private final ClassLoader classLoader;
    private final EntityFunction entityFunction;
    // Lazy assigned on accessors
    private EntityStore store;
    private IdentityGenerator generator;
    private ValueSerialization valueSerialization;
    private MetricsProvider metrics;

    @SuppressWarnings( "LeakingThisInConstructor" )
    public ModuleInstance( ModuleModel moduleModel, LayerInstance layerInstance, TransientsModel transientsModel,
                           EntitiesModel entitiesModel, ObjectsModel objectsModel, ValuesModel valuesModel,
                           ServicesModel servicesModel, ImportedServicesModel importedServicesModel )
    {
        // Constructor parameters
        model = moduleModel;
        layer = layerInstance;
        transients = transientsModel;
        values = valuesModel;
        objects = objectsModel;
        entities = entitiesModel;
        services = servicesModel.newInstance( this );
        importedServices = importedServicesModel.newInstance( this );

        // Eager instance objects
        activation = new ActivationDelegate( this );
        activationEventSupport = new ActivationEventListenerSupport();
        typeLookup = new TypeLookup( this );
        queryBuilderFactory = new QueryBuilderFactoryImpl( this );
        classLoader = new ModuleClassLoader( this, Thread.currentThread().getContextClassLoader() );
        entityFunction = new EntityFunction( this );

        // Activation
        services.registerActivationEventListener( activationEventSupport );
        importedServices.registerActivationEventListener( activationEventSupport );
    }

    @Override
    public String toString()
    {
        return model.toString();
    }

    // Implementation of Module
    @Override
    public String name()
    {
        return model.name();
    }

    @Override
    public ClassLoader classLoader()
    {
        return classLoader;
    }

    @Override
    public EntityDescriptor entityDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            ModelModule<EntityModel> entityModel = typeLookup.lookupEntityModel( type );
            if( entityModel == null )
            {
                return null;
            }
            return entityModel.model();
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public ObjectDescriptor objectDescriptor( String typeName )
    {
        try
        {
            Class<?> type = classLoader().loadClass( typeName );
            ModelModule<ObjectModel> objectModel = typeLookup.lookupObjectModel( type );
            if( objectModel == null )
            {
                return null;
            }
            return objectModel.model();
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public TransientDescriptor transientDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            ModelModule<TransientModel> transientModel = typeLookup.lookupTransientModel( type );
            if( transientModel == null )
            {
                return null;
            }
            return transientModel.model();
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public ValueDescriptor valueDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            ModelModule<ValueModel> valueModel = typeLookup.lookupValueModel( type );
            if( valueModel == null )
            {
                return null;
            }
            return valueModel.model();
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    // Implementation of MetaInfoHolder
    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return model.metaInfo( infoType );
    }

    // Implementation of ObjectFactory
    @Override
    public <T> T newObject( Class<T> mixinType, Object... uses )
        throws NoSuchObjectException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ModelModule<ObjectModel> modelModule = typeLookup.lookupObjectModel( mixinType );

        if( modelModule == null )
        {
            throw new NoSuchObjectException( mixinType.getName(), name() );
        }

        InjectionContext injectionContext = new InjectionContext( modelModule.module(), UsesInstance.EMPTY_USES.use( uses ) );
        return mixinType.cast( modelModule.model().newInstance( injectionContext ) );
    }

    @Override
    public void injectTo( Object instance, Object... uses )
        throws ConstructionException
    {
        NullArgumentException.validateNotNull( "instance", instance );
        ModelModule<ObjectModel> modelModule = typeLookup.lookupObjectModel( instance.getClass() );

        if( modelModule == null )
        {
            throw new NoSuchObjectException( instance.getClass().getName(), name() );
        }

        InjectionContext injectionContext = new InjectionContext( modelModule.module(), UsesInstance.EMPTY_USES.use( uses ) );
        modelModule.model().inject( injectionContext, instance );
    }

    // Implementation of TransientBuilderFactory
    @Override
    public <T> TransientBuilder<T> newTransientBuilder( Class<T> mixinType )
        throws NoSuchTransientException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ModelModule<TransientModel> modelModule = typeLookup.lookupTransientModel( mixinType );

        if( modelModule == null )
        {
            throw new NoSuchTransientException( mixinType.getName(), name() );
        }

        Map<AccessibleObject, Property<?>> properties = new HashMap<AccessibleObject, Property<?>>();
        for( PropertyModel propertyModel : modelModule.model().state().properties() )
        {
            Property<?> property = new PropertyInstance<Object>( propertyModel.getBuilderInfo(),
                                                                 propertyModel.initialValue( modelModule.module() ) );
            properties.put( propertyModel.accessor(), property );
        }

        TransientStateInstance state = new TransientStateInstance( properties );

        return new TransientBuilderInstance<T>( modelModule, state, UsesInstance.EMPTY_USES );
    }

    @Override
    public <T> T newTransient( final Class<T> mixinType, Object... uses )
        throws NoSuchTransientException, ConstructionException
    {
        return newTransientBuilder( mixinType ).use( uses ).newInstance();
    }

    // Implementation of ValueBuilderFactory
    @Override
    public <T> T newValue( Class<T> mixinType )
        throws NoSuchValueException, ConstructionException
    {
        return newValueBuilder( mixinType ).newInstance();
    }

    @Override
    public <T> ValueBuilder<T> newValueBuilder( Class<T> mixinType )
        throws NoSuchValueException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ModelModule<ValueModel> compositeModelModule = typeLookup.lookupValueModel( mixinType );

        if( compositeModelModule == null )
        {
            throw new NoSuchValueException( mixinType.getName(), name() );
        }

        ValueStateModel.StateResolver stateResolver = new InitialStateResolver( compositeModelModule.module() );
        return new ValueBuilderInstance<T>( compositeModelModule, this, stateResolver );
    }

    @Override
    public <T> ValueBuilder<T> newValueBuilderWithState( Class<T> mixinType,
                                                         Function<PropertyDescriptor, Object> propertyFunction,
                                                         Function<AssociationDescriptor, EntityReference> associationFunction,
                                                         Function<AssociationDescriptor, Iterable<EntityReference>> manyAssociationFunction )
    {
        NullArgumentException.validateNotNull( "propertyFunction", propertyFunction );
        NullArgumentException.validateNotNull( "associationFunction", associationFunction );
        NullArgumentException.validateNotNull( "manyAssociationFunction", manyAssociationFunction );

        ModelModule<ValueModel> compositeModelModule = typeLookup.lookupValueModel( mixinType );

        if( compositeModelModule == null )
        {
            throw new NoSuchValueException( mixinType.getName(), name() );
        }

        ValueStateModel.StateResolver stateResolver = new FunctionStateResolver( propertyFunction, associationFunction, manyAssociationFunction );
        return new ValueBuilderWithState<T>( compositeModelModule, this, stateResolver );
    }

    private static class InitialStateResolver
        implements ValueStateModel.StateResolver
    {

        private final ModuleInstance module;

        public InitialStateResolver( ModuleInstance module )
        {
            this.module = module;
        }

        @Override
        public Object getPropertyState( PropertyDescriptor propertyDescriptor )
        {
            return propertyDescriptor.initialValue( module );
        }

        @Override
        public EntityReference getAssociationState( AssociationDescriptor associationDescriptor )
        {
            return null;
        }

        @Override
        public List<EntityReference> getManyAssociationState( AssociationDescriptor associationDescriptor )
        {
            return new ArrayList<EntityReference>();
        }
    }

    private static class FunctionStateResolver
        implements ValueStateModel.StateResolver
    {

        private final Function<PropertyDescriptor, Object> propertyFunction;
        private final Function<AssociationDescriptor, EntityReference> associationFunction;
        private final Function<AssociationDescriptor, Iterable<EntityReference>> manyAssociationFunction;

        private FunctionStateResolver( Function<PropertyDescriptor, Object> propertyFunction,
                                       Function<AssociationDescriptor, EntityReference> associationFunction,
                                       Function<AssociationDescriptor, Iterable<EntityReference>> manyAssociationFunction )
        {
            this.propertyFunction = propertyFunction;
            this.associationFunction = associationFunction;
            this.manyAssociationFunction = manyAssociationFunction;
        }

        @Override
        public Object getPropertyState( PropertyDescriptor propertyDescriptor )
        {
            return propertyFunction.map( propertyDescriptor );
        }

        @Override
        public EntityReference getAssociationState( AssociationDescriptor associationDescriptor )
        {
            return associationFunction.map( associationDescriptor );
        }

        @Override
        public List<EntityReference> getManyAssociationState( AssociationDescriptor associationDescriptor )
        {
            return toList( manyAssociationFunction.map( associationDescriptor ) );
        }
    }

    @Override
    public <T> ValueBuilder<T> newValueBuilderWithPrototype( T prototype )
    {
        NullArgumentException.validateNotNull( "prototype", prototype );

        ValueInstance valueInstance = ValueInstance.valueInstanceOf( (ValueComposite) prototype );
        Class<Composite> valueType = (Class<Composite>) first( valueInstance.types() );

        ModelModule<ValueModel> modelModule = typeLookup.lookupValueModel( valueType );

        if( modelModule == null )
        {
            throw new NoSuchValueException( valueType.getName(), name() );
        }

        return new ValueBuilderWithPrototype<T>( modelModule, this, prototype );
    }

    @Override
    public <T> T newValueFromSerializedState( Class<T> mixinType, String serializedState )
        throws NoSuchValueException, ConstructionException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ModelModule<ValueModel> modelModule = typeLookup.lookupValueModel( mixinType );

        if( modelModule == null )
        {
            throw new NoSuchValueException( mixinType.getName(), name() );
        }

        try
        {
            return valueSerialization().deserialize( modelModule.model().valueType(), serializedState );
        }
        catch( ValueSerializationException ex )
        {
            throw new ConstructionException( "Could not create value from serialized state", ex );
        }
    }

    // Implementation of UnitOfWorkFactory
    @Override
    public UnitOfWork newUnitOfWork()
    {
        return newUnitOfWork( Usecase.DEFAULT );
    }

    @Override
    public UnitOfWork newUnitOfWork( long currentTime )
    {
        return newUnitOfWork( Usecase.DEFAULT, currentTime );
    }

    @Override
    public UnitOfWork newUnitOfWork( Usecase usecase )
    {
        return newUnitOfWork( usecase == null ? Usecase.DEFAULT : usecase, System.currentTimeMillis() );
    }

    @Override
    public UnitOfWork newUnitOfWork( Usecase usecase, long currentTime )
    {
        UnitOfWorkInstance unitOfWorkInstance = new UnitOfWorkInstance( usecase, currentTime, metricsProvider() );
        return new ModuleUnitOfWork( ModuleInstance.this, unitOfWorkInstance );
    }

    @Override
    public boolean isUnitOfWorkActive()
    {
        Stack<UnitOfWorkInstance> stack = UnitOfWorkInstance.getCurrent();
        return !stack.isEmpty();
    }

    @Override
    public UnitOfWork currentUnitOfWork()
    {
        Stack<UnitOfWorkInstance> stack = UnitOfWorkInstance.getCurrent();
        if( stack.size() == 0 )
        {
            throw new IllegalStateException( "No current UnitOfWork active" );
        }
        return new ModuleUnitOfWork( ModuleInstance.this, stack.peek() );
    }

    @Override
    public UnitOfWork getUnitOfWork( EntityComposite entity )
    {
        EntityInstance instance = EntityInstance.entityInstanceOf( entity );
        return instance.unitOfWork();
    }

    // Implementation of QueryBuilderFactory
    @Override
    public <T> QueryBuilder<T> newQueryBuilder( final Class<T> resultType )
    {
        return queryBuilderFactory.newQueryBuilder( resultType );
    }

    // Implementation of ServiceFinder
    @Override
    public <T> ServiceReference<T> findService( Class<T> serviceType )
    {
        return typeLookup.lookupServiceReference( (Type) serviceType );
    }

    @Override
    public <T> ServiceReference<T> findService( Type serviceType )
    {
        return typeLookup.lookupServiceReference( serviceType );
    }

    @Override
    public <T> Iterable<ServiceReference<T>> findServices( Class<T> serviceType )
    {
        return typeLookup.lookupServiceReferences( (Type) serviceType );
    }

    @Override
    public <T> Iterable<ServiceReference<T>> findServices( Type serviceType )
    {
        return typeLookup.lookupServiceReferences( serviceType );
    }

    // Implementation of Activation
    @Override
    public void activate()
        throws ActivationException
    {
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATING ) );
        activation.activate( model.newActivatorsInstance(), iterable( services, importedServices ) );
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATED ) );
    }

    @Override
    public void passivate()
        throws PassivationException
    {
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATING ) );
        activation.passivate();
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATED ) );
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activationEventSupport.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activationEventSupport.deregisterActivationEventListener( listener );
    }

    // Other methods
    /* package */ ModuleModel model()
    {
        return model;
    }

    public LayerInstance layerInstance()
    {
        return layer;
    }

    public TypeLookup typeLookup()
    {
        return typeLookup;
    }

    public Function2<EntityReference, Type, Object> getEntityFunction()
    {
        return entityFunction;
    }

    private static class EntityFunction
        implements Function2<EntityReference, Type, Object>
    {

        private final UnitOfWorkFactory uowf;

        private EntityFunction( UnitOfWorkFactory uowf )
        {
            this.uowf = uowf;
        }

        @Override
        public Object map( EntityReference entityReference, Type type )
        {
            return uowf.currentUnitOfWork().get( RAW_CLASS.map( type ), entityReference.identity() );
        }
    }

    public EntityStore entityStore()
    {
        synchronized( this )
        {
            if( store == null )
            {
                ServiceReference<EntityStore> service = findService( EntityStore.class );
                if( service == null )
                {
                    throw new UnitOfWorkException( "No EntityStore service available in module " + name() );
                }
                store = service.get();
            }
        }
        return store;
    }

    /* package */ IdentityGenerator identityGenerator()
    {
        synchronized( this )
        {
            if( generator == null )
            {
                ServiceReference<IdentityGenerator> service = findService( IdentityGenerator.class );
                generator = service.get();
            }
            return generator;
        }
    }

    public ValueSerialization valueSerialization()
    {
        synchronized( this )
        {
            if( valueSerialization == null )
            {
                try
                {
                    ServiceReference<ValueSerialization> service = findService( ValueSerialization.class );
                    valueSerialization = service.get();
                }
                catch( NoSuchServiceException e )
                {
                    valueSerialization = new OrgJsonValueSerialization( layer.applicationInstance(), this, this );
                }
            }
        }
        return valueSerialization;
    }

    /* package */ MetricsProvider metricsProvider()
    {
        synchronized( this )
        {
            if( metrics == null )
            {
                try
                {
                    ServiceReference<MetricsProvider> service = findService( MetricsProvider.class );
                    metrics = service.get();
                }
                catch( NoSuchServiceException e )
                {
                    metrics = new MetricsProviderAdapter();
                }
            }
        }
        return metrics;
    }

    Iterable<ModelModule<ObjectModel>> visibleObjects( Visibility visibility )
    {
        return map( ModelModule.<ObjectModel>modelModuleFunction( this ),
                    filter( new VisibilitySpecification( visibility ), objects.models() ) );
    }

    Iterable<ModelModule<TransientModel>> visibleTransients( Visibility visibility )
    {
        return map( ModelModule.<TransientModel>modelModuleFunction( this ),
                    filter( new VisibilitySpecification( visibility ), transients.models() ) );
    }

    Iterable<ModelModule<EntityModel>> visibleEntities( Visibility visibility )
    {
        return map( ModelModule.<EntityModel>modelModuleFunction( this ),
                    filter( new VisibilitySpecification( visibility ), entities.models() ) );
    }

    Iterable<ModelModule<ValueModel>> visibleValues( Visibility visibility )
    {
        return map( ModelModule.<ValueModel>modelModuleFunction( this ),
                    filter( new VisibilitySpecification( visibility ), values.models() ) );
    }

    Iterable<ServiceReference<?>> visibleServices( Visibility visibility )
    {
        return flatten( services.visibleServices( visibility ),
                        importedServices.visibleServices( visibility ) );
    }

    // Module ClassLoader
    private static class ModuleClassLoader
        extends ClassLoader
    {

        private final ModuleInstance moduleInstance;
        private final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();

        private ModuleClassLoader( ModuleInstance moduleInstance, ClassLoader classLoader )
        {
            super( classLoader );
            this.moduleInstance = moduleInstance;
        }

        @Override
        protected Class<?> findClass( String name )
            throws ClassNotFoundException
        {
            Class<?> clazz = classes.get( name );
            if( clazz == null )
            {
                Specification<ModelDescriptor> modelTypeSpecification = modelTypeSpecification( name );
                Specification<ModelModule<ModelDescriptor>> translate = Specifications.translate( ModelModule.modelFunction(), modelTypeSpecification );
                // Check module
                {
                    Iterable<ModelModule<ModelDescriptor>> i = cast( flatten(
                        cast( moduleInstance.visibleObjects( Visibility.module ) ),
                        cast( moduleInstance.visibleEntities( Visibility.module ) ),
                        cast( moduleInstance.visibleTransients( Visibility.module ) ),
                        cast( moduleInstance.visibleValues( Visibility.module ) ) ) );

                    Iterable<ModelModule<ModelDescriptor>> moduleModels = filter( translate, i );
                    Iterator<ModelModule<ModelDescriptor>> iter = moduleModels.iterator();
                    if( iter.hasNext() )
                    {
                        clazz = first( iter.next().model().types() );

                        if( iter.hasNext() )
                        {
                            // Ambiguous exception
                            throw new ClassNotFoundException( name, new AmbiguousTypeException(
                                "More than one model matches the classname " + name + ":" + toList( moduleModels ) ) );
                        }
                    }
                }

                // Check layer
                if( clazz == null )
                {
                    Iterable<ModelModule<ModelDescriptor>> flatten = cast( flatten(
                        cast( moduleInstance.layerInstance().visibleObjects( Visibility.layer ) ),
                        cast( moduleInstance.layerInstance().visibleTransients( Visibility.layer ) ),
                        cast( moduleInstance.layerInstance().visibleEntities( Visibility.layer ) ),
                        cast( moduleInstance.layerInstance().visibleValues( Visibility.layer ) ),
                        cast( moduleInstance.layerInstance().visibleObjects( Visibility.application ) ),
                        cast( moduleInstance.layerInstance().visibleTransients( Visibility.application ) ),
                        cast( moduleInstance.layerInstance().visibleEntities( Visibility.application ) ),
                        cast( moduleInstance.layerInstance().visibleValues( Visibility.application ) ) ) );
                    Iterable<ModelModule<ModelDescriptor>> layerModels = filter( translate, flatten );
                    Iterator<ModelModule<ModelDescriptor>> iter = layerModels.iterator();
                    if( iter.hasNext() )
                    {
                        clazz = first( iter.next().model().types() );

                        if( iter.hasNext() )
                        {
                            // Ambiguous exception
                            throw new ClassNotFoundException( name, new AmbiguousTypeException(
                                "More than one model matches the classname " + name + ":" + toList( layerModels ) ) );
                        }
                    }
                }

                // Check used layers
                if( clazz == null )
                {
                    Iterable<ModelModule<ModelDescriptor>> flatten = cast( flatten(
                        cast( moduleInstance.layerInstance().usedLayersInstance().visibleObjects() ),
                        cast( moduleInstance.layerInstance().usedLayersInstance().visibleTransients() ),
                        cast( moduleInstance.layerInstance().usedLayersInstance().visibleEntities() ),
                        cast( moduleInstance.layerInstance().usedLayersInstance().visibleValues() ) ) );
                    Iterable<ModelModule<ModelDescriptor>> usedLayersModels = filter( translate, flatten );
                    Iterator<ModelModule<ModelDescriptor>> iter = usedLayersModels.iterator();
                    if( iter.hasNext() )
                    {
                        clazz = first( iter.next().model().types() );

                        if( iter.hasNext() )
                        {
                            // Ambiguous exception
                            throw new ClassNotFoundException( name, new AmbiguousTypeException(
                                "More than one model matches the classname " + name + ":" + toList( usedLayersModels ) ) );
                        }
                    }
                }

                if( clazz == null )
                {
                    throw new ClassNotFoundException( name );
                }
                classes.put( name, clazz );
            }

            return clazz;
        }
    }

}
