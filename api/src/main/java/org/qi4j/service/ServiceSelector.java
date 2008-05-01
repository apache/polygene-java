/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This class helps you select a particular service
 * from a list. Provide a Selector which does the actual
 * selection fro the list. A common case is to select
 * based on identity of the service, which you can do this way:
 * new ServiceSelector<MyService>(services, ServiceSelector.id("someId"))
 */
public class ServiceSelector<T>
{
    public static Selector<Object> id( final String anId )
    {
        return new Selector<Object>()
        {
            public ServiceReference<Object> select( Iterable<ServiceReference<Object>> services )
            {
                for( ServiceReference<Object> service : services )
                {
                    if( service.identity().get().equals( anId ) )
                    {
                        return service;
                    }
                }
                return null;
            }
        };
    }

    public static Selector<Object> firstActive()
    {
        return new Selector<Object>()
        {
            public ServiceReference<Object> select( Iterable<ServiceReference<Object>> services )
            {
                for( ServiceReference<Object> service : services )
                {
                    if( service.isActive() )
                    {
                        return service;
                    }
                }
                return null;
            }
        };
    }

    private Iterable<ServiceReference<T>> services;
    private Selector selector;

    public ServiceSelector( Iterable<ServiceReference<T>> services, Selector selector )
    {
        this.services = services;
        this.selector = selector;
    }

    public T get()
    {
        ServiceReference<T> serviceRef = selector.select( services );

        if( serviceRef != null )
        {
            return serviceRef.get();
        }
        else
        {
            return null;
        }
    }

    public T proxy()
    {
        final T instance = get();
        InvocationHandler handler = new InvocationHandler()
        {
            public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
            {
                return method.invoke( instance, objects );
            }
        };

        return (T) Proxy.newProxyInstance( instance.getClass().getClassLoader(), instance.getClass().getInterfaces(), handler );
    }

    public interface Selector<T>
    {
        ServiceReference<T> select( Iterable<ServiceReference<T>> services );
    }
}
