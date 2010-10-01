/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.structure;

import org.qi4j.runtime.composite.AbstractConstraintModel;
import org.qi4j.runtime.composite.CompositeMethodModel;
import org.qi4j.runtime.composite.ConstructorModel;
import org.qi4j.runtime.composite.MethodConcernModel;
import org.qi4j.runtime.composite.MethodConcernsModel;
import org.qi4j.runtime.composite.MethodConstraintsModel;
import org.qi4j.runtime.composite.MethodSideEffectModel;
import org.qi4j.runtime.composite.MethodSideEffectsModel;
import org.qi4j.runtime.composite.MixinModel;
import org.qi4j.runtime.composite.TransientModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.injection.InjectedFieldModel;
import org.qi4j.runtime.injection.InjectedMethodModel;
import org.qi4j.runtime.injection.InjectedParametersModel;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.service.ImportedServiceModel;
import org.qi4j.runtime.service.ServiceModel;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.structure.DescriptorVisitor;

/**
 * Adapter of modelvisitor for the SPI descriptors
 */
public class DescriptorModelVisitor<ThrowableType extends Exception>
    extends ModelVisitor<ThrowableType>
{
    private DescriptorVisitor<ThrowableType> visitor;

    public DescriptorModelVisitor( DescriptorVisitor<ThrowableType> visitor )
    {
        super();
        this.visitor = visitor;
    }

    @Override
    public void visit( ApplicationModel applicationModel )
        throws ThrowableType
    {
        visitor.visit( applicationModel );
    }

    @Override
    public void visit( LayerModel layerModel )
        throws ThrowableType
    {
        visitor.visit( layerModel );
    }

    @Override
    public void visit( ModuleModel moduleModel )
        throws ThrowableType
    {
        visitor.visit( moduleModel );
    }

    @Override
    public void visit( TransientModel transientModel )
        throws ThrowableType
    {
        visitor.visit( transientModel );
    }

    @Override
    public void visit( EntityModel entityModel )
        throws ThrowableType
    {
        visitor.visit( entityModel );
    }

    @Override
    public void visit( ValueModel valueModel )
        throws ThrowableType
    {
        visitor.visit( valueModel );
    }

    @Override
    public void visit( ServiceModel serviceModel )
        throws ThrowableType
    {
        visitor.visit( (ServiceDescriptor) serviceModel );
    }

    @Override
    public void visit( ImportedServiceModel serviceModel )
        throws ThrowableType
    {
        visitor.visit( serviceModel );
    }

    @Override
    public void visit( ObjectModel objectModel )
        throws ThrowableType
    {
        visitor.visit( objectModel );
    }

    @Override
    public void visit( CompositeMethodModel compositeMethodModel )
        throws ThrowableType
    {
        visitor.visit( compositeMethodModel );
    }

    @Override
    public void visit( MethodConstraintsModel methodConstraintsModel )
        throws ThrowableType
    {
        visitor.visit( methodConstraintsModel );
    }

    @Override
    public void visit( AbstractConstraintModel constraintModel )
        throws ThrowableType
    {
        visitor.visit( constraintModel );
    }

    @Override
    public void visit( MethodConcernsModel methodConcernsModel )
        throws ThrowableType
    {
        visitor.visit( methodConcernsModel );
    }

    @Override
    public void visit( MethodConcernModel methodConcernModel )
        throws ThrowableType
    {
        visitor.visit( methodConcernModel );
    }

    public void visit( MethodSideEffectsModel methodSideEffectsModel )
        throws ThrowableType
    {
        visitor.visit( methodSideEffectsModel );
    }

    @Override
    public void visit( MethodSideEffectModel methodSideEffectModel )
        throws ThrowableType
    {
        visitor.visit( methodSideEffectModel );
    }

    @Override
    public void visit( ConstructorModel constructorModel )
        throws ThrowableType
    {
        visitor.visit( constructorModel );
    }

    @Override
    public void visit( InjectedParametersModel injectedParametersModel )
        throws ThrowableType
    {
        visitor.visit( injectedParametersModel );
    }

    @Override
    public void visit( InjectedFieldModel injectedFieldModel )
        throws ThrowableType
    {
        visitor.visit( injectedFieldModel );
    }

    @Override
    public void visit( InjectedMethodModel injectedMethodModel )
        throws ThrowableType
    {
        visitor.visit( injectedMethodModel );
    }

    @Override
    public void visit( MixinModel mixinModel )
        throws ThrowableType
    {
        visitor.visit( mixinModel );
    }
}
