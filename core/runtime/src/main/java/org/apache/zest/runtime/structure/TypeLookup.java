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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.AmbiguousTypeException;
import org.apache.zest.api.composite.ModelDescriptor;
import org.apache.zest.api.composite.TransientDescriptor;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.object.ObjectDescriptor;
import org.apache.zest.api.service.NoSuchServiceException;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.value.ValueDescriptor;
import org.apache.zest.spi.module.ModelModule;

import static java.util.stream.Stream.concat;
import static org.apache.zest.api.common.Visibility.application;
import static org.apache.zest.api.common.Visibility.layer;
import static org.apache.zest.api.common.Visibility.module;
import static org.apache.zest.api.util.Classes.RAW_CLASS;
import static org.apache.zest.api.util.Classes.interfacesOf;
import static org.apache.zest.functional.Iterables.first;

/**
 * Central place for Composite Type lookups.
 */
public class TypeLookup
{

    // Constructor parameters
    private final ModuleInstance moduleInstance;
    // Eager instance objects
    private final Map<Class<?>, ModelModule<ObjectDescriptor>> objectModels;
    private final Map<Class<?>, ModelModule<TransientDescriptor>> transientModels;
    private final Map<Class<?>, ModelModule<ValueDescriptor>> valueModels;
    private final Map<Class<?>, List<ModelModule<EntityDescriptor>>> allEntityModels;
    private final Map<Class<?>, ModelModule<EntityDescriptor>> unambiguousEntityModels;
    private final Map<Class, ServiceReference<?>> serviceReferences;
    private final Map<Class, List<ServiceReference<?>>> servicesReferences;

    /**
     * Create a new TypeLookup bound to the given ModuleInstance.
     *
     * @param moduleInstance ModuleInstance bound to this TypeLookup
     */
    TypeLookup( ModuleInstance moduleInstance )
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
    ModelModule<ObjectDescriptor> lookupObjectModel( final Class type )
    {
        ModelModule<ObjectDescriptor> model = objectModels.get( type );
        if( model == null )
        {
            List<ModelModule<ObjectDescriptor>> allModels = allObjects().collect( Collectors.toList() );
            model = ambiguityMatching( type, allModels, new ExactTypeMatching<>( type ) );
            if( model == null )
            {
                model = ambiguityMatching( type, allModels, new AssignableFromTypeMatching<>( type ) );
            }
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
    ModelModule<TransientDescriptor> lookupTransientModel( final Class type )
    {
        ModelModule<TransientDescriptor> model = transientModels.get( type );
        if( model == null )
        {
            List<ModelModule<TransientDescriptor>> allModels = allTransients().collect( Collectors.toList() );
            model = ambiguityMatching( type, allModels, new ExactTypeMatching<>( type ) );
            if( model == null )
            {
                model = ambiguityMatching( type, allModels, new AssignableFromTypeMatching<>( type ) );
            }
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
    public ModelModule<ValueDescriptor> lookupValueModel( final Class type )
    {
        ModelModule<ValueDescriptor> model = valueModels.get( type );
        if( model == null )
        {
            List<ModelModule<ValueDescriptor>> allModels = allValues().collect( Collectors.toList() );
            model = ambiguityMatching( type, allModels, new ExactTypeMatching<>( type ) );
            if( model == null )
            {
                model = ambiguityMatching( type, allModels, new AssignableFromTypeMatching<>( type ) );
            }
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
    ModelModule<EntityDescriptor> lookupEntityModel( final Class type )
    {
        ModelModule<EntityDescriptor> model = unambiguousEntityModels.get( type );

        if( model == null )
        {
            List<ModelModule<EntityDescriptor>> allModels = allEntities().collect( Collectors.toList() );
            model = ambiguityMatching( type, allModels, new ExactTypeMatching<>( type ) );
            if( model == null )
            {
                model = ambiguityMatching( type, allModels, new AssignableFromTypeMatching<>( type ) );
            }
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
    Iterable<ModelModule<EntityDescriptor>> lookupEntityModels( final Class type )
    {
        List<ModelModule<EntityDescriptor>> result = allEntityModels.get( type );
        if( result == null )
        {
            result = concat(
                allEntities().filter( new ExactTypeMatching<>( type ) ),
                allEntities().filter( new AssignableFromTypeMatching<>( type ) )
            ).distinct().collect( Collectors.toList() );
            allEntityModels.put( type, result );
        }
        return result;
    }

    /**
     * Lookup first ServiceReference matching the given Type.
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * <p>See {@link #lookupServiceReferences(Type)}.</p>
     *
     * @param <T>         Service Type
     * @param serviceType Looked up Type
     *
     * @return First matching ServiceReference
     */
    <T> ServiceReference<T> lookupServiceReference( Type serviceType )
    {
        @SuppressWarnings( "unchecked" )
        ServiceReference<T> serviceReference = (ServiceReference<T>) serviceReferences.get( serviceType );
        if( serviceReference == null )
        {
            // Lazily resolve ServiceReference
            serviceReference = first( lookupServiceReferences( serviceType ) );
            if( serviceReference != null )
            {
                serviceReferences.put( (Class) serviceType, serviceReference );
            }
        }

        if( serviceReference == null )
        {
            throw new NoSuchServiceException( RAW_CLASS.apply( serviceType ).getName(), moduleInstance.name() );
        }
        return serviceReference;
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
     * @param <T>  Service Type
     * @param type Looked up Type
     *
     * @return All matching ServiceReferences
     */
    <T> List<ServiceReference<T>> lookupServiceReferences( final Type type )
    {
        List<ServiceReference<?>> serviceRefs = servicesReferences.get( type );
        if( serviceRefs == null )
        {
            serviceRefs = concat(
                allServices()
                    .filter( ref -> ref.types().anyMatch( clazz -> clazz.equals( type ) ) ),
                allServices()
                    .filter( ref -> ref.types().anyMatch(
                                 t -> !( t.equals( type ) ) && ((Class)type).isAssignableFrom( t ) )
                    )
            ).distinct().collect( Collectors.toList() );
            servicesReferences.put( (Class) type, serviceRefs );
        }
        List<ServiceReference<T>> result = new ArrayList<>();
        //noinspection unchecked
        serviceRefs.forEach( ref -> result.add( (ServiceReference<T>) ref ) );
        return result;
    }

    public Stream<Class<?>> allVisibleObjects()
    {
        return allObjects().flatMap( model -> model.model().types() );
    }

    private Stream<ModelModule<ObjectDescriptor>> allObjects()
    {
        return concat( moduleInstance.visibleObjects( module ),
                       concat(
                           moduleInstance.layerInstance().visibleObjects( layer ),
                           concat(
                               moduleInstance.layerInstance().visibleObjects( application ),
                               moduleInstance.layerInstance().usedLayersInstance().visibleObjects()
                           )
                       )
        );
    }

    private Stream<ModelModule<TransientDescriptor>> allTransients()
    {
        return concat( moduleInstance.visibleTransients( module ),
                       concat(
                           moduleInstance.layerInstance().visibleTransients( layer ),
                           concat(
                               moduleInstance.layerInstance().visibleTransients( application ),
                               moduleInstance.layerInstance().usedLayersInstance().visibleTransients()
                           )
                       )
        );
    }

    private Stream<ModelModule<ValueDescriptor>> allValues()
    {
        return concat( moduleInstance.visibleValues( module ),
                       concat(
                           moduleInstance.layerInstance().visibleValues( layer ),
                           concat(
                               moduleInstance.layerInstance().visibleValues( application ),
                               moduleInstance.layerInstance().usedLayersInstance().visibleValues()
                           )
                       )
        );
    }

    private Stream<ModelModule<EntityDescriptor>> allEntities()
    {
        return concat( moduleInstance.visibleEntities( module ),
                       concat(
                           moduleInstance.layerInstance().visibleEntities( layer ),
                           concat(
                               moduleInstance.layerInstance().visibleEntities( application ),
                               moduleInstance.layerInstance().usedLayersInstance().visibleEntities()
                           )
                       )
        );
    }

    private Stream<ServiceReference<?>> allServices()
    {
        return concat( moduleInstance.visibleServices( module ),
                       concat(
                           moduleInstance.layerInstance().visibleServices( layer ),
                           concat(
                               moduleInstance.layerInstance().visibleServices( application ),
                               moduleInstance.layerInstance().usedLayersInstance().visibleServices()
                           )
                       )
        );
    }

    private <T extends ModelDescriptor> ModelModule<T> ambiguityMatching(
        Class type,
        List<ModelModule<T>> modelModules,
        TypeMatching<T> matching
    )
    {
        List<ModelModule<T>> models = modelModules.stream()
            .filter( matching )
            .filter( new SameVisibility<>() )
            .distinct()
            .collect( Collectors.toList() );

        if( models.size() > 1 )
        {
            throw new AmbiguousTypeException( "More than one type matches " + type.getName() + ": " + models + "]" );
        }
        if( models.isEmpty() )
        {
            return null;
        }
        return models.get( 0 );
    }

    private static abstract class TypeMatching<T extends ModelDescriptor>
        implements Predicate<ModelModule<T>>
    {
        protected final Type lookedUpType;

        protected TypeMatching( Type lookedUpType )
        {
            this.lookedUpType = lookedUpType;
        }

        @Override
        public final boolean test( ModelModule<T> model )
        {
            if( lookedUpType instanceof Class )
            {
                return model.model().types().anyMatch( checkMatch( lookedUpType ) );
            }
            else
            {
                if( lookedUpType instanceof ParameterizedType )
                {
                    // Foo<Bar> check
                    // First check Foo
                    ParameterizedType parameterizedType = (ParameterizedType) lookedUpType;
                    Type rawType = parameterizedType.getRawType();
                    if( !model.model().types().anyMatch( checkMatch( rawType ) ) )
                    {
                        return false;
                    }
                    // Then check Bar
                    return interfacesOf( model.model().types() ).anyMatch( intf -> intf.equals( lookedUpType ) );
                }
                else if( lookedUpType instanceof WildcardType )
                {
                    return true;
                }
                return false;
            }
        }

        protected abstract Predicate<Type> checkMatch( Type matchTo );
    }

    private static final class ExactTypeMatching<T extends ModelDescriptor> extends TypeMatching<T>
    {
        private ExactTypeMatching( Type lookedUpType )
        {
            super( lookedUpType );
        }

        protected Predicate<Type> checkMatch( Type matchTo )
        {
            return matchTo::equals;
        }
    }

    private static final class AssignableFromTypeMatching<T extends ModelDescriptor> extends TypeMatching<T>
    {
        private AssignableFromTypeMatching( Type lookedUpType )
        {
            super( lookedUpType );
        }

        protected Predicate<Type> checkMatch( Type matchTo )
        {
            // TODO; what to do if there is ParameterizedType here?? Now set to ClassCastException and see if anything surfaces
//            if( matchTo instanceof Class )
            {
                Class<?> clazz = (Class<?>) matchTo;
                return candidate ->
                    !candidate.equals( matchTo ) && clazz.isAssignableFrom( (Class<?>) candidate );
            }
//            return candidate -> candidate.equals( matchTo );
        }
    }

    /**
     * This Predicate will filter out all Models that doesn't have the same visisbility as the first one.
     */
    private class SameVisibility<T extends ModelDescriptor>
        implements Predicate<ModelModule<T>>
    {
        private Visibility current = null;

        @Override
        public boolean test( ModelModule<T> model )
        {
            if( current == null )
            {
                current = model.model().visibility();
                return true;
            }
            return current == model.model().visibility();
        }
    }
}
