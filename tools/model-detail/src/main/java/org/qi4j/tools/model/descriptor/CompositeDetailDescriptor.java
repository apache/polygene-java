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

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.composite.MethodDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public class CompositeDetailDescriptor<T extends CompositeDescriptor>
{
    protected final T descriptor;
    protected ModuleDetailDescriptor module;
    protected final List<CompositeMethodDetailDescriptor> methods;
    protected final List<MixinDetailDescriptor> mixins;

    CompositeDetailDescriptor( T aDescriptor )
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;
        methods = new LinkedList<CompositeMethodDetailDescriptor>();
        mixins = new LinkedList<MixinDetailDescriptor>();
    }

    /**
     * @return Descriptor of this {@code CompositeDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final T descriptor()
    {
        return descriptor;
    }

    /**
     * @return Methods of this {@code CompositeDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final Iterable<CompositeMethodDetailDescriptor> methods()
    {
        return methods;
    }

    /**
     * @return Mixins of this {@code CompositeDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    public final Iterable<MixinDetailDescriptor> mixins()
    {
        return mixins;
    }

    /**
     * Return method detail descriptor of the requested method. Returns {@code null} if not found.
     *
     * @param aMethod Method to look up. This argument must not be {@code null}.
     *
     * @return method detail descriptor of the requested method.
     *
     * @since 0.5
     */
    public final CompositeMethodDetailDescriptor getMethodDescriptor( Method aMethod )
    {
        for( CompositeMethodDetailDescriptor descriptor : methods )
        {
            MethodDescriptor methodDescriptor = descriptor.descriptor();
            Method method = methodDescriptor.method();
            if( method.equals( aMethod ) )
            {
                return descriptor;
            }
        }

        return null;
    }

    /**
     * @return Module that own this {@code CompositeDetailDescriptor}.
     *
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

    final void addMethod( CompositeMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setComposite( this );
        methods.add( aDescriptor );
    }

    final void addMixin( MixinDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setComposite( this );
        mixins.add( aDescriptor );
    }

    public String toString()
    {
        return descriptor.toString();
    }
}