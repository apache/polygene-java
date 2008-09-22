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
package org.qi4j.library.swing.visualizer.overview.descriptor;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.CompositeMethodDescriptor;
import org.qi4j.spi.composite.MixinDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public class CompositeDetailDescriptor<T extends CompositeDescriptor>
{
    private final T descriptor;
    private final List<CompositeMethodDetailDescriptor> methods;
    private final List<MixinDescriptor> mixins;

    CompositeDetailDescriptor( T aDescriptor )
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;
        methods = new LinkedList<CompositeMethodDetailDescriptor>();
        mixins = new LinkedList<MixinDescriptor>();
    }

    /**
     * @return standard composite descriptor.
     * @since 0.5
     */
    public final T descriptor()
    {
        return descriptor;
    }

    /**
     * @return methods of composite represented by this descriptor.
     * @since 0.5
     */
    public final Iterable<CompositeMethodDetailDescriptor> methods()
    {
        return methods;
    }

    /**
     * Return method detail descriptor of the requested method. Returns {@code null} if not found.
     *
     * @param aMethod Method to look up. This argument must not be {@code null}.
     * @return method detail descriptor of the requested method.
     * @since 0.5
     */
    public CompositeMethodDetailDescriptor getMethodDescriptor( Method aMethod )
    {
        for( CompositeMethodDetailDescriptor descriptor : methods )
        {
            CompositeMethodDescriptor methodDescriptor = descriptor.descriptor();
            Method method = methodDescriptor.method();
            if( method.equals( aMethod ) )
            {
                return descriptor;
            }
        }

        return null;
    }

    /**
     * @return mixins of composite represented by this descriptor.
     * @since 0.5
     */
    public final Iterable<MixinDescriptor> mixins()
    {
        return mixins;
    }

    final void addMethod( CompositeMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        methods.add( aDescriptor );
    }

    final void addMixin( MixinDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        mixins.add( aDescriptor );
    }

}
