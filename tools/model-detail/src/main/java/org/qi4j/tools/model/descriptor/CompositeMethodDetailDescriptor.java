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

import org.qi4j.api.composite.MethodDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class CompositeMethodDetailDescriptor
{
    private final MethodDescriptor descriptor;

    private CompositeDetailDescriptor composite;
    private MethodConstraintsDetailDescriptor constraints;
    private MethodConcernsDetailDescriptor concerns;
    private MethodSideEffectsDetailDescriptor sideEffects;

    CompositeMethodDetailDescriptor( MethodDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        descriptor = aDescriptor;

        composite = null;
        constraints = null;
        sideEffects = null;
    }

    /**
     * @return Descriptor of this {@code CompositeMethodDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final MethodDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constraints of this {@code CompositeMethodDetailDescriptor}.
     *         Returns {@code null} if this method does not have any constraints.
     *
     * @since 0.5
     */
    public final MethodConstraintsDetailDescriptor constraints()
    {
        return constraints;
    }

    /**
     * @return Concerns of this {@code CompositeMethodDetailDescriptor}. Returns {@code null} if this method does not
     *         have any concerns.
     *
     * @since 0.5
     */
    public final MethodConcernsDetailDescriptor concerns()
    {
        return concerns;
    }

    /**
     * @return Side-effects of this {@code CompositeMethodDetailDescriptor}. Returns {@code null}
     *         if this method does not have any side effects.
     *
     * @since 0.5
     */
    public final MethodSideEffectsDetailDescriptor sideEffects()
    {
        return sideEffects;
    }

    /**
     * @return Composite that owns this {@code CompositeMethodDetailDescriptor}.
     *
     * @since 0.5
     */
    public final CompositeDetailDescriptor composite()
    {
        return composite;
    }

    final void setComposite( CompositeDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        composite = aDescriptor;
    }

    final void setConstraints( MethodConstraintsDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setMethod( this );
        constraints = aDescriptor;
    }

    public void setConcerns( MethodConcernsDetailDescriptor aDescriptor )
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setMethod( this );
        concerns = aDescriptor;
    }

    final void setSideEffects( MethodSideEffectsDetailDescriptor aDescriptor )
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setMethod( this );
        sideEffects = aDescriptor;
    }

    @Override
    public final String toString()
    {
        return descriptor.method().getName();
    }
}