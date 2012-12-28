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

import org.qi4j.api.composite.ConstructorDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class ConstructorDetailDescriptor
{
    private final ConstructorDescriptor descriptor;
    private ObjectDetailDescriptor object;
    private MixinDetailDescriptor mixin;
    private MethodConcernDetailDescriptor methodConcern;
    private MethodSideEffectDetailDescriptor methodSideEffect;
    private InjectedParametersDetailDescriptor parameters;

    ConstructorDetailDescriptor( ConstructorDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;
        parameters = null;
    }

    /**
     * @return Descriptor of this {@code ConstructorDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final ConstructorDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constructor parameters of this {@code ConstructorDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final InjectedParametersDetailDescriptor parameters()
    {
        return parameters;
    }

    /**
     * @return Object that own this {@code ConstructorDetailDescriptor}.
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
     * @return Mixin that own this {@code ConstructorDetailDescriptor}.
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
     * @return Method concern that own this {@code ConstructorDetailDescriptor}.
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
     * @return Method side effect that own this {@code ConstructorDetailDescriptor}.
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

    final void setInjectedParameter( InjectedParametersDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setConstructor( this );
        parameters = aDescriptor;
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
        return descriptor.constructor().getDeclaringClass().getSimpleName();
    }
}