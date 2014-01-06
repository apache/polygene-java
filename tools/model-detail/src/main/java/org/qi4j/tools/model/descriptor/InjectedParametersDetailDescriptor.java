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

import org.qi4j.api.composite.InjectedParametersDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public class InjectedParametersDetailDescriptor
{
    private final InjectedParametersDescriptor descriptor;
    private ConstructorDetailDescriptor constructor;
    private InjectedMethodDetailDescriptor method;

    InjectedParametersDetailDescriptor( InjectedParametersDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        descriptor = aDescriptor;
    }

    /**
     * @return Descriptor of this {@code InjectedParametersDetailDescriptor}. Never returns {@code null}.
     *
     * @since 0.5
     */
    public final InjectedParametersDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constructor that owns this {@code InjectedParametersDetailDescriptor}.
     *         If {@code null}, this {@code InjectedParametersDetailDescriptor} is owned by a method.
     *
     * @see #method()
     * @since 0.5
     */
    public final ConstructorDetailDescriptor constructor()
    {
        return constructor;
    }

    /**
     * @return Method that owns this {@code InjectedParametersDetailDescriptor}.
     *         If {@code null}, this {@code InjectedParametersDetailDescriptor} is owned by a constructor.
     *
     * @see #constructor() ()
     * @since 0.5
     */
    public final InjectedMethodDetailDescriptor method()
    {
        return method;
    }

    final void setConstructor( ConstructorDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        constructor = aDescriptor;
    }

    final void setMethod( InjectedMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        method = aDescriptor;
    }
}