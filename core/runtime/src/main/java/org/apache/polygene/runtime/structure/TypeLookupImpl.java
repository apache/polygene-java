/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.runtime.structure;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.composite.AmbiguousTypeException;
import org.apache.polygene.api.composite.ModelDescriptor;
import org.apache.polygene.api.composite.TransientDescriptor;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.object.ObjectDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.structure.TypeLookup;
import org.apache.polygene.api.type.HasTypes;
import org.apache.polygene.api.value.ValueDescriptor;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.apache.polygene.api.common.Visibility.application;
import static org.apache.polygene.api.common.Visibility.layer;
import static org.apache.polygene.api.util.Classes.interfacesOf;

/**
 * Central place for Composite Type lookups.
 */
class TypeLookupImpl
    implements TypeLookup
{
    private final LazyValue<List<ObjectDescriptor>> allObjects;
    private final LazyValue<List<TransientDescriptor>> allTransients;
    private final LazyValue<List<ValueDescriptor>> allValues;
    private final LazyValue<List<EntityDescriptor>> allEntities;
    private final LazyValue<List<? extends ModelDescriptor>> allServices;
    private final ConcurrentHashMap<Class<?>, ObjectDescriptor> objectModels;
    private final ConcurrentHashMap<Class<?>, TransientDescriptor> transientModels;
    private final ConcurrentHashMap<Class<?>, ValueDescriptor> valueModels;
    private final ConcurrentHashMap<Class<?>, List<EntityDescriptor>> entityModels;
    private final ConcurrentHashMap<Class<?>, EntityDescriptor> unambiguousEntityModels;
    private final ConcurrentHashMap<Type, ModelDescriptor> serviceModels;
    private final ConcurrentHashMap<Type, List<? extends ModelDescriptor>> servicesReferences;

    private final ModuleDescriptor moduleModel;

    /**
     * Create a new TypeLookup bound to the given ModuleModel.
     *
     * @param module ModuleModel bound to this TypeLookup
     */
    TypeLookupImpl( ModuleModel module )
    {
        moduleModel = module;

        // Instance caches
        allObjects = new LazyValue<>();
        allTransients = new LazyValue<>();
        allValues = new LazyValue<>();
        allEntities = new LazyValue<>();
        allServices = new LazyValue<>();
        objectModels = new ConcurrentHashMap<>();
        transientModels = new ConcurrentHashMap<>();
        valueModels = new ConcurrentHashMap<>();
        entityModels = new ConcurrentHashMap<>();
        unambiguousEntityModels = new ConcurrentHashMap<>();
        serviceModels = new ConcurrentHashMap<>();
        servicesReferences = new ConcurrentHashMap<>();
    }

    @Override
    public ObjectDescriptor lookupObjectModel( final Class<?> type )
    {
        return objectModels.computeIfAbsent( type, key ->
        {
            List<? extends ObjectDescriptor> allModels = getAllObjects();
            ObjectDescriptor model = ambiguityMatching( key, allModels, new ExactTypeMatching<>( key ) );
            if( model == null )
            {
                model = ambiguityMatching( key, allModels, new AssignableFromTypeMatching<>( key ) );
            }
            return model;
        } );
    }

    @Override
    public TransientDescriptor lookupTransientModel( final Class<?> type )
    {
        return transientModels.computeIfAbsent( type, key ->
        {
            List<? extends TransientDescriptor> allModels = getAllTransients();
            TransientDescriptor model = ambiguityMatching( key, allModels, new ExactTypeMatching<>( key ) );
            if( model == null )
            {
                model = ambiguityMatching( key, allModels, new AssignableFromTypeMatching<>( key ) );
            }
            return model;
        } );
    }

    @Override
    public ValueDescriptor lookupValueModel( final Class<?> type )
    {
        return valueModels.computeIfAbsent( type, key ->
        {
            List<? extends ValueDescriptor> allModels = getAllValues();
            ValueDescriptor model = ambiguityMatching( key, allModels, new ExactTypeMatching<>( key ) );
            if( model == null )
            {
                model = ambiguityMatching( key, allModels, new AssignableFromTypeMatching<>( key ) );
            }
            return model;
        } );
    }

    @Override
    public EntityDescriptor lookupEntityModel( final Class<?> type )
    {
        return unambiguousEntityModels.computeIfAbsent( type, key ->
        {
            List<? extends EntityDescriptor> allModels = getAllEntities();
            EntityDescriptor model = ambiguityMatching( key, allModels, new ExactTypeMatching<>( key ) );
            if( model == null )
            {
                model = ambiguityMatching( key, allModels, new AssignableFromTypeMatching<>( key ) );
            }
            return model;
        } );
    }

    @Override
    public List<EntityDescriptor> lookupEntityModels( final Class type )
    {
        return entityModels.computeIfAbsent(
            type,
            key -> new TypeMatchesSelector<EntityDescriptor>( key ).selectFrom( allEntities() ) );
    }

    @Override
    public ModelDescriptor lookupServiceModel( Type serviceType )
    {
        return serviceModels.computeIfAbsent(
            serviceType,
            key -> new BestTypeMatchSelector<ModelDescriptor>( key ).selectFrom( allServices() )
                                                                    .bestMatchOrElse( null ) );
    }

    @Override
    public List<? extends ModelDescriptor> lookupServiceModels( final Type type )
    {
        return servicesReferences.computeIfAbsent(
            type,
            key -> new TypeMatchesSelector<ModelDescriptor>( key ).selectFrom( allServices() ) );
    }

    @Override
    public Stream<ObjectDescriptor> allObjects()
    {
        return getAllObjects().stream();
    }

    private List<ObjectDescriptor> getAllObjects()
    {
        return allObjects.computeIfAbsent(
            () -> concat( moduleModel.objects(),
                          concat(
                              concat(
                                  moduleModel.layer().visibleObjects( layer ),
                                  moduleModel.layer()
                                             .visibleObjects( application )
                              ),
                              moduleModel.layer()
                                         .usedLayers()
                                         .layers()
                                         .flatMap( layer -> layer.visibleObjects( application ) )
                          )
            ).collect( toList() )
        );
    }

    @Override
    public Stream<TransientDescriptor> allTransients()
    {
        return getAllTransients().stream();
    }

    private List<TransientDescriptor> getAllTransients()
    {
        return allTransients.computeIfAbsent(
            () -> concat( moduleModel.transientComposites(),
                          concat(
                              concat(
                                  moduleModel.layer().visibleTransients( layer ),
                                  moduleModel.layer().visibleTransients( application )
                              ),
                              moduleModel.layer()
                                         .usedLayers()
                                         .layers()
                                         .flatMap( layer -> layer.visibleTransients( application ) )
                          )
            ).collect( toList() )
        );
    }

    @Override
    public Stream<ValueDescriptor> allValues()
    {
        return getAllValues().stream();
    }

    private List<ValueDescriptor> getAllValues()
    {
        return allValues.computeIfAbsent(
            () -> concat( moduleModel.valueComposites(),
                          concat(
                              concat( moduleModel.layer().visibleValues( layer ),
                                      moduleModel.layer().visibleValues( application )
                              ),
                              moduleModel.layer()
                                         .usedLayers()
                                         .layers()
                                         .flatMap( layer1 -> layer1.visibleValues( application ) )
                          )
            ).collect( toList() )
        );
    }

    @Override
    public Stream<EntityDescriptor> allEntities()
    {
        return getAllEntities().stream();
    }

    private List<EntityDescriptor> getAllEntities()
    {
        return allEntities.computeIfAbsent(
            () -> concat( moduleModel.entityComposites(),
                          concat(
                              concat(
                                  moduleModel.layer().visibleEntities( layer ),
                                  moduleModel.layer().visibleEntities( application )
                              ),
                              moduleModel.layer()
                                         .usedLayers()
                                         .layers()
                                         .flatMap( layer -> layer.visibleEntities( application ) )
                          )
            ).collect( toList() )
        );
    }

    @Override
    public Stream<? extends ModelDescriptor> allServices()
    {
        return getAllServices().stream();
    }

    private List<? extends ModelDescriptor> getAllServices()
    {
        return allServices.computeIfAbsent(
            () -> concat(
                concat( moduleModel.serviceComposites(),
                        concat(
                            concat(
                                moduleModel.layer().visibleServices( layer ),
                                moduleModel.layer().visibleServices( application )
                            ),
                            moduleModel.layer()
                                       .usedLayers()
                                       .layers()
                                       .flatMap( layer -> layer.visibleServices( application ) )
                        )
                ),
                concat( moduleModel.importedServices(),
                        concat(
                            concat(
                                moduleModel.layer().visibleServices( layer ),
                                moduleModel.layer().visibleServices( application )
                            ),
                            moduleModel.layer()
                                       .usedLayers()
                                       .layers()
                                       .flatMap( layer -> layer.visibleServices( application ) )
                        )
                )
            ).collect( toList() )
        );
    }

    private static <T extends ModelDescriptor> T ambiguityMatching(
        Class type,
        List<T> modelModules,
        TypeMatching<T> matching
    )
    {
        List<T> models = modelModules.stream()
                                     .filter( matching.and( new SameVisibility<>() ) )
                                     .distinct()
                                     .collect( toList() );
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

    private static abstract class TypeMatching<T extends HasTypes>
        implements Predicate<T>
    {
        protected final Type lookedUpType;

        protected TypeMatching( Type lookedUpType )
        {
            this.lookedUpType = lookedUpType;
        }

        @Override
        public final boolean test( T model )
        {
            if( lookedUpType instanceof Class )
            {
                return model.types().anyMatch( checkMatch( lookedUpType ) );
            }
            else
            {
                if( lookedUpType instanceof ParameterizedType )
                {
                    // Foo<Bar> check
                    // First check Foo
                    ParameterizedType parameterizedType = (ParameterizedType) lookedUpType;
                    Type rawType = parameterizedType.getRawType();
                    if( model.types().noneMatch( checkMatch( rawType ) ) )
                    {
                        return false;
                    }
                    // Then check Bar
                    return interfacesOf( model.types() ).anyMatch( intf -> intf.equals( lookedUpType ) );
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

    private static class ExactTypeMatching<T extends HasTypes> extends TypeMatching<T>
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

    private static class AssignableFromTypeMatching<T extends HasTypes> extends TypeMatching<T>
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
                return candidate -> !candidate.equals( matchTo ) && clazz.isAssignableFrom( (Class<?>) candidate );
            }
//            return candidate -> candidate.equals( matchTo );
        }
    }

    /**
     * Selects descriptors by combining {@link ExactTypeMatching} and {@link AssignableFromTypeMatching}.
     *
     * Selected descriptors are sorted, exact matches first, assignable ones second.
     * Other than that, original order is preserved.
     *
     * <code>
     *     [ assignable1, matching1, assignable2, assignable3, matching2, non-matching-nor-assignable ]
     * </code>
     * results in
     * <code>
     *     [ matching1, matching2, assignable1, assignable2, assignable3 ]
     * </code>
     *
     * @param <T> Descriptor type
     */
    private static class TypeMatchesSelector<T extends HasTypes> extends ArrayList<T>
    {
        private final ExactTypeMatching<T> exactMatchPredicate;
        private final AssignableFromTypeMatching<T> assignablePredicate;
        private Integer lastMatchIndex;

        private TypeMatchesSelector( Type type )
        {
            this.exactMatchPredicate = new ExactTypeMatching<>( type );
            this.assignablePredicate = new AssignableFromTypeMatching<>( type );
        }

        List<T> selectFrom( Stream<? extends T> candidates )
        {
            candidates.forEach( this::addDescriptor );
            return this;
        }

        private void addDescriptor( T descriptor )
        {
            if( contains( descriptor ) )
            {
                return;
            }
            if( exactMatchPredicate.test( descriptor ) )
            {
                Integer nextMatchIndex = lastMatchIndex == null ? 0 : lastMatchIndex + 1;
                add( nextMatchIndex, descriptor );
                lastMatchIndex = nextMatchIndex;
            }
            else if( assignablePredicate.test( descriptor ) )
            {
                add( descriptor );
            }
        }

        boolean containsExactMatches()
        {
            return lastMatchIndex != null;
        }
    }

    /**
     * Selects the best matching descriptor by combining {@link ExactTypeMatching} and {@link AssignableFromTypeMatching}.
     *
     * Selected descriptor is the first exact match if it exists, the first assignable otherwise.
     *
     * @param <T> Descriptor type
     */
    private static class BestTypeMatchSelector<T extends HasTypes>
    {
        private TypeMatchesSelector<T> descriptors;

        BestTypeMatchSelector( Type type )
        {
            this.descriptors = new TypeMatchesSelector<>( type );
        }

        BestTypeMatchSelector<T> selectFrom( Stream<? extends T> candidates )
        {
            candidates.forEach( this::addDescriptor );
            return this;
        }

        T bestMatchOrElse( T or )
        {
            return !descriptors.isEmpty() ? descriptors.get( 0 ) : or;
        }

        private void addDescriptor( T descriptor )
        {
            // Until an exact match is found, even if we already found assignable ones,
            // keep selecting in case the last element is an exact match.
            if( !descriptors.containsExactMatches() )
            {
                descriptors.addDescriptor( descriptor );
            }
        }
    }

    /**
     * This Predicate will filter out all Models that doesn't have the same visibility as the first one.
     */
    private static class SameVisibility<T extends ModelDescriptor>
        implements Predicate<T>
    {
        private Visibility current = null;

        @Override
        public boolean test( T model )
        {
            if( current == null )
            {
                current = model.visibility();
                return true;
            }
            return current == model.visibility();
        }
    }

    private static class LazyValue<T>
    {
        private volatile T value;

        public T computeIfAbsent( Supplier<T> supplier )
        {
            if( value == null )
            {
                synchronized( this )
                {
                    if( value == null )
                    {
                        value = supplier.get();
                    }
                }
            }
            return value;
        }
    }
}
