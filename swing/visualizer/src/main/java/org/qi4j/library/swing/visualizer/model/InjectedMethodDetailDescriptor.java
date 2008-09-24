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
import org.qi4j.spi.composite.InjectedMethodDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @see InjectedMethodDescriptor
 * @since 0.5
 */
public final class InjectedMethodDetailDescriptor
{
    private final InjectedMethodDescriptor descriptor;
    private ObjectDetailDescriptor object;
    private MixinDetailDescriptor mixin;
    private final List<InjectedParametersDetailDescriptor> parameters;

    InjectedMethodDetailDescriptor( InjectedMethodDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        descriptor = aDescriptor;
        parameters = new LinkedList<InjectedParametersDetailDescriptor>();
    }

    /**
     * @return Descriptor of this {@code InjectedMethodDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final InjectedMethodDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Method parameters of this {@code InjectedMethodDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final Iterable<InjectedParametersDetailDescriptor> parameters()
    {
        return parameters;
    }

    /**
     * @return Object that owns this {@code InjectedMethodDetailDescriptor}.
     *         If {@code null} this {@code InjectedMethodDetailDescriptor} is owned by a mixin.
     * @see #mixin()
     * @since 0.5
     */
    public final ObjectDetailDescriptor object()
    {
        return object;
    }

    /**
     * @return Mixin that owns this {@code InjectedMethodDetailDescriptor}.
     *         If {@code null} this {@code InjectedMethodDetailDescriptor} is owned by an object.
     * @see #object()
     * @since 0.5
     */
    public final MixinDetailDescriptor mixin()
    {
        return mixin;
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

    final void addInjectedParameter( InjectedParametersDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setMethod( this );
        parameters.add( aDescriptor );
    }
}
