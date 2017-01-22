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
package org.apache.polygene.runtime.value;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.association.AssociationStateHolder;
import org.apache.polygene.api.common.ConstructionException;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.runtime.composite.FunctionStateResolver;
import org.apache.polygene.runtime.composite.MixinModel;
import org.apache.polygene.runtime.composite.MixinsModel;
import org.apache.polygene.runtime.composite.StateResolver;
import org.apache.polygene.runtime.composite.UsesInstance;
import org.apache.polygene.runtime.injection.InjectionContext;
import org.apache.polygene.runtime.structure.ModuleInstance;

/**
 * Implementation of ValueBuilder with a prototype supplied
 */
public class ValueBuilderWithPrototype<T>
    implements ValueBuilder<T>
{
    private ValueInstance prototypeInstance;
    private final ValueModel valueModel;

    public ValueBuilderWithPrototype( ValueDescriptor compositeModelModule,
                                      ModuleInstance currentModule,
                                      T prototype
    )
    {
        valueModel = (ValueModel) compositeModelModule;
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
            mixins,
            state
        );

        int i = 0;
        InjectionContext injectionContext = new InjectionContext( valueInstance, UsesInstance.EMPTY_USES, state );
        for( MixinModel mixinModel : mixinsModel.mixinModels() )
        {
            mixins[ i++ ] = mixinModel.newInstance( injectionContext );
        }

        valueInstance.prepareToBuild();
        this.prototypeInstance = valueInstance;
    }

    @Override
    public T prototype()
    {
        verifyUnderConstruction();
        return prototypeInstance.proxy();
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
            return prototypeInstance.proxy();
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
        public Object apply( PropertyDescriptor descriptor )
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
        public EntityReference apply( AssociationDescriptor descriptor )
        {
            return prototypeState.associationFor( descriptor.accessor() ).reference();
        }
    }

    private static class AssociationDescriptorIterableFunction
        implements Function<AssociationDescriptor, Stream<EntityReference>>
    {
        private final ValueStateInstance prototypeState;

        public AssociationDescriptorIterableFunction( ValueStateInstance prototypeState )
        {
            this.prototypeState = prototypeState;
        }

        @Override
        public Stream<EntityReference> apply( AssociationDescriptor descriptor )
        {
            return prototypeState.manyAssociationFor( descriptor.accessor() ).references();
        }
    }

    private static class AssociationDescriptorMapFunction
        implements Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>>
    {
        private final ValueStateInstance prototypeState;

        public AssociationDescriptorMapFunction( ValueStateInstance prototypeState )
        {
            this.prototypeState = prototypeState;
        }

        @Override
        public Stream<Map.Entry<String, EntityReference>> apply( AssociationDescriptor descriptor )
        {
            return prototypeState.namedAssociationFor( descriptor.accessor() ).references();
        }
    }
}
