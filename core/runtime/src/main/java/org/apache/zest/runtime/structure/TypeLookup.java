/*
 * Copyright (c) 2008-2012, Rickard Öberg.
 * Copyright (c) 2008-2012, Niclas Hedhman.
 * Copyright (c) 2012, Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.runtime.structure;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.zest.api.composite.AmbiguousTypeException;
import org.apache.zest.api.composite.ModelDescriptor;
import org.apache.zest.api.service.NoSuchServiceException;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.runtime.composite.TransientModel;
import org.apache.zest.runtime.entity.EntityModel;
import org.apache.zest.runtime.legacy.Specifications;
import org.apache.zest.runtime.object.ObjectModel;
import org.apache.zest.runtime.value.ValueModel;
import org.apache.zest.spi.module.ModelModule;

import static java.util.stream.Stream.concat;
import static org.apache.zest.api.common.Visibility.application;
import static org.apache.zest.api.common.Visibility.layer;
import static org.apache.zest.api.common.Visibility.module;
import static org.apache.zest.api.util.Classes.RAW_CLASS;
import static org.apache.zest.api.util.Classes.interfacesOf;
import static org.apache.zest.functional.Iterables.cast;
import static org.apache.zest.functional.Iterables.first;

/**
 * Central place for Composite Type lookups.
 */
public class TypeLookup
{

    // Constructor parameters
    private final ModuleInstance moduleInstance;
    // Eager instance objects
    private final Map<Class<?>, ModelModule<ObjectModel>> objectModels;
    private final Map<Class<?>, ModelModule<TransientModel>> transientModels;
    private final Map<Class<?>, ModelModule<ValueModel>> valueModels;
    private final Map<Class<?>, List<ModelModule<EntityModel>>> allEntityModels;
    private final Map<Class<?>, ModelModule<EntityModel>> unambiguousEntityModels;
    private final Map<Type, ServiceReference<?>> serviceReferences;
    private final Map<Type, Iterable<ServiceReference<?>>> servicesReferences;

    /**
     * Create a new TypeLookup bound to the given ModuleInstance.
     *
     * @param moduleInstance ModuleInstance bound to this TypeLookup
     */
    /* package */ TypeLookup( ModuleInstance moduleInstance )
    {
        // Constructor parameters
        this.moduleInstance = moduleInstance;

        // Eager instance objects
        objectModels = new ConcurrentHashMap<>();
        transientModels = new ConcurrentHashMap<>();
        valueModels = new ConcurrentHashMap<>();
        allEntityModels = new ConcurrentHashMap<>();
        unambiguousEntityModels = new ConcurrentHashMap<>();
        serviceReferences = new ConcurrentHashMap<>();
        servicesReferences = new ConcurrentHashMap<>();
    }

    /**
     * Lookup first Object Model matching the given Type.
     *
     * <p>First, if Object Models exactly match the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>exact</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Second, if Object Models match a type assignable to the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>assignable</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * @param type Looked up Type
     *
     * @return First matching Object Model
     */
    @SuppressWarnings( { "raw", "unchecked" } )
    /* package */ ModelModule<ObjectModel> lookupObjectModel( final Class type )
    {
        ModelModule<ObjectModel> model = objectModels.get( type );

        if( model == null )
        {
            // Unambiguously and lazily resolve ObjectModel
            Stream<ModelModule<? extends ModelDescriptor>> models = concat(
                ambiguousTypeCheck( type,
                                    findModels( new ExactTypeLookupSpecification( type ),
                                                moduleInstance.visibleObjects( module ),
                                                moduleInstance.layerInstance().visibleObjects( layer ),
                                                moduleInstance.layerInstance().visibleObjects( application ),
                                                moduleInstance.layerInstance()
                                                    .usedLayersInstance()
                                                    .visibleObjects() ) ),

                ambiguousTypeCheck( type,
                                    findModels( new AssignableTypeLookupSpecification( type ),
                                                moduleInstance.visibleObjects( module ),
                                                moduleInstance.layerInstance()
                                                    .visibleObjects( layer ),
                                                moduleInstance.layerInstance()
                                                    .visibleObjects( application ),
                                                moduleInstance.layerInstance()
                                                    .usedLayersInstance()
                                                    .visibleObjects() ) ) );

            model = (ModelModule<ObjectModel>) models.findFirst().orElse( null );

            if( model != null )
            {
                objectModels.put( type, model );
            }
        }

        return model;
    }

    /**
     * Lookup first Transient Model matching the given Type.
     *
     * <p>First, if Transient Models exactly match the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>exact</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Second, if Transient Models match a type assignable to the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>assignable</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * @param type Looked up Type
     *
     * @return First matching Transient Model
     */
    @SuppressWarnings( { "raw", "unchecked" } )
    /* package */ ModelModule<TransientModel> lookupTransientModel( final Class type )
    {
        ModelModule<TransientModel> model = transientModels.get( type );

        if( model == null )
        {
            // Unambiguously and lazily resolve ObjectModel
            Stream<ModelModule<? extends ModelDescriptor>> models = concat(
                ambiguousTypeCheck( type,
                                    findModels( new ExactTypeLookupSpecification( type ),
                                                moduleInstance.visibleTransients( module ),
                                                moduleInstance.layerInstance().visibleTransients( layer ),
                                                moduleInstance.layerInstance().visibleTransients( application ),
                                                moduleInstance.layerInstance()
                                                    .usedLayersInstance()
                                                    .visibleTransients() ) ),

                ambiguousTypeCheck( type,
                                    findModels( new AssignableTypeLookupSpecification( type ),
                                                moduleInstance.visibleTransients( module ),
                                                moduleInstance.layerInstance()
                                                    .visibleTransients( layer ),
                                                moduleInstance.layerInstance()
                                                    .visibleTransients( application ),
                                                moduleInstance.layerInstance()
                                                    .usedLayersInstance()
                                                    .visibleTransients() ) ) );

            model = (ModelModule<TransientModel>) models.findFirst().orElse( null );

            if( model != null )
            {
                transientModels.put( type, model );
            }
        }
        return model;
    }

    /**
     * Lookup first Value Model matching the given Type.
     *
     * <p>First, if Value Models exactly match the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>exact</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Second, if Value Models match a type assignable to the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>assignable</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * @param type Looked up Type
     *
     * @return First matching Value Model
     */
    @SuppressWarnings( { "raw", "unchecked" } )
    public ModelModule<ValueModel> lookupValueModel( final Class type )
    {
        ModelModule<ValueModel> model = valueModels.get( type );

        if( model == null )
        {
            // Unambiguously and lazily resolve ObjectModel
            Stream<ModelModule<? extends ModelDescriptor>> models = concat(
                ambiguousTypeCheck( type,
                                    findModels( new ExactTypeLookupSpecification( type ),
                                                moduleInstance.visibleValues( module ),
                                                moduleInstance.layerInstance().visibleValues( layer ),
                                                moduleInstance.layerInstance().visibleValues( application ),
                                                moduleInstance.layerInstance()
                                                    .usedLayersInstance()
                                                    .visibleValues() ) ),

                ambiguousTypeCheck( type,
                                    findModels( new AssignableTypeLookupSpecification( type ),
                                                moduleInstance.visibleValues( module ),
                                                moduleInstance.layerInstance()
                                                    .visibleValues( layer ),
                                                moduleInstance.layerInstance()
                                                    .visibleValues( application ),
                                                moduleInstance.layerInstance()
                                                    .usedLayersInstance()
                                                    .visibleValues() ) ) );

            model = (ModelModule<ValueModel>) models.findFirst().orElse( null );

            if( model != null )
            {
                valueModels.put( type, model );
            }
        }

        return model;
    }

    /**
     * Lookup first Entity Model matching the given Type.
     *
     * <p>First, if Entity Models exactly match the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>exact</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Second, if Entity Models match a type assignable to the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>assignable</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * <p><b>Should be used for creational use cases only.</b> For non-creational use cases see
     * {@link #lookupEntityModels(java.lang.Class)}.</p>
     *
     * @param type Looked up Type
     *
     * @return First matching Entity Model
     */
    @SuppressWarnings( { "raw", "unchecked" } )
    /* package */ ModelModule<EntityModel> lookupEntityModel( final Class type )
    {
        ModelModule<EntityModel> model = unambiguousEntityModels.get( type );

        if( model == null )
        {
            // Unambiguously and lazily resolve ObjectModel
            Stream<ModelModule<? extends ModelDescriptor>> models = concat(
                ambiguousTypeCheck( type,
                                    findModels( new ExactTypeLookupSpecification( type ),
                                                moduleInstance.visibleEntities( module ),
                                                moduleInstance.layerInstance().visibleEntities( layer ),
                                                moduleInstance.layerInstance().visibleEntities( application ),
                                                moduleInstance.layerInstance()
                                                    .usedLayersInstance()
                                                    .visibleEntities() ) ),

                ambiguousTypeCheck( type,
                                    findModels( new AssignableTypeLookupSpecification( type ),
                                                moduleInstance.visibleEntities( module ),
                                                moduleInstance.layerInstance()
                                                    .visibleEntities( layer ),
                                                moduleInstance.layerInstance()
                                                    .visibleEntities( application ),
                                                moduleInstance.layerInstance()
                                                    .usedLayersInstance()
                                                    .visibleEntities() ) ) );

            model = (ModelModule<EntityModel>) models.findFirst().orElse( null );

            if( model != null )
            {
                unambiguousEntityModels.put( type, model );
            }
        }

        return model;
    }

    /**
     * Lookup all Entity Models matching the given Type.
     *
     * <p>Returned Iterable contains, in order, Entity Models that: </p>
     *
     * <ul>
     * <li>exactly match the given type, in Visibility then Assembly order ;</li>
     * <li>match a type assignable to the given type, in Visibility then Assembly order.</li>
     * </ul>
     *
     * <p>Multiple <b>exact</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     * <p>Multiple <b>assignable</b> matches are <b>allowed</b> to enable polymorphic fetches and queries.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * <p><b>Should be used for non-creational use cases only.</b> For creational use cases see
     * {@link #lookupEntityModel(java.lang.Class)}.</p>
     *
     * @param type Looked up Type
     *
     * @return All matching Entity Models
     */
    /* package */ Iterable<ModelModule<EntityModel>> lookupEntityModels( final Class type )
    {
        List<ModelModule<EntityModel>> result = allEntityModels.get( type );
        if( result == null )
        {
            // Unambiguously and lazily resolve ObjectModel
            Stream<ModelModule<? extends ModelDescriptor>> models = concat(
                ambiguousTypeCheck( type,
                                    findModels( new ExactTypeLookupSpecification( type ),
                                                moduleInstance.visibleEntities( module ),
                                                moduleInstance.layerInstance().visibleEntities( layer ),
                                                moduleInstance.layerInstance().visibleEntities( application ),
                                                moduleInstance.layerInstance()
                                                    .usedLayersInstance()
                                                    .visibleEntities() ) ),

                findModels( new AssignableTypeLookupSpecification( type ),
                            moduleInstance.visibleEntities( module ),
                            moduleInstance.layerInstance().visibleEntities( layer ),
                            moduleInstance.layerInstance().visibleEntities( application ),
                            moduleInstance.layerInstance().usedLayersInstance().visibleEntities() )
            ).distinct();

            result = models.map( m -> (ModelModule<EntityModel>) m ).collect( Collectors.toList() );
            allEntityModels.put( type, result );
        }
        return result;
    }

    /**
     * Lookup first ServiceReference matching the given Type.
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * <p>See {@link #lookupServiceReferences(java.lang.reflect.Type)}.</p>
     *
     * @param <T>         Service Type
     * @param serviceType Looked up Type
     *
     * @return First matching ServiceReference
     */
    /* package */
    @SuppressWarnings( "unchecked" )
    <T> ServiceReference<T> lookupServiceReference( Type serviceType )
    {
        ServiceReference<?> serviceReference = serviceReferences.get( serviceType );
        if( serviceReference == null )
        {
            // Lazily resolve ServiceReference
            serviceReference = first( lookupServiceReferences( serviceType ) );
            if( serviceReference != null )
            {
                serviceReferences.put( serviceType, serviceReference );
            }
        }

        if( serviceReference == null )
        {
            throw new NoSuchServiceException( RAW_CLASS.apply( serviceType ).getName(), moduleInstance.name() );
        }

        return (ServiceReference<T>) serviceReference;
    }

    /**
     * Lookup all ServiceReferences matching the given Type.
     *
     * <p>Returned Iterable contains, in order, ServiceReferences that: </p>
     *
     * <ul>
     * <li>exactly match the given type, in Visibility then Assembly order ;</li>
     * <li>match a type assignable to the given type, in Visibility then Assembly order.</li>
     * </ul>
     *
     * <p>Multiple <b>exact</b> matches with the same Visibility are <b>allowed</b> to enable polymorphic lookup/injection.</p>
     * <p>Multiple <b>assignable</b> matches with the same Visibility are <b>allowed</b> for the very same reason.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * @param <T>         Service Type
     * @param serviceType Looked up Type
     *
     * @return All matching ServiceReferences
     */
    @SuppressWarnings( "unchecked" )
    /* package */ <T> Iterable<ServiceReference<T>> lookupServiceReferences( final Type serviceType )
    {
        Iterable<ServiceReference<?>> serviceRefs = servicesReferences.get( serviceType );
        if( serviceRefs == null )
        {
            // Lazily resolve ServicesReferences
            Stream<ServiceReference<?>> matchingServices = concat(
                findServiceReferences( new ExactTypeLookupSpecification( serviceType ),
                                       moduleInstance.visibleServices( module ),
                                       moduleInstance.layerInstance().visibleServices( layer ),
                                       moduleInstance.layerInstance().visibleServices( application ),
                                       moduleInstance.layerInstance().usedLayersInstance().visibleServices() ),
                findServiceReferences( new AssignableTypeLookupSpecification( serviceType ),
                                       moduleInstance.visibleServices( module ),
                                       moduleInstance.layerInstance().visibleServices( layer ),
                                       moduleInstance.layerInstance().visibleServices( application ),
                                       moduleInstance.layerInstance().usedLayersInstance().visibleServices() )
            ).distinct();
            serviceRefs = matchingServices.collect( Collectors.toList() );
            servicesReferences.put( serviceType, serviceRefs );
        }

        return cast( serviceRefs );
    }

    @SafeVarargs
    private static Stream<ModelModule<? extends ModelDescriptor>> findModels( Predicate<Stream<Class<?>>> specification,
                                                                              Stream<ModelModule<? extends ModelDescriptor>>... models
    )
    {
        Function<ModelModule<? extends ModelDescriptor>, Stream<Class<?>>> function = new ModelModuleTypesFunction();
        Predicate<ModelModule<? extends ModelDescriptor>> spec = Specifications.translate( function, specification );
        Stream<ModelModule<? extends ModelDescriptor>> stream = Stream.of( models ).flatMap( flatten -> flatten );
        return stream.filter( spec );
    }

    @SafeVarargs
    private static Stream<ServiceReference<?>> findServiceReferences( Predicate<Stream<Class<?>>> specification,
                                                                      Stream<ServiceReference<?>>... references
    )
    {
        Predicate<ServiceReference<?>> spec = Specifications.translate( new ServiceReferenceTypesFunction(), specification );
        return Stream.of( references ).flatMap( flatten -> flatten ).filter( spec );
    }

    /**
     * Check if the list of models contains several ones with the same visibility. If yes, then
     * throw an AmbiguousTypeException
     */
    private static Stream<ModelModule<? extends ModelDescriptor>> ambiguousTypeCheck( final Class<?> type,
                                                                                      Stream<ModelModule<? extends ModelDescriptor>> models
    )
    {
        // TODO: Figure out why AmbiguityFinder doesn't implement Function<ModelModule<T>, ModelModule<T>>, when it clearly says it does. Absurd.
        Function<ModelModule<? extends ModelDescriptor>, ModelModule<? extends ModelDescriptor>> ambiguityFinder = new AmbiguityFinder( type );
        return models.map( ambiguityFinder );
    }

    public Stream<Class<?>> allVisibleObjects()
    {
        return concat( moduleInstance.visibleObjects( module ),
                       concat(
                           moduleInstance.layerInstance().visibleObjects( layer ),
                           concat(
                               moduleInstance.layerInstance().visibleObjects( application ),
                               moduleInstance.layerInstance().usedLayersInstance().visibleObjects()
                           )
                       )
        ).flatMap( model -> model.model().types() );
    }

    private static class ModelModuleTypesFunction<T extends ModelDescriptor>
        implements Function<ModelModule<T>, Stream<Class<?>>>
    {

        @Override
        public Stream<Class<?>> apply( ModelModule<T> modelModule )
        {
            return modelModule.model().types();
        }
    }

    private static class ServiceReferenceTypesFunction
        implements Function<ServiceReference<?>, Stream<Class<?>>>
    {

        @Override
        public Stream<Class<?>> apply( ServiceReference<?> serviceReference )
        {
            return serviceReference.types();
        }
    }

    private static final class ExactTypeLookupSpecification
        implements Predicate<Stream<Class<?>>>
    {

        protected final Type lookedUpType;

        private ExactTypeLookupSpecification( Type lookedUpType )
        {
            this.lookedUpType = lookedUpType;
        }

        protected boolean checkClassMatch( Class<?> candidate, Class<?> lookedUpType )
        {
            return candidate.equals( lookedUpType );
        }

        @Override
        public final boolean test( Stream<Class<?>> types )
        {
            if( lookedUpType instanceof Class )
            {
                // Straight class assignability check
                return checkClassMatch( types, (Class) lookedUpType );
            }
            else
            {
                if( lookedUpType instanceof ParameterizedType )
                {
                    // Foo<Bar> check
                    // First check Foo
                    ParameterizedType parameterizedType = (ParameterizedType) lookedUpType;
                    if( !checkClassMatch( types, (Class) parameterizedType.getRawType() ) )
                    {
                        return false;
                    }
                    // Then check Bar
                    return interfacesOf( types ).anyMatch( intf -> intf.equals( lookedUpType ) );
                }
                else if( lookedUpType instanceof WildcardType )
                {
                    return true;
                }
                return false;
            }
        }

        private boolean checkClassMatch( Stream<Class<?>> candidates, Class<?> lookedUpType )
        {
            return candidates.anyMatch( candidate -> checkClassMatch( candidate, lookedUpType ) );
        }
    }

    private static final class AssignableTypeLookupSpecification
        implements Predicate<Stream<Class<?>>>
    {

        protected final Type lookedUpType;

        private AssignableTypeLookupSpecification( Type lookedUpType )
        {
            this.lookedUpType = lookedUpType;
        }

        protected boolean checkClassMatch( Class<?> candidate, Class<?> lookedUpType )
        {
            return !candidate.equals( lookedUpType ) && lookedUpType.isAssignableFrom( candidate );
        }

        @Override
        public final boolean test( Stream<Class<?>> types )
        {
            if( lookedUpType instanceof Class )
            {
                // Straight class assignability check
                return checkClassMatch( types, (Class) lookedUpType );
            }
            else
            {
                if( lookedUpType instanceof ParameterizedType )
                {
                    // Foo<Bar> check
                    // First check Foo
                    ParameterizedType parameterizedType = (ParameterizedType) lookedUpType;
                    if( !checkClassMatch( types, (Class) parameterizedType.getRawType() ) )
                    {
                        return false;
                    }
                    // Then check Bar
                    interfacesOf( types ).anyMatch( intf -> intf.equals( lookedUpType ) );
                }
                else if( lookedUpType instanceof WildcardType )
                {
                    return true;
                }
                return false;
            }
        }

        private boolean checkClassMatch( Stream<Class<?>> candidates, Class<?> lookedUpType )
        {
            return candidates.anyMatch( candidate -> checkClassMatch( candidate, lookedUpType ) );
        }
    }

    private static class AmbiguityFinder<T extends ModelDescriptor>
        implements Function<ModelModule<T>, ModelModule<T>>
    {
        private ModelModule<T> current = null;
        private final Class<?> type;

        private AmbiguityFinder( Class<?> type )
        {
            this.type = type;
        }

        @Override
        public ModelModule<T> apply( ModelModule<T> model )
        {
            if( current != null && !model.equals( current ) )
            {
                if( model.model().visibility() == current.model().visibility() )
                {
                    throw new AmbiguousTypeException( "More than one type matches " + type.getName() + ": " + current + ", " + model + "]" );
                }
            }
            else
            {
                current = model;
            }
            return current;
        }
    }
}
