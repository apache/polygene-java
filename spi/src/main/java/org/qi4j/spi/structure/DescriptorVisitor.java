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

package org.qi4j.spi.structure;

import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.spi.composite.CompositeMethodDescriptor;
import org.qi4j.spi.composite.ConstructorDescriptor;
import org.qi4j.spi.composite.InjectedFieldDescriptor;
import org.qi4j.spi.composite.InjectedMethodDescriptor;
import org.qi4j.spi.composite.InjectedParametersDescriptor;
import org.qi4j.spi.composite.TransientDescriptor;
import org.qi4j.spi.concern.MethodConcernDescriptor;
import org.qi4j.spi.concern.MethodConcernsDescriptor;
import org.qi4j.spi.constraint.ConstraintDescriptor;
import org.qi4j.spi.constraint.MethodConstraintsDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.mixin.MixinDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.sideeffect.MethodSideEffectDescriptor;
import org.qi4j.spi.sideeffect.MethodSideEffectsDescriptor;
import org.qi4j.spi.value.ValueDescriptor;

/**
 * Extend and override this class in order to introspect a Qi4j model.
 * Call {@link ApplicationSPI#visitDescriptor(DescriptorVisitor)} to use.
 */
public class DescriptorVisitor
{
    public void visit( ApplicationDescriptor applicationDescriptor )
    {
    }

    public void visit( LayerDescriptor layerDescriptor )
    {
    }

    public void visit( ModuleDescriptor moduleDescriptor )
    {
    }

    public void visit( TransientDescriptor transientDescriptor )
    {
    }

    public void visit( EntityDescriptor entityDescriptor )
    {
    }

    public void visit( ServiceDescriptor serviceDescriptor )
    {
    }

    public void visit( ImportedServiceDescriptor importedServiceDescriptor )
    {
    }

    public void visit( ObjectDescriptor objectDescriptor )
    {
    }

    public void visit( ValueDescriptor valueDescriptor )
    {
    }

    public void visit( CompositeMethodDescriptor compositeMethodDescriptor )
    {
    }

    public void visit( MethodConstraintsDescriptor methodConstraintsDescriptor )
    {
    }

    public void visit( ConstraintDescriptor constraintDescriptor )
    {
    }

    public void visit( MethodConcernsDescriptor methodConcernsDescriptor )
    {
    }

    public void visit( MethodConcernDescriptor methodConcernDescriptor )
    {
    }

    public void visit( MethodSideEffectsDescriptor methodSideEffectsDescriptor )
    {
    }

    public void visit( MethodSideEffectDescriptor methodSideEffectDescriptor )
    {
    }

    public void visit( ConstructorDescriptor constructorDescriptor )
    {
    }

    public void visit( InjectedParametersDescriptor injectedParametersDescriptor )
    {
    }

    public void visit( InjectedFieldDescriptor injectedFieldDescriptor )
    {
    }

    public void visit( InjectedMethodDescriptor injectedMethodDescriptor )
    {
    }

    public void visit( MixinDescriptor mixinDescriptor )
    {
    }
}