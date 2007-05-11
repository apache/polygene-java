/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package iop.runtime;

import iop.api.annotation.Modifies;
import iop.sample.domain.HelloWorldSpeakerProxy;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public final class ModifierInstance
{
    @Modifies Object firstModifier;
    Field lastModifies;
    Object lastModifier;
    LastModifierInvocationHandler lastHandler;

    public Object getFirstModifier()
    {
        return firstModifier;
    }

    public Field getLastModifies()
    {
        return lastModifies;
    }

    public Object getLastModifier()
    {
        return lastModifier;
    }

    public void setLastModifies( Field aLastModifies )
    {
        lastModifies = aLastModifies;
    }

    public void setLastModifier( Object aLastModifier )
    {
        lastModifier = aLastModifier;
    }

    public void setNextModifier(Object aNextModifier)
        throws IllegalAccessException
    {
        if (lastHandler == null)
        {
            lastHandler = new LastModifierInvocationHandler();
            try
            {
                Class nextInterface = aNextModifier.getClass().getInterfaces()[ 0 ];
                lastModifies.set( lastModifier, Proxy.newProxyInstance( nextInterface.getClassLoader(), new Class[]{ nextInterface }, lastHandler));
            }
            catch( IllegalAccessException e )
            {
                e.printStackTrace();
            }
        }
        lastHandler.setNext( aNextModifier );
    }

    static class LastModifierInvocationHandler
        implements InvocationHandler
    {
        Object next;

        public void setNext( Object aNext )
        {
            next = aNext;
        }

        public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
        {
            try
            {
                return method.invoke( next, args);
            }
            catch( InvocationTargetException e )
            {
                throw e.getTargetException();
            }
        }
    }
}
