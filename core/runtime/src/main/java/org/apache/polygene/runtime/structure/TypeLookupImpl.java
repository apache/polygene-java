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

import java.lang.reflect.Type;
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
import org.apache.polygene.api.type.HasAssignableFromType;
import org.apache.polygene.api.type.HasEqualType;
import org.apache.polygene.api.type.HasTypesCollectors;
import org.apache.polygene.api.value.ValueDescriptor;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.apache.polygene.api.common.Visibility.application;
import static org.apache.polygene.api.common.Visibility.layer;

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

    private final ModuleDescriptor module;

    /**
     * Create a new TypeLookup bound to the given Module.
     *
     * @param module Module bound to this TypeLookup
     */
    TypeLookupImpl( ModuleDescriptor module )
    {
        this.module = module;

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
            ObjectDescriptor model = ambiguityMatching( key, allModels, new HasEqualType<>( key ) );
            if( model == null )
            {
                model = ambiguityMatching( key, allModels, new HasAssignableFromType<>( key ) );
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
            TransientDescriptor model = ambiguityMatching( key, allModels, new HasEqualType<>( key ) );
            if( model == null )
            {
                model = ambiguityMatching( key, allModels, new HasAssignableFromType<>( key ) );
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
            ValueDescriptor model = ambiguityMatching( key, allModels, new HasEqualType<>( key ) );
            if( model == null )
            {
                model = ambiguityMatching( key, allModels, new HasAssignableFromType<>( key ) );
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
            EntityDescriptor model = ambiguityMatching( key, allModels, new HasEqualType<>( key ) );
            if( model == null )
            {
                model = ambiguityMatching( key, allModels, new HasAssignableFromType<>( key ) );
            }
            return model;
        } );
    }

    @Override
    public List<EntityDescriptor> lookupEntityModels( final Class type )
    {
        return entityModels.computeIfAbsent(
            type,
            key -> allEntities().collect( HasTypesCollectors.matchingTypes( key ) ) );
    }

    @Override
    public ModelDescriptor lookupServiceModel( Type serviceType )
    {
        return serviceModels.computeIfAbsent(
            serviceType,
            key -> allServices().collect( HasTypesCollectors.matchingType( key ) ).orElse( null ) );
    }

    @Override
    public List<? extends ModelDescriptor> lookupServiceModels( final Type type )
    {
        return servicesReferences.computeIfAbsent(
            type,
            key -> allServices().collect( HasTypesCollectors.matchingTypes( key ) ) );
    }

    @Override
    public Stream<ObjectDescriptor> allObjects()
    {
        return getAllObjects().stream();
    }

    private List<ObjectDescriptor> getAllObjects()
    {
        return allObjects.computeIfAbsent(
            () -> concat( module.objects(),
                          concat(
                              concat(
                                  module.layer().visibleObjects( layer ),
                                  module.layer().visibleObjects( application )
                              ),
                              module.layer().usedLayers().layers()
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
            () -> concat( module.transientComposites(),
                          concat(
                              concat(
                                  module.layer().visibleTransients( layer ),
                                  module.layer().visibleTransients( application )
                              ),
                              module.layer().usedLayers().layers()
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
            () -> concat( module.valueComposites(),
                          concat(
                              concat( module.layer().visibleValues( layer ),
                                      module.layer().visibleValues( application )
                              ),
                              module.layer().usedLayers().layers()
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
            () -> concat( module.entityComposites(),
                          concat(
                              concat(
                                  module.layer().visibleEntities( layer ),
                                  module.layer().visibleEntities( application )
                              ),
                              module.layer().usedLayers().layers()
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
                concat( module.serviceComposites(),
                        concat(
                            concat(
                                module.layer().visibleServices( layer ),
                                module.layer().visibleServices( application )
                            ),
                            module.layer().usedLayers().layers()
                                  .flatMap( layer -> layer.visibleServices( application ) )
                        )
                ),
                concat( module.importedServices(),
                        concat(
                            concat(
                                module.layer().visibleServices( layer ),
                                module.layer().visibleServices( application )
                            ),
                            module.layer().usedLayers().layers()
                                  .flatMap( layer -> layer.visibleServices( application ) )
                        )
                )
            ).collect( toList() )
        );
    }

    private static <T extends ModelDescriptor> T ambiguityMatching(
        Class type,
        List<T> modelModules,
        Predicate<T> matching
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

        private T computeIfAbsent( Supplier<T> supplier )
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
