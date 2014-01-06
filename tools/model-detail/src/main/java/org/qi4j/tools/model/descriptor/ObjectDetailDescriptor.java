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
import org.qi4j.api.object.ObjectDescriptor;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

public final class ObjectDetailDescriptor
    implements InjectableDetailDescriptor
{
    private final ObjectDescriptor descriptor;
    private ModuleDetailDescriptor module;
    private final List<ConstructorDetailDescriptor> constructors = new LinkedList<>();
    private final List<InjectedMethodDetailDescriptor> injectedMethods = new LinkedList<>();
    private final List<InjectedFieldDetailDescriptor> injectedFields = new LinkedList<>();

    ObjectDetailDescriptor( ObjectDescriptor descriptor )
    {
        validateNotNull( "ObjectDescriptor", descriptor );
        this.descriptor = descriptor;
    }

    /**
     * @return Descriptor of this {@code ObjectDetailDescriptor}. Never return {@code null}.
     */
    public final ObjectDescriptor descriptor()
    {
        return descriptor;
    }

    /**
     * @return Constructors of this {@code ObjectDetailDescriptor}. Never return {@code null}.
     */
    @Override
    public final Iterable<ConstructorDetailDescriptor> constructors()
    {
        return constructors;
    }

    /**
     * @return Injected methods of this {@code ObjectDetailDescriptor}. Never return {@code null}.
     */
    @Override
    public final Iterable<InjectedMethodDetailDescriptor> injectedMethods()
    {
        return injectedMethods;
    }

    /**
     * @return Injected fields of this {@code ObjectDetailDescriptor}. Never return {@code null}.
     */
    @Override
    public final Iterable<InjectedFieldDetailDescriptor> injectedFields()
    {
        return injectedFields;
    }

    /**
     * @return Module that own this {@code ObjectDetailDescriptor}. Never return {@code null}.
     */
    public final ModuleDetailDescriptor module()
    {
        return module;
    }

    final void setModule( ModuleDetailDescriptor descriptor )
    {
        validateNotNull( "ModuleDetailDescriptor", descriptor );
        module = descriptor;
    }

    final void addConstructor( ConstructorDetailDescriptor descriptor )
    {
        validateNotNull( "ConstructorDetailDescriptor", descriptor );
        descriptor.setObject( this );
        constructors.add( descriptor );
    }

    final void addInjectedMethod( InjectedMethodDetailDescriptor descriptor )
    {
        validateNotNull( "InjectedMethodDetailDescriptor", descriptor );
        descriptor.setObject( this );
        injectedMethods.add( descriptor );
    }

    final void addInjectedField( InjectedFieldDetailDescriptor descriptor )
    {
        validateNotNull( "InjectedFieldDetailDescriptor", descriptor );
        descriptor.setObject( this );
        injectedFields.add( descriptor );
    }

    @Override
    public String toString()
    {
        return descriptor.toString();
    }
}
