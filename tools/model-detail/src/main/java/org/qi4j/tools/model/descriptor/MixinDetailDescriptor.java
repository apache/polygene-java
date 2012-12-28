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
import org.qi4j.api.mixin.MixinDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class MixinDetailDescriptor
    implements InjectableDetailDescriptor
{
    private final MixinDescriptor descriptor;
    private CompositeDetailDescriptor composite;
    private final List<ConstructorDetailDescriptor> constructors;
    private final List<InjectedMethodDetailDescriptor> injectedMethods;
    private final List<InjectedFieldDetailDescriptor> injectedFields;

    MixinDetailDescriptor( MixinDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;
        constructors = new LinkedList<ConstructorDetailDescriptor>();
        injectedMethods = new LinkedList<InjectedMethodDetailDescriptor>();
        injectedFields = new LinkedList<InjectedFieldDetailDescriptor>();
    }

    /**
     * @return Descriptor of this {@code MixinDetailDescriptor}.
     *
     * @since 0.5
     */
    public final MixinDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constructors of this {@code MixinDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    @Override
    public final Iterable<ConstructorDetailDescriptor> constructors()
    {
        return constructors;
    }

    /**
     * @return Injected methods of this {@code MixinDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    @Override
    public final Iterable<InjectedMethodDetailDescriptor> injectedMethods()
    {
        return injectedMethods;
    }

    /**
     * @return Injected fields of this {@code MixinDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    @Override
    public final Iterable<InjectedFieldDetailDescriptor> injectedFields()
    {
        return injectedFields;
    }

    /**
     * @return Composite that owns this {@code MixinDetailDescriptor}. Never return {@code null}.
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

    final void addConstructor( ConstructorDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setMixin( this );
        constructors.add( aDescriptor );
    }

    final void addInjectedMethod( InjectedMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setMixin( this );
        injectedMethods.add( aDescriptor );
    }

    final void addInjectedField( InjectedFieldDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setMixin( this );
        injectedFields.add( aDescriptor );
    }

    @Override
    public String toString()
    {
        return descriptor.mixinClass().getSimpleName();
    }
}