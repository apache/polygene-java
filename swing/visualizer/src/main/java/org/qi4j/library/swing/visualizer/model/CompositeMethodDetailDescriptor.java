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
package org.qi4j.library.swing.visualizer.model;

import java.util.LinkedList;
import java.util.List;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.spi.composite.CompositeMethodDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @see CompositeMethodDescriptor
 * @since 0.5
 */
public final class CompositeMethodDetailDescriptor
{
    private final CompositeMethodDescriptor descriptor;
    private CompositeDetailDescriptor composite;
    private final List<MethodConstraintsDetailDescriptor> constraints;
    private final List<MethodConcernDetailDescriptor> concerns;
    private final List<MethodSideEffectDetailDescriptor> sideEffects;


    CompositeMethodDetailDescriptor( CompositeMethodDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        descriptor = aDescriptor;

        constraints = new LinkedList<MethodConstraintsDetailDescriptor>();
        concerns = new LinkedList<MethodConcernDetailDescriptor>();
        sideEffects = new LinkedList<MethodSideEffectDetailDescriptor>();
    }

    /**
     * @return Descriptor of this {@code CompositeMethodDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final CompositeMethodDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constraints of this {@code CompositeMethodDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final Iterable<MethodConstraintsDetailDescriptor> constraints()
    {
        return constraints;
    }

    /**
     * @return Concerns of this {@code CompositeMethodDetailDescriptor}. concerns. Never return {@code null}.
     * @since 0.5
     */
    public final Iterable<MethodConcernDetailDescriptor> concerns()
    {
        return concerns;
    }

    /**
     * @return Side-effects of this {@code CompositeMethodDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final Iterable<MethodSideEffectDetailDescriptor> sideEffects()
    {
        return sideEffects;
    }

    /**
     * @return Composite that owns this {@code CompositeMethodDetailDescriptor}. Never return {@code null}.
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

    final void addConstraint( MethodConstraintsDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setMethod( this );
        constraints.add( aDescriptor );
    }

    final void addConcern( MethodConcernDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setMethod( this );
        concerns.add( aDescriptor );
    }

    final void addSideEffect( MethodSideEffectDetailDescriptor aDescriptor )
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setMethod( this );
        sideEffects.add( aDescriptor );
    }
}
