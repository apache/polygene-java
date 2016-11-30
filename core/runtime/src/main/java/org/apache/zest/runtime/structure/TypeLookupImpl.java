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
package org.apache.zest.runtime.structure;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.AmbiguousTypeException;
import org.apache.zest.api.composite.ModelDescriptor;
import org.apache.zest.api.composite.TransientDescriptor;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.object.ObjectDescriptor;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.structure.TypeLookup;
import org.apache.zest.api.type.HasTypes;
import org.apache.zest.api.value.ValueDescriptor;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.apache.zest.api.common.Visibility.application;
import static org.apache.zest.api.common.Visibility.layer;
import static org.apache.zest.api.util.Classes.interfacesOf;

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
        return entityModels.computeIfAbsent( type, key -> new TypeMatchingDescriptors<EntityDescriptor>(key).selectedFrom(allEntities()));
    }

    @Override
    public ModelDescriptor lookupServiceModel( Type serviceType )
    {
        return serviceModels.computeIfAbsent( serviceType,
                                              key -> new BestTypeMatchingDescriptors<ModelDescriptor>(key).selectedFrom(allServices()).bestMatchOrElse(null));
    }

    @Override
    public List<? extends ModelDescriptor> lookupServiceModels( final Type type1 )
    {
        return servicesReferences.computeIfAbsent( type1, type ->new TypeMatchingDescriptors<ModelDescriptor>(type).selectedFrom(allServices()));
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
    
    private static class TypeMatchingDescriptors<T extends HasTypes> extends ArrayList<T> {

        /**
         * mutable :-( But performance is an issue here.
         */
        private Integer lastMatchingindex;
        private final ExactTypeMatching<T> exactMatchingPredicate;
        private final AssignableFromTypeMatching<T> assignablePredicate;

        private TypeMatchingDescriptors(Type type) {
            this.lastMatchingindex = null;
            this.exactMatchingPredicate = new ExactTypeMatching<>(type);
            this.assignablePredicate = new AssignableFromTypeMatching<>(type);
        }

        TypeMatchingDescriptors<T> selectedFrom(Stream<? extends T> candidates){
            candidates.forEach(this::smartAddition);
            return this;
        }
        /**
         * Sorts the descriptors in a common list : matching ones first,
         * assignable ones follow. The order of arrival is important :
         * 
         * "{assignable1, matching1, assignable2,assignable3,matching2,
         * non-matching-or-assignable}" should result in "{ matching1,
         * matching2, assignable1, assignable2, assignable3}"
         */
        private  void smartAddition(T descriptor) {
            if (contains(descriptor)) {
                return;
            }
            if ( exactMatchingPredicate.test(descriptor)) {
                Integer nextMatchingIdx = lastMatchingindex == null ? 0 : lastMatchingindex + 1;
                add(nextMatchingIdx, descriptor);
                lastMatchingindex = nextMatchingIdx;
                return;
            }
            if (assignablePredicate.test(descriptor)) {
                add(descriptor);
            }
        }

        private  boolean containsExactMatches() {
            return lastMatchingindex != null;
        }

    }

    private static class BestTypeMatchingDescriptors<T extends HasTypes> {

        private TypeMatchingDescriptors<T> descriptors;

        private  BestTypeMatchingDescriptors(Type type) {
            this(new TypeMatchingDescriptors<>(type));
        }

        private  BestTypeMatchingDescriptors(TypeMatchingDescriptors<T> descriptors) {
            this.descriptors = descriptors;
        }

        BestTypeMatchingDescriptors<T> selectedFrom(Stream<? extends T> candidates) {
            candidates.forEach(this::smartAddition);
            return this;
        }
        
        private T bestMatchOrElse(T or) {
            return !descriptors.isEmpty() ? descriptors.get(0) : or;
        }

        /**
         * We want the first matching if exists, the first assignable otherwise.
         * While there is no matching descriptor, even if we found assignable
         * ones, we keep searching in case the last element is a matching type.
         */
        private void smartAddition(T descriptor) {
            if (!descriptors.containsExactMatches()) {
                descriptors.smartAddition(descriptor);
            }
        }
    }

}
