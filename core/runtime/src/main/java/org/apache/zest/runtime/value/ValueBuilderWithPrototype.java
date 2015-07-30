/*
 * Copyright 2007, Rickard Öberg.
 * Copyright 2009, Niclas Hedhman.
 * Copyright 2012, Kent Sølvsten.
 * Copyright 2013, Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.runtime.value;

import java.util.HashMap;
import java.util.Map;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.association.AssociationStateHolder;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.common.ConstructionException;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.functional.Function;
import org.apache.zest.runtime.composite.FunctionStateResolver;
import org.apache.zest.runtime.composite.MixinModel;
import org.apache.zest.runtime.composite.MixinsModel;
import org.apache.zest.runtime.composite.StateResolver;
import org.apache.zest.runtime.composite.UsesInstance;
import org.apache.zest.runtime.injection.InjectionContext;
import org.apache.zest.spi.module.ModelModule;
import org.apache.zest.runtime.structure.ModuleInstance;

/**
 * Implementation of ValueBuilder with a prototype supplied
 */
public class ValueBuilderWithPrototype<T>
    implements ValueBuilder<T>
{
    private ValueInstance prototypeInstance;
    private final ValueModel valueModel;

    public ValueBuilderWithPrototype( ModelModule<ValueModel> compositeModelModule,
                                      ModuleInstance currentModule,
                                      T prototype
    )
    {
        valueModel = compositeModelModule.model();
        // Only shallow clone, as all generic types of the ValueComposites are expected to be Immutable.

        MixinsModel mixinsModel = valueModel.mixinsModel();
        Object[] mixins = mixinsModel.newMixinHolder();
        final ValueStateInstance prototypeState = ValueInstance.valueInstanceOf( (ValueComposite) prototype ).state();
        StateResolver resolver = new FunctionStateResolver(
            new PropertyDescriptorFunction( prototypeState ),
            new AssociationDescriptorEntityReferenceFunction( prototypeState ),
            new AssociationDescriptorIterableFunction( prototypeState ),
            new AssociationDescriptorMapFunction( prototypeState )
        );
        ValueStateInstance state = new ValueStateInstance( compositeModelModule, currentModule, resolver );
        ValueInstance valueInstance = new ValueInstance(
            valueModel,
            currentModule,
            mixins,
            state
        );

        int i = 0;
        InjectionContext injectionContext = new InjectionContext( valueInstance, UsesInstance.EMPTY_USES, state );
        for( MixinModel mixinModel : mixinsModel.mixinModels() )
        {
            mixins[ i++ ] = mixinModel.newInstance( injectionContext );
        }

//        // Use serialization-deserialization to make a copy of the prototype
//        final Object value;
//        try
//        {
//            // @TODO there is probably a more efficient way to do this
//            ValueSerialization valueSerialization = currentModule.valueSerialization();
//            String serialized = valueSerialization.serialize( prototype );
//            value = valueSerialization.deserialize( valueModel.valueType(), serialized);
//        }
//        catch( ValueSerializationException e )
//        {
//            throw new IllegalStateException( "Could not serialize-copy Value", e );
//        }

//        ValueInstance valueInstance = ValueInstance.valueInstanceOf( (ValueComposite) value );
        valueInstance.prepareToBuild();
        this.prototypeInstance = valueInstance;
    }

    @Override
    public T prototype()
    {
        verifyUnderConstruction();
        return prototypeInstance.<T>proxy();
    }

    @Override
    public AssociationStateHolder state()
    {
        verifyUnderConstruction();
        return prototypeInstance.state();
    }

    @Override
    public <K> K prototypeFor( Class<K> mixinType )
    {
        verifyUnderConstruction();
        return prototypeInstance.newProxy( mixinType );
    }

    @Override
    public T newInstance()
        throws ConstructionException
    {
        verifyUnderConstruction();

        // Set correct info's (immutable) on the state
        prototypeInstance.prepareBuilderState();

        // Check that it is valid
        valueModel.checkConstraints( prototypeInstance.state() );

        try
        {
            return prototypeInstance.<T>proxy();
        }
        finally
        {
            // Invalidate builder
            prototypeInstance = null;
        }
    }

    private void verifyUnderConstruction()
    {
        if( prototypeInstance == null )
        {
            throw new IllegalStateException( "ValueBuilder instances cannot be reused" );
        }
    }

    private static class PropertyDescriptorFunction
        implements Function<PropertyDescriptor, Object>
    {
        private final ValueStateInstance prototypeState;

        public PropertyDescriptorFunction( ValueStateInstance prototypeState )
        {
            this.prototypeState = prototypeState;
        }

        @Override
        public Object map( PropertyDescriptor descriptor )
        {
            return prototypeState.propertyFor( descriptor.accessor() ).get();
        }
    }

    private static class AssociationDescriptorEntityReferenceFunction
        implements Function<AssociationDescriptor, EntityReference>
    {
        private final ValueStateInstance prototypeState;

        public AssociationDescriptorEntityReferenceFunction( ValueStateInstance prototypeState )
        {
            this.prototypeState = prototypeState;
        }

        @Override
        public EntityReference map( AssociationDescriptor descriptor )
        {
            return prototypeState.associationFor( descriptor.accessor() ).reference();
        }
    }

    private static class AssociationDescriptorIterableFunction
        implements Function<AssociationDescriptor, Iterable<EntityReference>>
    {
        private final ValueStateInstance prototypeState;

        public AssociationDescriptorIterableFunction( ValueStateInstance prototypeState )
        {
            this.prototypeState = prototypeState;
        }

        @Override
        public Iterable<EntityReference> map( AssociationDescriptor descriptor )
        {
            return prototypeState.manyAssociationFor( descriptor.accessor() ).references();
        }
    }

    private static class AssociationDescriptorMapFunction
        implements Function<AssociationDescriptor, Map<String, EntityReference>>
    {
        private final ValueStateInstance prototypeState;

        public AssociationDescriptorMapFunction( ValueStateInstance prototypeState )
        {
            this.prototypeState = prototypeState;
        }

        @Override
        public Map<String, EntityReference> map( AssociationDescriptor descriptor )
        {
            Map<String, EntityReference> result = new HashMap<>();
            NamedAssociation<?> namedAssociation = prototypeState.namedAssociationFor( descriptor.accessor() );
            for( String name : namedAssociation )
            {
                result.put( name, namedAssociation.referenceOf( name ) );
            }
            return result;
        }
    }
}
