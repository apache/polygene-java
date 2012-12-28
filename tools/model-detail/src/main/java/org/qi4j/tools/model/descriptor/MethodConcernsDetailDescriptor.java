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
import org.qi4j.api.concern.ConcernsDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class MethodConcernsDetailDescriptor
{
    private final ConcernsDescriptor descriptor;
    private CompositeMethodDetailDescriptor method;
    private final List<MethodConcernDetailDescriptor> concerns;

    MethodConcernsDetailDescriptor( ConcernsDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        descriptor = aDescriptor;
        concerns = new LinkedList<MethodConcernDetailDescriptor>();
    }

    /**
     * @return Descriptor of this {@code MethodConcernsDescriptor}.
     *
     * @since 0.5
     */
    public final ConcernsDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Method that owns this {@code MethodConcernsDescriptor}.
     *
     * @since 0.5
     */
    public final CompositeMethodDetailDescriptor method()
    {
        return method;
    }

    /**
     * @return concerns of this {@code MethodConcernDetailDescriptor}.
     *
     * @since 0.5
     */
    public final Iterable<MethodConcernDetailDescriptor> concerns()
    {
        return concerns;
    }

    final void setMethod( CompositeMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        method = aDescriptor;
    }

    final void addConcern( MethodConcernDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        aDescriptor.setConcerns( this );
        concerns.add( aDescriptor );
    }
}