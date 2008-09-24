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

import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.spi.composite.MethodConcernDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class MethodConcernDetailDescriptor
{
    private final MethodConcernDescriptor descriptor;
    private CompositeMethodDetailDescriptor method;

    MethodConcernDetailDescriptor( MethodConcernDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;
    }

    /**
     * @return Descriptor of this {@code MethodConcernDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final MethodConcernDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Method that owns this {@code MethodConcernDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final CompositeMethodDetailDescriptor method()
    {
        return method;
    }

    final void setMethod( CompositeMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        method = aDescriptor;
    }
}
