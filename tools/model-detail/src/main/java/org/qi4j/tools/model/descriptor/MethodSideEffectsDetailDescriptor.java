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

import java.util.LinkedList;
import java.util.List;
import org.qi4j.api.sideeffect.SideEffectsDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class MethodSideEffectsDetailDescriptor
{
    private final SideEffectsDescriptor descriptor;
    private CompositeMethodDetailDescriptor method;
    private final List<MethodSideEffectDetailDescriptor> sideEffects;

    MethodSideEffectsDetailDescriptor( SideEffectsDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;
        sideEffects = new LinkedList<MethodSideEffectDetailDescriptor>();
    }

    /**
     * @return Descriptor of this {@code MethodSideEffectsDescriptor}.
     *
     * @since 0.5
     */
    public final SideEffectsDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Composite method that owns this {@code MethodSideEffectsDescriptor}.
     *
     * @since 0.5
     */
    public final CompositeMethodDetailDescriptor method()
    {
        return method;
    }

    /**
     * @return Side effects of this {@code MethodSideEffectDetailDescriptor}.
     *
     * @since 0.5
     */
    public final Iterable<MethodSideEffectDetailDescriptor> sideEffects()
    {
        return sideEffects;
    }

    final void setMethod( CompositeMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        method = aDescriptor;
    }

    final void addSideEffect( MethodSideEffectDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setSideEffects( aDescriptor );
        sideEffects.add( aDescriptor );
    }
}