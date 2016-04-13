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
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.structure.TypeLookup;
import org.apache.zest.api.type.HasTypes;
import org.apache.zest.api.value.ValueDescriptor;

import static java.util.stream.Stream.concat;
import static org.apache.zest.api.common.Visibility.application;
import static org.apache.zest.api.common.Visibility.layer;
import static org.apache.zest.api.util.Classes.interfacesOf;
import static org.apache.zest.functional.Iterables.first;

/**
 * Central place for Composite Type lookups.
 */
class TypeLookupImpl
    implements TypeLookup
{

    // Constructor parameters
    private final ModuleDescriptor moduleModel;

    // Eager instance objects
    private final ConcurrentHashMap<Class<?>, ObjectDescriptor> objectModels;
    private final ConcurrentHashMap<Class<?>, TransientDescriptor> transientModels;
    private final ConcurrentHashMap<Class<?>, ValueDescriptor> valueModels;
    private final ConcurrentHashMap<Class<?>, List<? extends EntityDescriptor>> allEntityModels;
    private final ConcurrentHashMap<Class<?>, EntityDescriptor> unambiguousEntityModels;
    private final ConcurrentHashMap<Type, ModelDescriptor> serviceModels;
    private final ConcurrentHashMap<Type, List<ModelDescriptor>> servicesReferences;

    /**
     * Create a new TypeLookup bound to the given moduleModel.
     *
     * @param module ModuleModel bound to this TypeLookup
     */
    TypeLookupImpl( ModuleModel module )
    {
        // Constructor parameters
        this.moduleModel = module;

        // Eager instance objects
        objectModels = new ConcurrentHashMap<>();
        transientModels = new ConcurrentHashMap<>();
        valueModels = new ConcurrentHashMap<>();
        allEntityModels = new ConcurrentHashMap<>();
        unambiguousEntityModels = new ConcurrentHashMap<>();
        serviceModels = new ConcurrentHashMap<>();
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
    @Override
    public ObjectDescriptor lookupObjectModel( final Class<?> type )
    {
        return objectModels.computeIfAbsent( type, key ->
        {
            List<ObjectDescriptor> allModels = allObjects().collect( Collectors.toList() );
            ObjectDescriptor model = ambiguityMatching( key, allModels, new ExactTypeMatching<>( key ) );
            if( model == null )
            {
                model = ambiguityMatching( key, allModels, new AssignableFromTypeMatching<>( key ) );
            }
            return model;
        } );
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
    @Override
    public TransientDescriptor lookupTransientModel( final Class<?> type )
    {
        return transientModels.computeIfAbsent( type, key ->
        {
            List<TransientDescriptor> allModels = allTransients().collect( Collectors.toList() );
            TransientDescriptor model = ambiguityMatching( key, allModels, new ExactTypeMatching<>( key ) );
            if( model == null )
            {
                model = ambiguityMatching( key, allModels, new AssignableFromTypeMatching<>( key ) );
            }
            return model;
        } );
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
    @Override
    public ValueDescriptor lookupValueModel( final Class<?> type )
    {
        return valueModels.computeIfAbsent( type, key ->
        {
            List<ValueDescriptor> allModels = allValues().collect( Collectors.toList() );
            ValueDescriptor model = ambiguityMatching( key, allModels, new ExactTypeMatching<>( key ) );
            if( model == null )
            {
                model = ambiguityMatching( key, allModels, new AssignableFromTypeMatching<>( key ) );
            }
            return model;
        } );
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
    @Override
    public EntityDescriptor lookupEntityModel( final Class<?> type )
    {
        return unambiguousEntityModels.computeIfAbsent( type, key ->
        {
            List<EntityDescriptor> allModels = allEntities().collect( Collectors.toList() );
            EntityDescriptor model = ambiguityMatching( key, allModels, new ExactTypeMatching<>( key ) );
            if( model == null )
            {
                model = ambiguityMatching( key, allModels, new AssignableFromTypeMatching<>( key ) );
            }
            return model;
        } );
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
    @Override
    public Iterable<? extends EntityDescriptor> lookupEntityModels( final Class type )
    {
        return allEntityModels.computeIfAbsent( type, key ->
            concat(
                allEntities().filter( ref -> new ExactTypeMatching<>( key ).test( ref ) ),
                allEntities().filter( ref -> new AssignableFromTypeMatching<>( key ).test( ref ) )
            ).distinct().collect( Collectors.toList() )
        );
    }

    @Override
    public ModelDescriptor lookupServiceModel( Type serviceType1 )
    {
        return serviceModels.computeIfAbsent( serviceType1, key -> first( lookupServiceModels( key ) ) );
    }

    @Override
    public List<? extends ModelDescriptor> lookupServiceModels( Type type1 )
    {
        return servicesReferences.computeIfAbsent( type1, type ->
        {
            // There is a requirement that "exact match" services must be returned before "assignable match"
            // services, hence the dual streams instead of a OR filter.
            return Stream.concat(
                allServices()
                    .filter( new ExactTypeMatching<>( type ) ),
                allServices()
                    .filter( new AssignableFromTypeMatching<>( type ) ) )
                .distinct()
                .collect( Collectors.toList() );
        } );
    }

    @Override
    public Stream<Class<?>> allVisibleObjects()
    {
        return allObjects().flatMap( HasTypes::types );
    }

    @Override
    public Stream<? extends ObjectDescriptor> allObjects()
    {
        return concat( moduleModel.objects(),
                       concat(
                           concat(
                               moduleModel.layer().visibleObjects( layer ),
                               moduleModel.layer().visibleObjects( application )
                           ),
                           moduleModel.layer()
                               .usedLayers()
                               .layers()
                               .flatMap( layer -> layer.visibleObjects( application ) )
                       )
        );
    }

    @Override
    public Stream<? extends TransientDescriptor> allTransients()
    {
        return concat( moduleModel.transientComposites(),
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
        );
    }

    @Override
    public Stream<? extends ValueDescriptor> allValues()
    {

        return concat( moduleModel.valueComposites(),
                       concat(
                           concat( moduleModel.layer().visibleValues( layer ),
                                   moduleModel.layer().visibleValues( application )
                           ),
                           moduleModel.layer()
                               .usedLayers()
                               .layers()
                               .flatMap( layer1 -> layer1.visibleValues( application ) )
                       )
        );
    }

    @Override
    public Stream<? extends EntityDescriptor> allEntities()
    {
        return concat( moduleModel.entityComposites(),
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
        );
    }

    @Override
    public Stream<? extends ModelDescriptor> allServices()
    {
        Stream<? extends ModelDescriptor> managedServices =
            concat( moduleModel.serviceComposites(),
                    concat(
                        concat(
                            moduleModel.layer()
                                .visibleServices( layer ),
                            moduleModel.layer()
                                .visibleServices( application )
                        ),
                        moduleModel.layer()
                            .usedLayers()
                            .layers()
                            .flatMap( layer -> layer.visibleServices( application ) )
                    )
            );
        Stream<? extends ModelDescriptor> importedServices =
            concat( moduleModel.serviceComposites(),
                    concat(
                        concat(
                            moduleModel.layer()
                                .visibleServices( layer ),
                            moduleModel.layer()
                                .visibleServices( application )
                        ),
                        moduleModel.layer()
                            .usedLayers()
                            .layers()
                            .flatMap( layer -> layer.visibleServices( application ) )
                    )
            );
        return concat( managedServices, importedServices );
    }

    private <T extends ModelDescriptor> T ambiguityMatching(
        Class type,
        List<T> modelModules,
        TypeMatching<T> matching
    )
    {
        List<T> models = modelModules.stream()
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
                    if( !model.types().anyMatch( checkMatch( rawType ) ) )
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

    private static final class ExactTypeMatching<T extends HasTypes> extends TypeMatching<T>
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

    private static final class AssignableFromTypeMatching<T extends HasTypes> extends TypeMatching<T>
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
}
