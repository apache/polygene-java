/*
 * Copyright (c) 2008, Edward Yakop. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
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
package org.qi4j.tools.model.descriptor;

import org.qi4j.api.composite.ConstructorDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class ConstructorDetailDescriptor
{
    private final ConstructorDescriptor descriptor;
    private ActivatorDetailDescriptor activator;
    private ObjectDetailDescriptor object;
    private MixinDetailDescriptor mixin;
    private MethodConcernDetailDescriptor methodConcern;
    private MethodSideEffectDetailDescriptor methodSideEffect;
    private InjectedParametersDetailDescriptor parameters;

    ConstructorDetailDescriptor( ConstructorDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "ConstructorDescriptor", descriptor );
        this.descriptor = descriptor;
        this.parameters = null;
    }

    /**
     * @return Descriptor of this {@code ConstructorDetailDescriptor}. Never return {@code null}.
     */
    public final ConstructorDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constructor parameters of this {@code ConstructorDetailDescriptor}. Never return {@code null}.
     */
    public final InjectedParametersDetailDescriptor parameters()
    {
        return parameters;
    }

    /**
     * @return Activator that own this {@code ConstructorDetailDescriptor}.
     */
    public final ActivatorDetailDescriptor activator()
    {
        return activator;
    }

    /**
     * @return Object that own this {@code ConstructorDetailDescriptor}.
     */
    public final ObjectDetailDescriptor object()
    {
        return object;
    }

    /**
     * @return Mixin that own this {@code ConstructorDetailDescriptor}.
     */
    public final MixinDetailDescriptor mixin()
    {
        return mixin;
    }

    /**
     * @return Method concern that own this {@code ConstructorDetailDescriptor}.
     */
    public final MethodConcernDetailDescriptor methodConcern()
    {
        return methodConcern;
    }

    /**
     * @return Method side effect that own this {@code ConstructorDetailDescriptor}.
     */
    public final MethodSideEffectDetailDescriptor methodSideEffect()
    {
        return methodSideEffect;
    }

    final void setActivator( ActivatorDetailDescriptor descriptor )
    {
        validateNotNull( "ActivatorDetailDescriptor", descriptor );
        activator = descriptor;
    }

    final void setObject( ObjectDetailDescriptor descriptor )
    {
        validateNotNull( "ObjectDetailDescriptor", descriptor );
        object = descriptor;
    }

    final void setMixin( MixinDetailDescriptor descriptor )
    {
        validateNotNull( "MixinDetailDescriptor", descriptor );
        mixin = descriptor;
    }

    final void setMethodConcern( MethodConcernDetailDescriptor descriptor )
    {
        validateNotNull( "MethodConcernDetailDescriptor", descriptor );
        methodConcern = descriptor;
    }

    final void setInjectedParameter( InjectedParametersDetailDescriptor descriptor )
    {
        validateNotNull( "InjectedParametersDetailDescriptor", descriptor );
        descriptor.setConstructor( this );
        parameters = descriptor;
    }

    final void setMethodSideEffect( MethodSideEffectDetailDescriptor descriptor )
    {
        validateNotNull( "MethodSideEffectDetailDescriptor", descriptor );
        methodSideEffect = descriptor;
    }

    @Override
    public final String toString()
    {
        return descriptor.constructor().getDeclaringClass().getSimpleName();
    }

}
