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
import org.qi4j.service.ServiceDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @see ServiceDescriptor
 * @since 0.5
 */
public final class ServiceDetailDescriptor
{
    private final ServiceDescriptor descriptor;
    private ModuleDetailDescriptor module;

    ServiceDetailDescriptor( ServiceDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        descriptor = aDescriptor;
    }

    /**
     * @return Descriptor of this {@code ServiceDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final ServiceDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Module that owns this {@code ServiceDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final ModuleDetailDescriptor module()
    {
        return module;
    }

    final void setModule( ModuleDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        module = aDescriptor;
    }
}
