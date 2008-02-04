/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.general.test.model;

import static java.lang.Thread.currentThread;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import static java.lang.reflect.Proxy.newProxyInstance;
import org.qi4j.composite.scope.ConcernFor;
import org.qi4j.library.general.model.Descriptor;
import org.qi4j.property.Property;

public class DescriptorConcern
    implements Descriptor
{
    private static final Class[] INTERFACES = { Property.class };

    @ConcernFor
    private Descriptor next;

    @SuppressWarnings( "unchecked" )
    public Property<String> displayValue()
    {
        final Property<String> displayValueProperty = next.displayValue();
        ClassLoader currentThreadClassLoader = currentThread().getContextClassLoader();
        return (Property<String>) newProxyInstance( currentThreadClassLoader, INTERFACES, new InvocationHandler()
        {
            public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
            {
                String methodName = method.getName();
                if( "get".equals( methodName ) )
                {
                    return "My name is " + displayValueProperty.get();
                }
                return method.invoke( displayValueProperty, args );
            }
        } );
    }
}
