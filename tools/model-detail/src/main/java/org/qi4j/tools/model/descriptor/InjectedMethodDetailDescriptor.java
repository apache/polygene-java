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

import org.qi4j.api.composite.InjectedMethodDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class InjectedMethodDetailDescriptor
{
    private final InjectedMethodDescriptor descriptor;
    private ActivatorDetailDescriptor activator;
    private ObjectDetailDescriptor object;
    private MixinDetailDescriptor mixin;
    private MethodSideEffectDetailDescriptor methodSideEffect;
    private MethodConcernDetailDescriptor methodConcern;
    private InjectedParametersDetailDescriptor parameters;

    InjectedMethodDetailDescriptor( InjectedMethodDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "InjectedMethodDescriptor", descriptor );
        this.descriptor = descriptor;
        this.parameters = null;
    }

    /**
     * @return Descriptor of this {@code InjectedMethodDetailDescriptor}. Never return {@code null}.
     */
    public final InjectedMethodDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Method parameters of this {@code InjectedMethodDetailDescriptor}. Never return {@code null}.
     */
    public final InjectedParametersDetailDescriptor parameters()
    {
        return parameters;
    }

    /**
     * @return Activator that owns this {@code InjectedMethodDetailDescriptor}.
     */
    public final ActivatorDetailDescriptor activator()
    {
        return activator;
    }

    /**
     * @return Object that owns this {@code InjectedMethodDetailDescriptor}.
     */
    public final ObjectDetailDescriptor object()
    {
        return object;
    }

    /**
     * @return Mixin that owns this {@code InjectedMethodDetailDescriptor}.
     */
    public final MixinDetailDescriptor mixin()
    {
        return mixin;
    }

    /**
     * @return Method side effect that owns this {@code InjectedMethodDetailDescriptor}.
     */
    public final MethodSideEffectDetailDescriptor methodSideEffect()
    {
        return methodSideEffect;
    }

    /**
     * @return Method concern that owns this {@code InjectedMethodDetailDescriptor}.
     */
    public final MethodConcernDetailDescriptor methodConcern()
    {
        return methodConcern;
    }

    final void setActivator( ActivatorDetailDescriptor descriptor )
    {
        validateNotNull( "ActivatorDetailDescriptor", descriptor );
        activator = descriptor;
    }

    final void setObject( ObjectDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "ObjectDetailDescriptor", descriptor );
        object = descriptor;
    }

    final void setMixin( MixinDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "MixinDetailDescriptor", descriptor );
        mixin = descriptor;
    }

    final void setInjectedParameter( InjectedParametersDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "InjectedParametersDetailDescriptor", descriptor );

        descriptor.setMethod( this );
        parameters = descriptor;
    }

    final void setMethodSideEffect( MethodSideEffectDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "MethodSideEffectDetailDescriptor", descriptor );
        methodSideEffect = descriptor;
    }

    final void setMethodConcern( MethodConcernDetailDescriptor descriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "MethodConcernDetailDescriptor", descriptor );
        methodConcern = descriptor;
    }

}
