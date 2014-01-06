/*
 * Copyright (c) 2008, Edward Yakop. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.tools.model.descriptor;

import java.util.LinkedList;
import java.util.List;
import org.qi4j.api.sideeffect.SideEffectDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class MethodSideEffectDetailDescriptor
    implements InjectableDetailDescriptor
{
    private final SideEffectDescriptor descriptor;
    private MethodSideEffectDetailDescriptor sideEffects;

    private final List<ConstructorDetailDescriptor> constructors;
    private final List<InjectedMethodDetailDescriptor> injectedMethods;
    private final List<InjectedFieldDetailDescriptor> injectedFields;

    MethodSideEffectDetailDescriptor( SideEffectDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;
        constructors = new LinkedList<ConstructorDetailDescriptor>();
        injectedMethods = new LinkedList<InjectedMethodDetailDescriptor>();
        injectedFields = new LinkedList<InjectedFieldDetailDescriptor>();
    }

    /**
     * @return Descriptor of this {@code MethodSideEffectDetailDescriptor}. Never returns {@code null}.
     *
     * @since 0.5
     */
    public final SideEffectDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Method side effects that owns this {@code MethodSideEffectDetailDescriptor}. Never returns {@code null}.
     *
     * @since 0.5
     */
    public final MethodSideEffectDetailDescriptor sideEffects()
    {
        return sideEffects;
    }

    /**
     * @return Constructors of this {@code MethodSideEffectDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    @Override
    public final Iterable<ConstructorDetailDescriptor> constructors()
    {
        return constructors;
    }

    /**
     * @return Injected methods of this {@code MethodSideEffectDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    @Override
    public final Iterable<InjectedMethodDetailDescriptor> injectedMethods()
    {
        return injectedMethods;
    }

    /**
     * @return Injected fields of this {@code MethodSideEffectDetailDescriptor}. Never return {@code null}.
     *
     * @since 0.5
     */
    @Override
    public final Iterable<InjectedFieldDetailDescriptor> injectedFields()
    {
        return injectedFields;
    }

    final void setSideEffects( MethodSideEffectDetailDescriptor aDescriptor )
    {
        validateNotNull( "aDescriptor", aDescriptor );

        sideEffects = aDescriptor;
    }

   @Override
   public String toString()
   {
      return descriptor.modifierClass().getName();
   }

   final void addConstructor( ConstructorDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setMethodSideEffect( this );
        constructors.add( aDescriptor );
    }

    final void addInjectedMethod( InjectedMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setMethodSideEffect( this );
        injectedMethods.add( aDescriptor );
    }

    final void addInjectedField( InjectedFieldDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        aDescriptor.setMethodSideEffect( this );
        injectedFields.add( aDescriptor );
    }
}