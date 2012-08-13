/*
 * Copyright (c) 2008-2011, Rickard Öberg. All Rights Reserved.
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

import org.json.JSONException;
import org.json.JSONTokener;
import org.qi4j.api.activation.Activation;
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
import org.qi4j.api.event.ActivationEvent;
import org.qi4j.api.event.ActivationEventListener;
import org.qi4j.api.json.JSONDeserializer;
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
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.api.value.NoSuchValueException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.functional.Function;
import org.qi4j.functional.Function2;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;
import org.qi4j.runtime.activation.ActivationDelegate;
import org.qi4j.runtime.activation.ActivationEventListenerSupport;
import org.qi4j.runtime.association.AssociationInfo;
import org.qi4j.runtime.association.AssociationInstance;
import org.qi4j.runtime.association.AssociationModel;
import org.qi4j.runtime.association.ManyAssociationInstance;
import org.qi4j.runtime.association.ManyAssociationModel;
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
import org.qi4j.runtime.property.PropertyInfo;
import org.qi4j.runtime.property.PropertyInstance;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.runtime.query.QueryBuilderFactoryImpl;
import org.qi4j.runtime.service.ImportedServicesInstance;
import org.qi4j.runtime.service.ImportedServicesModel;
import org.qi4j.runtime.service.ServicesInstance;
import org.qi4j.runtime.service.ServicesModel;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.runtime.value.ManyAssociationValueState;
import org.qi4j.runtime.value.ReferenceProperty;
import org.qi4j.runtime.value.ValueBuilderInstance;
import org.qi4j.runtime.value.ValueBuilderWithPrototype;
import org.qi4j.runtime.value.ValueBuilderWithState;
import org.qi4j.runtime.value.ValueInstance;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.runtime.value.ValueStateInstance;
import org.qi4j.runtime.value.ValuesModel;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.metrics.MetricsProviderAdapter;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import static org.qi4j.api.util.Classes.interfacesOf;
import static org.qi4j.functional.Iterables.*;

/**
 * Instance of a Qi4j Module. Contains the various composites for this Module.
 */
public class ModuleInstance
    implements Module, Activation
{
    private final ActivationDelegate activation = new ActivationDelegate( this );
    private final ActivationEventListenerSupport activationEventSupport = new ActivationEventListenerSupport();
    private final ModuleModel moduleModel;
    private final LayerInstance layerInstance;
    private final TransientsModel transients;
    private final ValuesModel values;
    private final ObjectsModel objects;
    private final EntitiesModel entities;
    private final ServicesInstance services;
    private final ImportedServicesInstance importedServices;

    //lazy assigned on accessor
    private EntityStore store;
    //lazy assigned on accessor
    private IdentityGenerator generator;
    //lazy assigned on accessor
    private MetricsProvider metrics;

    private final QueryBuilderFactory queryBuilderFactory;

    private final ClassLoader classLoader;

    private final Function2<EntityReference, Type, Object> entityFunction = new Function2<EntityReference, Type, Object>()
    {
        @Override
        public Object map( EntityReference entityReference, Type type )
        {
            return currentUnitOfWork().get( Classes.RAW_CLASS.map( type ), entityReference.identity() );
        }
    };

    // Lookup caches
    private final Map<Class, ModelModule<ObjectModel>> objectModels;
    private final Map<Class, ModelModule<TransientModel>> transientModels;
    private final Map<Class, Iterable<ModelModule<EntityModel>>> entityModels;
    private final Map<Class, ModelModule<ValueModel>> valueModels;

    public ModuleInstance( ModuleModel moduleModel, LayerInstance layerInstance, TransientsModel transientsModel,
                           EntitiesModel entitiesModel, ObjectsModel objectsModel, ValuesModel valuesModel,
                           ServicesModel servicesModel, ImportedServicesModel importedServicesModel
    )
    {
        this.moduleModel = moduleModel;
        this.layerInstance = layerInstance;
        transients = transientsModel;
        values = valuesModel;
        objects = objectsModel;
        entities = entitiesModel;
        services = servicesModel.newInstance( this );
        services.registerActivationEventListener( activationEventSupport );
        importedServices = importedServicesModel.newInstance( this );
        importedServices.registerActivationEventListener( activationEventSupport );

        queryBuilderFactory = new QueryBuilderFactoryImpl( this );

        objectModels = new ConcurrentHashMap<Class, ModelModule<ObjectModel>>();
        transientModels = new ConcurrentHashMap<Class, ModelModule<TransientModel>>();
        entityModels = new ConcurrentHashMap<Class, Iterable<ModelModule<EntityModel>>>();
        valueModels = new ConcurrentHashMap<Class, ModelModule<ValueModel>>();

        this.classLoader = new ModuleClassLoader( Thread.currentThread().getContextClassLoader() );
    }

    public String name()
    {
        return moduleModel.name();
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return moduleModel.metaInfo( infoType );
    }

    public ModuleModel model()
    {
        return moduleModel;
    }

    public LayerInstance layerInstance()
    {
        return layerInstance;
    }

    public ServicesInstance services()
    {
        return services;
    }

    public ImportedServicesInstance importedServices()
    {
        return importedServices;
    }

    public EntityDescriptor entityDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            Iterable<ModelModule<EntityModel>> entityModels = findEntityModels( type );
            return first( map( ModelModule.<EntityModel>modelFunction(), entityModels ) );
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    public ObjectDescriptor objectDescriptor( String typeName )
    {
        try
        {
            Class<?> type = classLoader().loadClass( typeName );
            return findObjectModels( type ).model();
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    public TransientDescriptor transientDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            return findTransientModels( type ).model();
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    public ValueDescriptor valueDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            ModelModule<ValueModel> valueModel = findValueModels( type );
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

    public ClassLoader classLoader()
    {
        return classLoader;
    }

    public void activate()
        throws Exception
    {
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATING ) );
        activation.activate( moduleModel.newActivatorsInstance(), 
                                    Iterables.<Activation, Activation>iterable( services, importedServices ) );
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATED ) );
    }

    public void passivate()
        throws Exception
    {
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATING ) );
        activation.passivate();
        activationEventSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATED ) );
    }

    @Override
    public String toString()
    {
        return moduleModel.toString();
    }

    public Function2<EntityReference, Type, Object> getEntityFunction()
    {
        return entityFunction;
    }

    public Iterable<ModelModule<EntityModel>> findEntityModels( final Class type )
    {
        Iterable<ModelModule<EntityModel>> models = entityModels.get( type );

        if( models == null )
        {
            Specification<EntityModel> hasRole = new Specification<EntityModel>()
            {
                @Override
                public boolean satisfiedBy( EntityModel item )
                {
                    return item.hasRole( type );
                }
            };

            LinkedHashSet<ModelModule<EntityModel>> result = new LinkedHashSet<ModelModule<EntityModel>>();
            Iterables.addAll( result, ambiguousCheck( type, findModels( Classes.exactTypeSpecification( type ), visibleEntities( Visibility.module ), layerInstance()
                .visibleEntities( Visibility.layer ), layerInstance().visibleEntities( Visibility.application ), layerInstance()
                .usedLayersInstance()
                .visibleEntities() ) ) );
            Iterables.addAll( result, findModels( hasRole, visibleEntities( Visibility.module ), layerInstance().visibleEntities( Visibility.layer ), layerInstance()
                .visibleEntities( Visibility.application ), layerInstance().usedLayersInstance().visibleEntities() ) );

            models = result;

            entityModels.put( type, models );
        }

        return models;
    }

    public ModelModule<TransientModel> findTransientModels( final Class type )
    {
        ModelModule<TransientModel> model = transientModels.get( type );

        if( model == null )
        {
            Iterable<ModelModule<TransientModel>> flatten = flatten( ambiguousCheck( type, findModels( Classes.exactTypeSpecification( type ), visibleTransients( Visibility.module ), layerInstance()
                .visibleTransients( Visibility.layer ), layerInstance().visibleTransients( Visibility.application ), layerInstance()
                .usedLayersInstance()
                .visibleTransients() ) ),
                                                                     ambiguousCheck( type, findModels( Classes.assignableTypeSpecification( type ), visibleTransients( Visibility.module ), layerInstance()
                                                                         .visibleTransients( Visibility.layer ), layerInstance()
                                                                         .visibleTransients( Visibility.application ), layerInstance()
                                                                         .usedLayersInstance()
                                                                         .visibleTransients() ) ) );
            model = Iterables.first( flatten );

            if( model != null )
            {
                transientModels.put( type, model );
            }
        }

        return model;
    }

    public ModelModule<ObjectModel> findObjectModels( final Class type )
    {
        ModelModule<ObjectModel> model = objectModels.get( type );

        if( model == null )
        {
            Iterable<ModelModule<ObjectModel>> flatten = Iterables.flatten( ambiguousCheck( type, findModels( Classes.exactTypeSpecification( type ), visibleObjects( Visibility.module ), layerInstance()
                .visibleObjects( Visibility.layer ), layerInstance().visibleObjects( Visibility.application ), layerInstance()
                .usedLayersInstance()
                .visibleObjects() ) ),
                                                                            ambiguousCheck( type, findModels( Classes.assignableTypeSpecification( type ), visibleObjects( Visibility.module ), layerInstance()
                                                                                .visibleObjects( Visibility.layer ), layerInstance()
                                                                                .visibleObjects( Visibility.application ), layerInstance()
                                                                                .usedLayersInstance()
                                                                                .visibleObjects() ) ) );

            model = Iterables.first( flatten );

            if( model != null )
            {
                objectModels.put( type, model );
            }
        }

        return model;
    }

    public ModelModule<ValueModel> findValueModels( final Class type )
    {
        ModelModule<ValueModel> model = valueModels.get( type );

        if( model == null )
        {
            Iterable<ModelModule<ValueModel>> flatten = Iterables.flatten(
                ambiguousCheck( type,
                                findModels( Classes.exactTypeSpecification( type ),
                                            visibleValues( Visibility.module ),
                                            layerInstance().visibleValues( Visibility.layer ),
                                            layerInstance().visibleValues( Visibility.application ),
                                            layerInstance().usedLayersInstance()
                                                .visibleValues() ) ),
                ambiguousCheck( type,
                                findModels( Classes.assignableTypeSpecification( type ),
                                            visibleValues( Visibility.module ),
                                            layerInstance().visibleValues( Visibility.layer ),
                                            layerInstance().visibleValues( Visibility.application ),
                                            layerInstance().usedLayersInstance()
                                                .visibleValues() ) ) );

            model = Iterables.first( flatten );

            if( model != null )
            {
                valueModels.put( type, model );
            }
        }

        return model;
    }

    private <T extends ModelDescriptor> Iterable<ModelModule<T>> findModels( Specification<? super T> specification,
                                                                             Iterable<ModelModule<T>>... models
    )
    {
        Specification<ModelModule<T>> spec = Specifications.translate( ModelModule.<T>modelFunction(), specification );
        return Iterables.filter( spec, Iterables.flattenIterables( Iterables.iterable( models ) ) );
    }

    public Iterable<ModelModule<ObjectModel>> visibleObjects( Visibility visibility )
    {
        return map( ModelModule.<ObjectModel>modelModuleFunction( this ), filter( new VisibilitySpecification( visibility ), objects
            .models() ) );
    }

    Iterable<ModelModule<TransientModel>> visibleTransients( Visibility visibility )
    {
        return map( ModelModule.<TransientModel>modelModuleFunction( this ), filter( new VisibilitySpecification( visibility ), transients
            .models() ) );
    }

    public Iterable<ModelModule<EntityModel>> visibleEntities( Visibility visibility )
    {
        return map( ModelModule.<EntityModel>modelModuleFunction( this ), filter( new VisibilitySpecification( visibility ), entities
            .models() ) );
    }

    Iterable<ModelModule<ValueModel>> visibleValues( Visibility visibility )
    {
        return map( ModelModule.<ValueModel>modelModuleFunction( this ), filter( new VisibilitySpecification( visibility ), values
            .models() ) );
    }

    Iterable<ServiceReference> visibleServices( Visibility visibility )
    {
        return Iterables.flatten( services.visibleServices( visibility ),
                                  importedServices.visibleServices( visibility ) );
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

    public IdentityGenerator identityGenerator()
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

    public MetricsProvider metricsProvider()
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

    // Implementation of TransientBuilderFactory
    public <T> TransientBuilder<T> newTransientBuilder( Class<T> mixinType )
        throws NoSuchTransientException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );

        ModelModule<TransientModel> model = findTransientModels( mixinType );

        if( model == null )
        {
            throw new NoSuchTransientException( mixinType.getName(), name() );
        }

        Map<AccessibleObject, Property<?>> properties = new HashMap<AccessibleObject, Property<?>>();
        for( PropertyModel propertyModel : model.model().state().properties() )
        {
            Property property = new PropertyInstance<Object>( propertyModel.getBuilderInfo(), propertyModel.initialValue( model
                                                                                                                              .module() ) );
            properties.put( propertyModel.accessor(), property );
        }

        TransientStateInstance state = new TransientStateInstance( properties );

        return new TransientBuilderInstance<T>( model, state, UsesInstance.EMPTY_USES );
    }

    public <T> T newTransient( final Class<T> mixinType, Object... uses )
        throws NoSuchTransientException, ConstructionException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );

        ModelModule<TransientModel> model = findTransientModels( mixinType );

        if( model == null )
        {
            throw new NoSuchTransientException( mixinType.getName(), name() );
        }

        Map<AccessibleObject, Property<?>> properties = new HashMap<AccessibleObject, Property<?>>();
        for( PropertyModel propertyModel : model.model().state().properties() )
        {
            Property property = new PropertyInstance<Object>( propertyModel, propertyModel.initialValue( model.module() ) );
            properties.put( propertyModel.accessor(), property );
        }

        TransientStateInstance state = new TransientStateInstance( properties );

        model.model().checkConstraints( state );
        return model.model().newInstance( model.module(), UsesInstance.EMPTY_USES.use( uses ), state ).<T>proxy();
    }

    // Implementation of ObjectFactory
    public <T> T newObject( Class<T> mixinType, Object... uses )
        throws NoSuchObjectException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ModelModule<ObjectModel> model = findObjectModels( mixinType );

        if( model == null )
        {
            throw new NoSuchObjectException( mixinType.getName(), name() );
        }

        InjectionContext injectionContext = new InjectionContext( model.module(), UsesInstance.EMPTY_USES.use( uses ) );
        return mixinType.cast( model.model().newInstance( injectionContext ) );
    }

    @Override
    public void injectTo( Object instance, Object... uses )
        throws ConstructionException
    {
        NullArgumentException.validateNotNull( "instance", instance );
        ModelModule<ObjectModel> model = findObjectModels( instance.getClass() );

        if( model == null )
        {
            throw new NoSuchObjectException( instance.getClass().getName(), name() );
        }
        InjectionContext injectionContext = new InjectionContext( model.module(), UsesInstance.EMPTY_USES.use( uses ) );
        model.model().inject( injectionContext, instance );
    }

    // Implementation of ValueBuilderFactory
    public <T> T newValue( Class<T> mixinType )
        throws NoSuchValueException, ConstructionException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ModelModule<ValueModel> model = findValueModels( mixinType );

        if( model == null )
        {
            throw new NoSuchValueException( mixinType.getName(), name() );
        }

        Map<AccessibleObject, PropertyInstance<?>> properties = new LinkedHashMap<AccessibleObject, PropertyInstance<?>>();
        for( PropertyDescriptor propertyDescriptor : model.model().state().properties() )
        {
            Object initialValue = propertyDescriptor.initialValue( model.module() );
            PropertyInstance value = new PropertyInstance<Object>( (PropertyInfo) propertyDescriptor, initialValue );
            properties.put( propertyDescriptor.accessor(), value );
        }

        Map<AccessibleObject, AssociationInstance<?>> associations = new LinkedHashMap<AccessibleObject, AssociationInstance<?>>();
        for( AssociationDescriptor associationDescriptor : model.model().state().associations() )
        {
            AssociationInstance instance = new AssociationInstance<Object>( (AssociationInfo) associationDescriptor, entityFunction, new ReferenceProperty() );
            associations.put( associationDescriptor.accessor(), instance );
        }

        Map<AccessibleObject, ManyAssociationInstance<?>> manyAssociations = new LinkedHashMap<AccessibleObject, ManyAssociationInstance<?>>();
        for( AssociationDescriptor associationDescriptor : model.model().state().manyAssociations() )
        {
            manyAssociations.put( associationDescriptor.accessor(), new ManyAssociationInstance<Object>( (AssociationInfo) associationDescriptor, entityFunction, new ManyAssociationValueState( new ArrayList<EntityReference>() ) ) );
        }

        ValueStateInstance state = new ValueStateInstance( properties, associations, manyAssociations );

        model.model().checkConstraints( state );
        return mixinType.cast( model.model().newValueInstance( model.module(), state ).proxy() );
    }

    public <T> ValueBuilder<T> newValueBuilder( Class<T> mixinType )
        throws NoSuchValueException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ModelModule<ValueModel> modelModule = findValueModels( mixinType );

        if( modelModule == null )
        {
            throw new NoSuchValueException( mixinType.getName(), name() );
        }

        ValueModel valueModel = modelModule.model();
        Map<AccessibleObject, PropertyInstance<?>> properties = new LinkedHashMap<AccessibleObject, PropertyInstance<?>>();
        for( PropertyDescriptor propertyDescriptor : valueModel.state().properties() )
        {
            PropertyInfo builderInfo = ( (PropertyModel) propertyDescriptor ).getBuilderInfo();
            Object initialValue = propertyDescriptor.initialValue( modelModule.module() );
            PropertyInstance<Object> propertyInstance = new PropertyInstance<Object>( builderInfo, initialValue );
            properties.put( propertyDescriptor.accessor(), propertyInstance );
        }

        Map<AccessibleObject, AssociationInstance<?>> associations = new LinkedHashMap<AccessibleObject, AssociationInstance<?>>();
        for( AssociationDescriptor associationDescriptor : valueModel.state().associations() )
        {
            AssociationInfo builderInfo = ( (AssociationModel) associationDescriptor ).getBuilderInfo();
            ReferenceProperty referenceProperty = new ReferenceProperty();
            AssociationInstance<Object> associationInstance = new AssociationInstance<Object>( builderInfo, entityFunction, referenceProperty );
            associations.put( associationDescriptor.accessor(), associationInstance );
        }

        Map<AccessibleObject, ManyAssociationInstance<?>> manyAssociations = new LinkedHashMap<AccessibleObject, ManyAssociationInstance<?>>();
        for( AssociationDescriptor associationDescriptor : valueModel.state().manyAssociations() )
        {
            AssociationInfo builderInfo = ( (ManyAssociationModel) associationDescriptor ).getBuilderInfo();
            ManyAssociationValueState manyAssociationState = new ManyAssociationValueState( new ArrayList<EntityReference>() );
            ManyAssociationInstance<Object> associationInstance = new ManyAssociationInstance<Object>( builderInfo, entityFunction, manyAssociationState );
            manyAssociations.put( associationDescriptor.accessor(), associationInstance );
        }

        ValueStateInstance state = new ValueStateInstance( properties, associations, manyAssociations );
        return new ValueBuilderInstance<T>( modelModule , this, state);
    }

    @Override
    public <T> ValueBuilder<T> newValueBuilderWithState( Class<T> mixinType,
                                                         Function<PropertyDescriptor, Object> propertyFunction,
                                                         Function<AssociationDescriptor, EntityReference> associationFunction,
                                                         Function<AssociationDescriptor, Iterable<EntityReference>> manyAssociationFunction
    )
    {
        NullArgumentException.validateNotNull( "propertyFunction", propertyFunction );
        NullArgumentException.validateNotNull( "associationFunction", associationFunction );
        NullArgumentException.validateNotNull( "manyAssociationFunction", manyAssociationFunction );

        ModelModule<ValueModel> model = findValueModels( mixinType );

        if( model == null )
        {
            throw new NoSuchValueException( mixinType.getName(), name() );
        }

        Map<AccessibleObject, PropertyInstance<?>> properties = new LinkedHashMap<AccessibleObject, PropertyInstance<?>>();
        for( PropertyDescriptor propertyDescriptor : model.model().state().properties() )
        {
            properties.put( propertyDescriptor.accessor(), new PropertyInstance<Object>( ( (PropertyModel) propertyDescriptor )
                                                                                             .getBuilderInfo(), propertyFunction
                .map( propertyDescriptor ) ) );
        }

        Map<AccessibleObject, AssociationInstance<?>> associations = new LinkedHashMap<AccessibleObject, AssociationInstance<?>>();
        for( AssociationDescriptor associationDescriptor : model.model().state().associations() )
        {
            associations.put( associationDescriptor.accessor(), new AssociationInstance<Object>( ( (AssociationModel) associationDescriptor )
                                                                                                     .getBuilderInfo(), entityFunction, new ReferenceProperty( associationFunction
                                                                                                                                                                   .map( associationDescriptor ) ) ) );
        }

        Map<AccessibleObject, ManyAssociationInstance<?>> manyAssociations = new LinkedHashMap<AccessibleObject, ManyAssociationInstance<?>>();
        for( AssociationDescriptor associationDescriptor : model.model().state().manyAssociations() )
        {
            manyAssociations.put( associationDescriptor.accessor(), new ManyAssociationInstance<Object>( ( (ManyAssociationModel) associationDescriptor )
                                                                                                             .getBuilderInfo(), entityFunction, new ManyAssociationValueState( Iterables
                                                                                                                                                                                   .toList( manyAssociationFunction
                                                                                                                                                                                                .map( associationDescriptor ) ) ) ) );
        }

        ValueStateInstance state = new ValueStateInstance( properties, associations, manyAssociations );
        ValueInstance instance = model.model().newValueInstance( model.module(), state );
        instance.prepareToBuild();

        return new ValueBuilderWithState<T>( model, instance );
    }

    @Override
    public <T> ValueBuilder<T> newValueBuilderWithPrototype( T prototype )
    {
        NullArgumentException.validateNotNull( "prototype", prototype );

        ValueInstance valueInstance = ValueInstance.getValueInstance( (ValueComposite) prototype );
        Class<Composite> valueType = (Class<Composite>) first( valueInstance.types() );

        ModelModule<ValueModel> model = findValueModels( valueType );

        if( model == null )
        {
            throw new NoSuchValueException( valueType.getName(), name() );
        }
        return new ValueBuilderWithPrototype<T>( model, this, prototype);
    }

    public <T> T newValueFromJSON( Class<T> mixinType, String jsonValue )
        throws NoSuchValueException, ConstructionException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ModelModule<ValueModel> model = findValueModels( mixinType );

        if( model == null )
        {
            throw new NoSuchValueException( mixinType.getName(), name() );
        }

        try
        {
            return (T) new JSONDeserializer( model.module() ).deserialize( new JSONTokener( jsonValue ).nextValue(), model
                .model()
                .valueType() );
        }
        catch( JSONException e )
        {
            throw new ConstructionException( "Could not create value from JSON", e );
        }
    }

    // Implementation of UnitOfWorkFactory
    public UnitOfWork newUnitOfWork()
    {
        return newUnitOfWork( Usecase.DEFAULT );
    }

    public UnitOfWork newUnitOfWork( long currentTime )
    {
        return newUnitOfWork( Usecase.DEFAULT, currentTime );
    }

    public UnitOfWork newUnitOfWork( Usecase usecase )
    {
        if( usecase == null )
        {
            usecase = Usecase.DEFAULT;
        }
        return newUnitOfWork( usecase, System.currentTimeMillis() );
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

    public UnitOfWork currentUnitOfWork()
    {
        Stack<UnitOfWorkInstance> stack = UnitOfWorkInstance.getCurrent();
        if( stack.size() == 0 )
        {
            throw new IllegalStateException( "No current UnitOfWork active" );
        }
        return new ModuleUnitOfWork( ModuleInstance.this, stack.peek() );
    }

    public UnitOfWork getUnitOfWork( EntityComposite entity )
    {
        EntityInstance instance = EntityInstance.getEntityInstance( entity );
        return instance.unitOfWork();
    }

    // Implementation of ServiceFinder
    Map<Type, ServiceReference> serviceReferences = new ConcurrentHashMap<Type, ServiceReference>();
    Map<Type, Iterable<ServiceReference>> servicesReferences = new ConcurrentHashMap<Type, Iterable<ServiceReference>>();

    public <T> ServiceReference<T> findService( final Class<T> serviceType )
    {
        ServiceReference serviceReference = serviceReferences.get( serviceType );
        if( serviceReference == null )
        {
            Iterable<ServiceReference<T>> references = findServices( serviceType );
            serviceReference = Iterables.first( references );
            if( serviceReference != null )
            {
                serviceReferences.put( serviceType, serviceReference );
            }
        }

        if( serviceReference == null )
        {
            throw new NoSuchServiceException( serviceType.getName(), name() );
        }

        return serviceReference;
    }

    @Override
    public <T> ServiceReference<T> findService( Type serviceType )
    {
        ServiceReference serviceReference = serviceReferences.get( serviceType );
        if( serviceReference == null )
        {
            serviceReference = Iterables.first( findServices( serviceType ) );
            if( serviceReference != null )
            {
                serviceReferences.put( serviceType, serviceReference );
            }
        }

        if( serviceReference == null )
        {
            throw new IllegalArgumentException( "No service of type '" + serviceType + "' found" );
        }

        return serviceReference;
    }

    @Override
    public <T> Iterable<ServiceReference<T>> findServices( Class<T> serviceType )
    {
        return findServices( (Type) serviceType );
    }

    public <T> Iterable<ServiceReference<T>> findServices( final Type serviceType )
    {
        Iterable<ServiceReference> iterable = servicesReferences.get( serviceType );
        if( iterable == null )
        {
            Specification<Iterable<Class<?>>> typeSpecification = new Specification<Iterable<Class<?>>>()
            {
                @Override
                public boolean satisfiedBy( Iterable<Class<?>> item )
                {
                    if( serviceType instanceof Class )
                    {
                        // Straight class assignability check
                        return checkClassMatch( item, (Class) serviceType );
                    }
                    else if( serviceType instanceof ParameterizedType )
                    {
                        // Foo<Bar> check
                        // First check Foo
                        ParameterizedType parameterizedType = (ParameterizedType) serviceType;
                        if( !checkClassMatch( item, (Class) parameterizedType.getRawType() ) )
                        {
                            return false;
                        }

                        // Then check Bar
                        for( Type intf : interfacesOf( item ) )
                        {
                            if( intf.equals( serviceType ) )
                            {
                                return true;
                            }
                        }

                        // All parameters are the same - ok!
                        return false;
                    }
                    else if( serviceType instanceof WildcardType )
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }

                private boolean checkClassMatch( Iterable<Class<?>> types, Class type )
                {
                    for( Class<?> clazz : types )
                    {
                        if( type.isAssignableFrom( clazz ) )
                        {
                            return true;
                        }
                    }
                    return false;
                }
            };

            Function<ServiceReference, Iterable<Class<?>>> function = new Function<ServiceReference, Iterable<Class<?>>>()
            {
                @Override
                public Iterable<Class<?>> map( ServiceReference serviceReference )
                {
                    return serviceReference.types();
                }
            };
            Specification<ServiceReference> referenceTypeCheck = Specifications.translate( function, typeSpecification );

            Iterable<ServiceReference> matchingServices = Iterables.flatten(
                Iterables.filter( referenceTypeCheck, visibleServices( Visibility.module ) ),
                Iterables.filter( referenceTypeCheck, layerInstance.visibleServices( Visibility.layer ) ),
                Iterables.filter( referenceTypeCheck, layerInstance.visibleServices( Visibility.application ) ),
                Iterables.filter( referenceTypeCheck, layerInstance.usedLayersInstance().visibleServices() ) );

            iterable = Iterables.toList( matchingServices );
            servicesReferences.put( serviceType, iterable );
        }

        return Iterables.cast( iterable );
    }

    // Implementation of QueryBuilderFactory

    /**
     * @see QueryBuilderFactory#newQueryBuilder(Class)
     */
    public <T> QueryBuilder<T> newQueryBuilder( final Class<T> resultType )
    {
        return queryBuilderFactory.newQueryBuilder( resultType );
    }

    // Module classloader
    private class ModuleClassLoader
        extends ClassLoader
    {
        Map<String, Class> classes = new ConcurrentHashMap<String, Class>();

        private ModuleClassLoader( ClassLoader classLoader )
        {
            super( classLoader );
        }

        @Override
        protected Class<?> findClass( String name )
            throws ClassNotFoundException
        {
            Class clazz = classes.get( name );
            if( clazz == null )
            {
                Specification<ModelDescriptor> modelTypeSpecification = Classes.modelTypeSpecification( name );
                Specification<ModelModule<ModelDescriptor>> translate = Specifications.translate( ModelModule.modelFunction(), modelTypeSpecification );

                // Check module
                {
                    Iterable<ModelModule<ModelDescriptor>> i = cast( flatten( cast( visibleObjects( Visibility.module ) ),
                                                                              cast( visibleEntities( Visibility.module ) ),
                                                                              cast( visibleTransients( Visibility.module ) ),
                                                                              cast( visibleValues( Visibility.module ) ) ) );

                    Iterable<ModelModule<ModelDescriptor>> moduleModels = filter( translate, i );
                    Iterator<ModelModule<ModelDescriptor>> iter = moduleModels.iterator();
                    if( iter.hasNext() )
                    {
                        clazz = first( iter.next().model().types() );

                        if( iter.hasNext() )
                        {
                            // Ambiguous exception
                            throw new ClassNotFoundException( name, new AmbiguousTypeException( "More than one model matches the classname " + name + ":" + Iterables
                                .toList( moduleModels ) ) );
                        }
                    }
                }

                // Check layer
                if( clazz == null )
                {
                    Iterable<ModelModule<ModelDescriptor>> flatten = cast( flatten(
                        cast( layerInstance().visibleObjects( Visibility.layer ) ),
                        cast( layerInstance().visibleTransients( Visibility.layer ) ),
                        cast( layerInstance().visibleEntities( Visibility.layer ) ),
                        cast( layerInstance().visibleValues( Visibility.layer ) ),
                        cast( layerInstance().visibleObjects( Visibility.application ) ),
                        cast( layerInstance().visibleTransients( Visibility.application ) ),
                        cast( layerInstance().visibleEntities( Visibility.application ) ),
                        cast( layerInstance().visibleValues( Visibility.application ) ) ) );
                    Iterable<ModelModule<ModelDescriptor>> layerModels = filter( translate, flatten );
                    Iterator<ModelModule<ModelDescriptor>> iter = layerModels.iterator();
                    if( iter.hasNext() )
                    {
                        clazz = first( iter.next().model().types() );

                        if( iter.hasNext() )
                        {
                            // Ambiguous exception
                            throw new ClassNotFoundException( name, new AmbiguousTypeException( "More than one model matches the classname " + name + ":" + Iterables
                                .toList( layerModels ) ) );
                        }
                    }
                }

                // Check used layers
                if( clazz == null )
                {
                    Iterable<ModelModule<ModelDescriptor>> flatten = cast( flatten(
                        cast( layerInstance().usedLayersInstance().visibleObjects() ),
                        cast( layerInstance().usedLayersInstance().visibleTransients() ),
                        cast( layerInstance().usedLayersInstance().visibleEntities() ),
                        cast( layerInstance().usedLayersInstance().visibleValues() ) ) );
                    Iterable<ModelModule<ModelDescriptor>> usedLayersModels = filter( translate, flatten );
                    Iterator<ModelModule<ModelDescriptor>> iter = usedLayersModels.iterator();
                    if( iter.hasNext() )
                    {
                        clazz = first( iter.next().model().types() );

                        if( iter.hasNext() )
                        {
                            // Ambiguous exception
                            throw new ClassNotFoundException( name, new AmbiguousTypeException( "More than one model matches the classname " + name + ":" + Iterables
                                .toList( usedLayersModels ) ) );
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

    /**
     * Check if the list of models contains several ones with the same visibility. If yes, then
     * throw an AmbiguousTypeException
     *
     * @param type   the type that was checked
     * @param models
     * @param <T>
     *
     * @return
     */
    private <T extends ModelDescriptor> Iterable<ModelModule<T>> ambiguousCheck( final Class type,
                                                                                 final Iterable<ModelModule<T>> models
    )
    {
        return new Iterable<ModelModule<T>>()
        {
            @Override
            public Iterator<ModelModule<T>> iterator()
            {
                ModelModule current = null;
                List<ModelModule<T>> ambiguous = null;
                List<ModelModule<T>> results = new ArrayList<ModelModule<T>>();

                for( ModelModule<T> model : models )
                {
                    if( current != null && !model.equals( current ) )
                    {
                        if( model.model().visibility() == current.model().visibility() )
                        {
                            if( ambiguous == null )
                            {
                                ambiguous = new ArrayList<ModelModule<T>>();
                            }
                            ambiguous.add( model );
                        }
                    }
                    else
                    {
                        current = model;
                    }

                    results.add( model );
                }

                if( ambiguous != null )
                {
                    // Check if we had any ambiguities
                    ambiguous.add( current );
                    throw new AmbiguousTypeException( "More than one type matches " + type.getName() + ":" + ambiguous );
                }

                // Ambiguity check done, and no ambiguities found. Return results
                return results.iterator();
            }
        };
    }

    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activationEventSupport.registerActivationEventListener( listener );
    }

    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activationEventSupport.deregisterActivationEventListener( listener );
    }
}
