/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.api.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.stream.Stream;
import org.apache.zest.api.PolygeneAPI;
import org.apache.zest.api.structure.ModuleDescriptor;

/**
 * Thread-associated composites. This is basically a ThreadLocal which maintains a reference
 * to a TransientComposite instance for each thread. This can be used to implement various context
 * patterns without having to pass the context explicitly as a parameter to methods.
 */
public class CompositeContext<T extends TransientComposite>
    extends ThreadLocal<T>
{
    private ModuleDescriptor module;
    private Class<T> type;

    public CompositeContext( ModuleDescriptor module, Class<T> type )
    {
        this.module = module;
        this.type = type;
    }

    @Override
    protected T initialValue()
    {
        return module.instance().newTransient( type );
    }

    @SuppressWarnings( "unchecked" )
    public T proxy()
    {
        TransientComposite composite = get();

        Stream<Class<?>> types = PolygeneAPI.FUNCTION_COMPOSITE_INSTANCE_OF.apply( composite ).types();
        return (T) Proxy.newProxyInstance(
            composite.getClass().getClassLoader(),
            types.toArray( Class[]::new ),
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
