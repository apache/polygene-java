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
public class DescriptorVisitor<ThrowableType extends Throwable>
{
    public void visit( ApplicationDescriptor applicationDescriptor )
        throws ThrowableType
    {
    }

    public void visit( LayerDescriptor layerDescriptor )
        throws ThrowableType
    {
    }

    public void visit( ModuleDescriptor moduleDescriptor )
        throws ThrowableType
    {
    }

    public void visit( TransientDescriptor transientDescriptor )
        throws ThrowableType
    {
    }

    public void visit( EntityDescriptor entityDescriptor )
        throws ThrowableType
    {
    }

    public void visit( ServiceDescriptor serviceDescriptor )
        throws ThrowableType
    {
    }

    public void visit( ImportedServiceDescriptor importedServiceDescriptor )
        throws ThrowableType
    {
    }

    public void visit( ObjectDescriptor objectDescriptor )
        throws ThrowableType
    {
    }

    public void visit( ValueDescriptor valueDescriptor )
        throws ThrowableType
    {
    }

    public void visit( CompositeMethodDescriptor compositeMethodDescriptor )
        throws ThrowableType
    {
    }

    public void visit( MethodConstraintsDescriptor methodConstraintsDescriptor )
        throws ThrowableType
    {
    }

    public void visit( ConstraintDescriptor constraintDescriptor )
        throws ThrowableType
    {
    }

    public void visit( MethodConcernsDescriptor methodConcernsDescriptor )
        throws ThrowableType
    {
    }

    public void visit( MethodConcernDescriptor methodConcernDescriptor )
        throws ThrowableType
    {
    }

    public void visit( MethodSideEffectsDescriptor methodSideEffectsDescriptor )
        throws ThrowableType
    {
    }

    public void visit( MethodSideEffectDescriptor methodSideEffectDescriptor )
        throws ThrowableType
    {
    }

    public void visit( ConstructorDescriptor constructorDescriptor )
        throws ThrowableType
    {
    }

    public void visit( InjectedParametersDescriptor injectedParametersDescriptor )
        throws ThrowableType
    {
    }

    public void visit( InjectedFieldDescriptor injectedFieldDescriptor )
        throws ThrowableType
    {
    }

    public void visit( InjectedMethodDescriptor injectedMethodDescriptor )
        throws ThrowableType
    {
    }

    public void visit( MixinDescriptor mixinDescriptor )
        throws ThrowableType
    {
    }
}