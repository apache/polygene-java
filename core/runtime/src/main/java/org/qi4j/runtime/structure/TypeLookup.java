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
package org.qi4j.runtime.structure;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.api.service.NoSuchServiceException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.functional.Function;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;
import org.qi4j.runtime.composite.TransientModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.value.ValueModel;

import static org.qi4j.api.util.Classes.RAW_CLASS;
import static org.qi4j.api.util.Classes.interfacesOf;
import static org.qi4j.functional.Iterables.*;

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
    private final Map<Class<?>, Iterable<ModelModule<EntityModel>>> allEntityModels;
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
        objectModels = new ConcurrentHashMap<Class<?>, ModelModule<ObjectModel>>();
        transientModels = new ConcurrentHashMap<Class<?>, ModelModule<TransientModel>>();
        valueModels = new ConcurrentHashMap<Class<?>, ModelModule<ValueModel>>();
        allEntityModels = new ConcurrentHashMap<Class<?>, Iterable<ModelModule<EntityModel>>>();
        unambiguousEntityModels = new ConcurrentHashMap<Class<?>, ModelModule<EntityModel>>();
        serviceReferences = new ConcurrentHashMap<Type, ServiceReference<?>>();
        servicesReferences = new ConcurrentHashMap<Type, Iterable<ServiceReference<?>>>();
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
     * @param type  Looked up Type
     * @return      First matching Object Model
     */
    /* package */ ModelModule<ObjectModel> lookupObjectModel( final Class type )
    {
        ModelModule<ObjectModel> model = objectModels.get( type );

        if( model == null )
        {
            // Unambiguously and lazily resolve ObjectModel
            Iterable<ModelModule<ObjectModel>> flatten = flatten(
                ambiguousTypeCheck( type,
                                    findModels( new ExactTypeLookupSpecification( type ),
                                                moduleInstance.visibleObjects( Visibility.module ),
                                                moduleInstance.layerInstance().visibleObjects( Visibility.layer ),
                                                moduleInstance.layerInstance().visibleObjects( Visibility.application ),
                                                moduleInstance.layerInstance().usedLayersInstance().visibleObjects() ) ),
                ambiguousTypeCheck( type,
                                    findModels( new AssignableTypeLookupSpecification( type ),
                                                moduleInstance.visibleObjects( Visibility.module ),
                                                moduleInstance.layerInstance().visibleObjects( Visibility.layer ),
                                                moduleInstance.layerInstance().visibleObjects( Visibility.application ),
                                                moduleInstance.layerInstance().usedLayersInstance().visibleObjects() ) ) );

            model = first( flatten );

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
     * @param type  Looked up Type
     * @return      First matching Transient Model
     */
    /* package */ ModelModule<TransientModel> lookupTransientModel( final Class type )
    {
        ModelModule<TransientModel> model = transientModels.get( type );

        if( model == null )
        {
            // Unambiguously and lazily resolve TransientModel
            Iterable<ModelModule<TransientModel>> allModels = flatten(
                ambiguousTypeCheck( type,
                                    findModels( new ExactTypeLookupSpecification( type ),
                                                moduleInstance.visibleTransients( Visibility.module ),
                                                moduleInstance.layerInstance().visibleTransients( Visibility.layer ),
                                                moduleInstance.layerInstance().visibleTransients( Visibility.application ),
                                                moduleInstance.layerInstance().usedLayersInstance().visibleTransients() ) ),
                ambiguousTypeCheck( type,
                                    findModels( new AssignableTypeLookupSpecification( type ),
                                                moduleInstance.visibleTransients( Visibility.module ),
                                                moduleInstance.layerInstance().visibleTransients( Visibility.layer ),
                                                moduleInstance.layerInstance().visibleTransients( Visibility.application ),
                                                moduleInstance.layerInstance().usedLayersInstance().visibleTransients() ) ) );
            model = first( allModels );

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
     * @param type  Looked up Type
     * @return      First matching Value Model
     */
    public ModelModule<ValueModel> lookupValueModel( final Class type )
    {
        ModelModule<ValueModel> model = valueModels.get( type );

        if( model == null )
        {
            // Unambiguously and lazily resolve ValueModel
            Iterable<ModelModule<ValueModel>> flatten = flatten(
                ambiguousTypeCheck( type,
                                    findModels( new ExactTypeLookupSpecification( type ),
                                                moduleInstance.visibleValues( Visibility.module ),
                                                moduleInstance.layerInstance().visibleValues( Visibility.layer ),
                                                moduleInstance.layerInstance().visibleValues( Visibility.application ),
                                                moduleInstance.layerInstance().usedLayersInstance().visibleValues() ) ),
                ambiguousTypeCheck( type,
                                    findModels( new AssignableTypeLookupSpecification( type ),
                                                moduleInstance.visibleValues( Visibility.module ),
                                                moduleInstance.layerInstance().visibleValues( Visibility.layer ),
                                                moduleInstance.layerInstance().visibleValues( Visibility.application ),
                                                moduleInstance.layerInstance().usedLayersInstance().visibleValues() ) ) );

            model = first( flatten );

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
     * @param type  Looked up Type
     * @return      First matching Entity Model
     */
    /* package */ ModelModule<EntityModel> lookupEntityModel( final Class type )
    {
        ModelModule<EntityModel> model = unambiguousEntityModels.get( type );

        if( model == null )
        {
            // Unambiguously and lazily resolve EntityModels
            Iterable<ModelModule<EntityModel>> allModels = flatten(
                ambiguousTypeCheck( type,
                                    findModels( new ExactTypeLookupSpecification( type ),
                                                moduleInstance.visibleEntities( Visibility.module ),
                                                moduleInstance.layerInstance().visibleEntities( Visibility.layer ),
                                                moduleInstance.layerInstance().visibleEntities( Visibility.application ),
                                                moduleInstance.layerInstance().usedLayersInstance().visibleEntities() ) ),
                ambiguousTypeCheck( type,
                                    findModels( new AssignableTypeLookupSpecification( type ),
                                                moduleInstance.visibleEntities( Visibility.module ),
                                                moduleInstance.layerInstance().visibleEntities( Visibility.layer ),
                                                moduleInstance.layerInstance().visibleEntities( Visibility.application ),
                                                moduleInstance.layerInstance().usedLayersInstance().visibleEntities() ) ) );

            model = first( allModels );

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
     *  <li>exactly match the given type, in Visibility then Assembly order ;</li>
     *  <li>match a type assignable to the given type, in Visibility then Assembly order.</li>
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
     * @param type  Looked up Type
     * @return      All matching Entity Models
     */
    /* package */ Iterable<ModelModule<EntityModel>> lookupEntityModels( final Class type )
    {
        Iterable<ModelModule<EntityModel>> models = allEntityModels.get( type );
        if( models == null )
        {
            // Ambiguously and lasily resolve EntityModels
            Iterable<ModelModule<EntityModel>> matchingEntityModels = flatten(
                ambiguousTypeCheck( type,
                                    findModels( new ExactTypeLookupSpecification( type ),
                                                moduleInstance.visibleEntities( Visibility.module ),
                                                moduleInstance.layerInstance().visibleEntities( Visibility.layer ),
                                                moduleInstance.layerInstance().visibleEntities( Visibility.application ),
                                                moduleInstance.layerInstance().usedLayersInstance().visibleEntities() ) ),
                findModels( new AssignableTypeLookupSpecification( type ),
                            moduleInstance.visibleEntities( Visibility.module ),
                            moduleInstance.layerInstance().visibleEntities( Visibility.layer ),
                            moduleInstance.layerInstance().visibleEntities( Visibility.application ),
                            moduleInstance.layerInstance().usedLayersInstance().visibleEntities() ) );

            // Don't return the same EntityModel multiple times
            matchingEntityModels = unique( matchingEntityModels );

            models = toList( matchingEntityModels );
            allEntityModels.put( type, models );
        }
        return models;
    }

    /**
     * Lookup first ServiceReference matching the given Type.
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * <p>See {@link #lookupServiceReferences(java.lang.reflect.Type)}.</p>
     *
     * @param <T>           Service Type
     * @param serviceType   Looked up Type
     * @return              First matching ServiceReference
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
            throw new NoSuchServiceException( RAW_CLASS.map( serviceType ).getName(), moduleInstance.name() );
        }

        return (ServiceReference<T>) serviceReference;
    }

    /**
     * Lookup all ServiceReferences matching the given Type.
     *
     * <p>Returned Iterable contains, in order, ServiceReferences that: </p>
     *
     * <ul>
     *  <li>exactly match the given type, in Visibility then Assembly order ;</li>
     *  <li>match a type assignable to the given type, in Visibility then Assembly order.</li>
     * </ul>
     *
     * <p>Multiple <b>exact</b> matches with the same Visibility are <b>allowed</b> to enable polymorphic lookup/injection.</p>
     * <p>Multiple <b>assignable</b> matches with the same Visibility are <b>allowed</b> for the very same reason.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * @param <T>           Service Type
     * @param serviceType   Looked up Type
     * @return              All matching ServiceReferences
     */
    /* package */ <T> Iterable<ServiceReference<T>> lookupServiceReferences( final Type serviceType )
    {
        Iterable<ServiceReference<?>> serviceRefs = servicesReferences.get( serviceType );
        if( serviceRefs == null )
        {
            // Lazily resolve ServicesReferences
            Iterable<ServiceReference<?>> matchingServices = flatten(
                findServiceReferences( new ExactTypeLookupSpecification( serviceType ),
                                       moduleInstance.visibleServices( Visibility.module ),
                                       moduleInstance.layerInstance().visibleServices( Visibility.layer ),
                                       moduleInstance.layerInstance().visibleServices( Visibility.application ),
                                       moduleInstance.layerInstance().usedLayersInstance().visibleServices() ),
                findServiceReferences( new AssignableTypeLookupSpecification( serviceType ),
                                       moduleInstance.visibleServices( Visibility.module ),
                                       moduleInstance.layerInstance().visibleServices( Visibility.layer ),
                                       moduleInstance.layerInstance().visibleServices( Visibility.application ),
                                       moduleInstance.layerInstance().usedLayersInstance().visibleServices() ) );

            // Don't return the same ServiceReference multiple times
            matchingServices = unique( matchingServices );

            serviceRefs = toList( matchingServices );
            servicesReferences.put( serviceType, serviceRefs );
        }

        return cast( serviceRefs );
    }

    private static <T extends ModelDescriptor> Iterable<ModelModule<T>> findModels( Specification<Iterable<Class<?>>> specification,
                                                                                    Iterable<ModelModule<T>>... models )
    {
        Specification<ModelModule<T>> spec = Specifications.translate( new ModelModuleTypesFunction(), specification );
        Iterable<ModelModule<T>> flattened = flattenIterables( iterable( models ) );
        return filter( spec, flattened );
    }

    private static Iterable<ServiceReference<?>> findServiceReferences( Specification<Iterable<Class<?>>> specification,
                                                                        Iterable<ServiceReference<?>>... references )
    {
        Specification<ServiceReference<?>> spec = Specifications.translate( new ServiceReferenceTypesFunction(), specification );
        Iterable<ServiceReference<?>> flattened = flattenIterables( iterable( references ) );
        return filter( spec, flattened );
    }

    /**
     * Check if the list of models contains several ones with the same visibility. If yes, then
     * throw an AmbiguousTypeException
     */
    private static <T extends ModelDescriptor> Iterable<ModelModule<T>> ambiguousTypeCheck( final Class type,
                                                                                            final Iterable<ModelModule<T>> models )
    {
        return new Iterable<ModelModule<T>>()
        {

            @Override
            public Iterator<ModelModule<T>> iterator()
            {
                ModelModule<T> current = null;
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

    private static class ModelModuleTypesFunction<T extends ModelDescriptor>
        implements Function<ModelModule<T>, Iterable<Class<?>>>
    {

        @Override
        public Iterable<Class<?>> map( ModelModule<T> modelModule )
        {
            return modelModule.model().types();
        }

    }

    private static class ServiceReferenceTypesFunction
        implements Function<ServiceReference<?>, Iterable<Class<?>>>
    {

        @Override
        public Iterable<Class<?>> map( ServiceReference<?> serviceReference )
        {
            return serviceReference.types();
        }

    }

    private static abstract class AbstractTypeLookupSpecification
        implements Specification<Iterable<Class<?>>>
    {

        protected final Type lookedUpType;

        private AbstractTypeLookupSpecification( Type lookedUpType )
        {
            this.lookedUpType = lookedUpType;
        }

        @Override
        public final boolean satisfiedBy( Iterable<Class<?>> types )
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
                    for( Type intf : interfacesOf( types ) )
                    {
                        if( intf.equals( lookedUpType ) )
                        {
                            // All parameters are the same - ok!
                            return true;
                        }
                    }
                    return false;
                }
                else if( lookedUpType instanceof WildcardType )
                {
                    return true;
                }
                return false;
            }
        }

        private boolean checkClassMatch( Iterable<Class<?>> candidates, Class<?> lookedUpType )
        {
            for( Class<?> candidate : candidates )
            {
                if( checkClassMatch( candidate, lookedUpType ) )
                {
                    return true;
                }
            }
            return false;
        }

        protected abstract boolean checkClassMatch( Class<?> candidate, Class<?> lookedUpType );

    }

    private static final class ExactTypeLookupSpecification
        extends AbstractTypeLookupSpecification
    {

        private ExactTypeLookupSpecification( Type lookedupType )
        {
            super( lookedupType );
        }

        @Override
        protected boolean checkClassMatch( Class<?> candidate, Class<?> lookedUpType )
        {
            return candidate.equals( lookedUpType );
        }

    }

    private static final class AssignableTypeLookupSpecification
        extends AbstractTypeLookupSpecification
    {

        private AssignableTypeLookupSpecification( Type lookedupType )
        {
            super( lookedupType );
        }

        @Override
        protected boolean checkClassMatch( Class<?> candidate, Class<?> lookedUpType )
        {
            return !candidate.equals( lookedUpType ) && lookedUpType.isAssignableFrom( candidate );
        }

    }

}
