/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.api.Qi4j;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Iterables;

import static org.qi4j.functional.Iterables.toArray;

/**
 * Thread-associated composites. This is basically a ThreadLocal which maintains a reference
 * to a TransientComposite instance for each thread. This can be used to implement various context
 * patterns without having to pass the context explicitly as a parameter to methods.
 */
public class CompositeContext<T extends TransientComposite>
    extends ThreadLocal<T>
{
    private Module module;
    private Class<T> type;

    public CompositeContext( Module module, Class<T> type )
    {
        this.module = module;
        this.type = type;
    }

    @Override
    protected T initialValue()
    {
        return module.newTransient( type );
    }

    @SuppressWarnings( "unchecked" )
    public T proxy()
    {
        TransientComposite composite = get();

        Iterable<Class<?>> types = Qi4j.FUNCTION_COMPOSITE_INSTANCE_OF.map( composite ).types();
        return (T) Proxy.newProxyInstance(
            composite.getClass().getClassLoader(),
            toArray( Class.class, Iterables.<Class>cast( types ) ),
            new ContextInvocationhandler() );
    }

    private class ContextInvocationhandler
        implements InvocationHandler
    {

        @Override
        public Object invoke( Object object, Method method, Object[] objects )
            throws Throwable
        {
            try
            {
                return method.invoke( get(), objects );
            }
            catch( InvocationTargetException e )
            {
                throw e.getTargetException();
            }
        }
    }
}
