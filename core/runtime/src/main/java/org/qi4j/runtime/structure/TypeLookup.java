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
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.functional.Function;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;
import org.qi4j.runtime.composite.TransientModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.value.ValueModel;

import static org.qi4j.api.util.Classes.*;
import static org.qi4j.functional.Iterables.*;

public class TypeLookup
    implements ServiceFinder
{

    private final Map<Class, ModelModule<ObjectModel>> objectModels = new ConcurrentHashMap<Class, ModelModule<ObjectModel>>();
    private final Map<Class, ModelModule<TransientModel>> transientModels = new ConcurrentHashMap<Class, ModelModule<TransientModel>>();
    private final Map<Class, Iterable<ModelModule<EntityModel>>> entityModels = new ConcurrentHashMap<Class, Iterable<ModelModule<EntityModel>>>();
    private final Map<Class, ModelModule<ValueModel>> valueModels = new ConcurrentHashMap<Class, ModelModule<ValueModel>>();
    private final Map<Type, ServiceReference> serviceReferences = new ConcurrentHashMap<Type, ServiceReference>();
    private final Map<Type, Iterable<ServiceReference>> servicesReferences = new ConcurrentHashMap<Type, Iterable<ServiceReference>>();
    private final ModuleInstance moduleInstance;

    /* package */ TypeLookup( ModuleInstance moduleInstance )
    {
        this.moduleInstance = moduleInstance;
    }

    /* package */ Iterable<ModelModule<EntityModel>> findEntityModels( final Class type )
    {
        Iterable<ModelModule<EntityModel>> models = entityModels.get( type );

        if( models == null )
        {
            // Lazily resolve EntityModels
            models = flatten(
                ambiguousCheck( type,
                                findModels( exactTypeSpecification( type ),
                                            moduleInstance.visibleEntities( Visibility.module ),
                                            moduleInstance.layerInstance().visibleEntities( Visibility.layer ),
                                            moduleInstance.layerInstance().visibleEntities( Visibility.application ),
                                            moduleInstance.layerInstance().usedLayersInstance().visibleEntities() ) ),
                /* ambiguousCheck( type, */
                findModels( assignableTypeSpecification( type ),
                            moduleInstance.visibleEntities( Visibility.module ),
                            moduleInstance.layerInstance().visibleEntities( Visibility.layer ),
                            moduleInstance.layerInstance().visibleEntities( Visibility.application ),
                            moduleInstance.layerInstance().usedLayersInstance().visibleEntities() ) ) /*)*/;

            entityModels.put( type, models );
        }

        return models;
    }

    /* package */ ModelModule<TransientModel> findTransientModels( final Class type )
    {
        ModelModule<TransientModel> model = transientModels.get( type );

        if( model == null )
        {
            // Lazily resolve TransientModel
            Iterable<ModelModule<TransientModel>> flatten = flatten(
                ambiguousCheck( type,
                                findModels( exactTypeSpecification( type ),
                                            moduleInstance.visibleTransients( Visibility.module ),
                                            moduleInstance.layerInstance().visibleTransients( Visibility.layer ),
                                            moduleInstance.layerInstance().visibleTransients( Visibility.application ),
                                            moduleInstance.layerInstance().usedLayersInstance().visibleTransients() ) ),
                ambiguousCheck( type,
                                findModels( assignableTypeSpecification( type ),
                                            moduleInstance.visibleTransients( Visibility.module ),
                                            moduleInstance.layerInstance().visibleTransients( Visibility.layer ),
                                            moduleInstance.layerInstance().visibleTransients( Visibility.application ),
                                            moduleInstance.layerInstance().usedLayersInstance().visibleTransients() ) ) );
            model = first( flatten );

            if( model != null )
            {
                transientModels.put( type, model );
            }
        }

        return model;
    }

    /* package */ ModelModule<ObjectModel> findObjectModels( final Class type )
    {
        ModelModule<ObjectModel> model = objectModels.get( type );

        if( model == null )
        {
            // Lazily resolve ObjectModel
            Iterable<ModelModule<ObjectModel>> flatten = flatten(
                ambiguousCheck( type,
                                findModels( exactTypeSpecification( type ),
                                            moduleInstance.visibleObjects( Visibility.module ),
                                            moduleInstance.layerInstance().visibleObjects( Visibility.layer ),
                                            moduleInstance.layerInstance().visibleObjects( Visibility.application ),
                                            moduleInstance.layerInstance().usedLayersInstance().visibleObjects() ) ),
                ambiguousCheck( type,
                                findModels( assignableTypeSpecification( type ),
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

    public ModelModule<ValueModel> findValueModels( final Class type )
    {
        ModelModule<ValueModel> model = valueModels.get( type );

        if( model == null )
        {
            // Lazily resolve ValueModel
            Iterable<ModelModule<ValueModel>> flatten = flatten(
                ambiguousCheck( type,
                                findModels( exactTypeSpecification( type ),
                                            moduleInstance.visibleValues( Visibility.module ),
                                            moduleInstance.layerInstance().visibleValues( Visibility.layer ),
                                            moduleInstance.layerInstance().visibleValues( Visibility.application ),
                                            moduleInstance.layerInstance().usedLayersInstance().visibleValues() ) ),
                ambiguousCheck( type,
                                findModels( assignableTypeSpecification( type ),
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

    @Override
    public <T> ServiceReference<T> findService( Class<T> serviceType )
    {
        return findService( (Type) serviceType );
    }

    @Override
    public <T> ServiceReference<T> findService( Type serviceType )
    {
        ServiceReference serviceReference = serviceReferences.get( serviceType );
        if( serviceReference == null )
        {
            // Lazily resolve ServiceReference
            serviceReference = first( findServices( serviceType ) );
            if( serviceReference != null )
            {
                serviceReferences.put( serviceType, serviceReference );
            }
        }

        if( serviceReference == null )
        {
            throw new NoSuchServiceException( RAW_CLASS.map( serviceType ).getName(), moduleInstance.name() );
        }

        return serviceReference;
    }

    @Override
    public <T> Iterable<ServiceReference<T>> findServices( Class<T> serviceType )
    {
        return findServices( (Type) serviceType );
    }

    @Override
    public <T> Iterable<ServiceReference<T>> findServices( final Type serviceType )
    {
        Iterable<ServiceReference> iterable = servicesReferences.get( serviceType );
        if( iterable == null )
        {
            // Lazily resolve ServicesReferences
            Function<ServiceReference, Iterable<Class<?>>> referenceTypesFunction = new Function<ServiceReference, Iterable<Class<?>>>()
            {

                @Override
                public Iterable<Class<?>> map( ServiceReference serviceReference )
                {
                    return serviceReference.types();
                }

            };

            Specification<Iterable<Class<?>>> typeSpecification = new Specification<Iterable<Class<?>>>()
            {

                @Override
                public boolean satisfiedBy( Iterable<Class<?>> types )
                {
                    if( serviceType instanceof Class )
                    {
                        // Straight class assignability check
                        return checkClassMatch( types, (Class) serviceType );
                    }
                    else
                    {
                        if( serviceType instanceof ParameterizedType )
                        {
                            // Foo<Bar> check
                            // First check Foo
                            ParameterizedType parameterizedType = (ParameterizedType) serviceType;
                            if( !checkClassMatch( types, (Class) parameterizedType.getRawType() ) )
                            {
                                return false;
                            }

                            // Then check Bar
                            for( Type intf : interfacesOf( types ) )
                            {
                                if( intf.equals( serviceType ) )
                                {
                                    return true;
                                }
                            }

                            // All parameters are the same - ok!
                            return false;
                        }
                        else
                        {
                            if( serviceType instanceof WildcardType )
                            {
                                return true;

                            }
                            else
                            {
                                return false;
                            }
                        }
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

            Specification<ServiceReference> referenceTypeCheck = Specifications.translate( referenceTypesFunction, typeSpecification );

            Iterable<ServiceReference> matchingServices = flatten(
                filter( referenceTypeCheck, moduleInstance.visibleServices( Visibility.module ) ),
                filter( referenceTypeCheck, moduleInstance.layerInstance().visibleServices( Visibility.layer ) ),
                filter( referenceTypeCheck, moduleInstance.layerInstance().visibleServices( Visibility.application ) ),
                filter( referenceTypeCheck, moduleInstance.layerInstance().usedLayersInstance().visibleServices() ) );

            // Don't return the same ServiceReference multiple times
            matchingServices = unique( matchingServices );

            iterable = toList( matchingServices );
            servicesReferences.put( serviceType, iterable );
        }

        return cast( iterable );
    }

    private <T extends ModelDescriptor> Iterable<ModelModule<T>> findModels( Specification<? super T> specification,
                                                                             Iterable<ModelModule<T>>... models )
    {
        Specification<ModelModule<T>> spec = Specifications.translate( ModelModule.<T>modelFunction(), specification );
        Iterable<ModelModule<T>> flatten = flattenIterables( iterable( models ) );
        return filter( spec, flatten );
    }

    /**
     * Check if the list of models contains several ones with the same visibility. If yes, then
     * throw an AmbiguousTypeException
     */
    private <T extends ModelDescriptor> Iterable<ModelModule<T>> ambiguousCheck( final Class type,
                                                                                 final Iterable<ModelModule<T>> models )
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

}
