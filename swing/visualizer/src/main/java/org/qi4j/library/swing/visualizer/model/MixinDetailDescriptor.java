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
import org.qi4j.spi.composite.InjectedFieldDescriptor;
import org.qi4j.spi.composite.MixinDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @see MixinDescriptor
 * @since 0.5
 */
public final class MixinDetailDescriptor
{
    private final MixinDescriptor descriptor;
    private final List<ConstructorDetailDescriptor> constructors;
    private final List<InjectedMethodDetailDescriptor> injectedMethods;
    private final List<InjectedFieldDescriptor> injectedFields;

    MixinDetailDescriptor( MixinDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;
        constructors = new LinkedList<ConstructorDetailDescriptor>();
        injectedMethods = new LinkedList<InjectedMethodDetailDescriptor>();
        injectedFields = new LinkedList<InjectedFieldDescriptor>();
    }

    /**
     * @return Descriptor of this {@code MixinDetailDescriptor}.
     * @since 0.5
     */
    public final MixinDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constructors of this {@code MixinDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final Iterable<ConstructorDetailDescriptor> constructors()
    {
        return constructors;
    }

    /**
     * @return Injected methods of this {@code MixinDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final Iterable<InjectedMethodDetailDescriptor> injectedMethods()
    {
        return injectedMethods;
    }

    /**
     * @return Injected fields of this {@code MixinDetailDescriptor}. Never return {@code null}.
     * @since 0.5
     */
    public final Iterable<InjectedFieldDescriptor> getInjectedFields()
    {
        return injectedFields;
    }

    final void addConstructor( ConstructorDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        constructors.add( aDescriptor );
    }

    final void addInjectedMethod( InjectedMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        injectedMethods.add( aDescriptor );
    }

    final void addInjectedField( InjectedFieldDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        injectedFields.add( aDescriptor );
    }
}
