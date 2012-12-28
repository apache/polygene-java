/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.tools.model.descriptor;

import org.qi4j.api.composite.InjectedFieldDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class InjectedFieldDetailDescriptor
{
    private final InjectedFieldDescriptor descriptor;
    private ObjectDetailDescriptor object;
    private MixinDetailDescriptor mixin;
    private MethodConcernDetailDescriptor methodConcern;
    private MethodSideEffectDetailDescriptor methodSideEffect;

    InjectedFieldDetailDescriptor( InjectedFieldDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        descriptor = aDescriptor;
    }

    /**
     * @return Descriptor of this {@code InjectedFieldDetailDescriptor}. Never returns {@code null}.
     *
     * @since 0.5
     */
    public final InjectedFieldDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Object that own this {@code InjectedFieldDetailDescriptor}.
     *
     * @see #mixin()
     * @see #methodConcern()
     * @see #methodSideEffect()
     * @since 0.5
     */
    public final ObjectDetailDescriptor object()
    {
        return object;
    }

    /**
     * @return Mixin that own this {@code InjectedFieldDetailDescriptor}.
     *
     * @see #object()
     * @see #methodConcern()
     * @see #methodSideEffect()
     * @since 0.5
     */
    public final MixinDetailDescriptor mixin()
    {
        return mixin;
    }

    /**
     * @return Method concern that own this {@code InjectedFieldDetailDescriptor}.
     *
     * @see #object()
     * @see #mixin()
     * @see #methodSideEffect()
     * @since 0.5
     */
    public final MethodConcernDetailDescriptor methodConcern()
    {
        return methodConcern;
    }

    /**
     * @return Method side effect that own this {@code InjectedFieldDetailDescriptor}.
     *
     * @see #object()
     * @see #mixin()
     * @see #methodConcern()
     * @since 0.5
     */
    public final MethodSideEffectDetailDescriptor methodSideEffect()
    {
        return methodSideEffect;
    }

    final void setObject( ObjectDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        object = aDescriptor;
    }

    final void setMixin( MixinDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        mixin = aDescriptor;
    }

    final void setMethodConcern( MethodConcernDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        methodConcern = aDescriptor;
    }

    final void setMethodSideEffect( MethodSideEffectDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        methodSideEffect = aDescriptor;
    }

    @Override
    public final String toString()
    {
        return descriptor.field().getName();
    }
}