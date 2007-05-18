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
package org.qi4j.runtime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.List;
import org.qi4j.spi.object.InvocationInstance;
import org.qi4j.spi.object.ModifierInstance;
import org.qi4j.spi.object.ProxyReferenceInvocationHandler;
import org.qi4j.spi.object.ModifierInstanceFactory;
import org.qi4j.spi.object.InvocationInstancePool;

public final class InvocationInstancePoolImpl
    implements InvocationInstancePool
{
    private Map<Class, IdentityHashMap<Method, List<InvocationInstance>>> pools;
    private ThreadLocal<IdentityHashMap<Method, IdentityHashMap<Class, List<InvocationInstance>>>> threadInstances;
    private ModifierInstanceFactory factory;

    public InvocationInstancePoolImpl( ModifierInstanceFactory aFactory )
    {
        pools = new HashMap<Class, IdentityHashMap<Method, List<InvocationInstance>>>();
        factory = aFactory;
        threadInstances = new ThreadLocal<IdentityHashMap<Method, IdentityHashMap<Class, List<InvocationInstance>>>>()
        {

            protected IdentityHashMap<Method, IdentityHashMap<Class, List<InvocationInstance>>> initialValue()
            {
                return new IdentityHashMap<Method, IdentityHashMap<Class, List<InvocationInstance>>>();
            }
        };

    }

    public InvocationInstance get( Method method, Class bindingType, Object mixin )
    {
        IdentityHashMap<Method, IdentityHashMap<Class, List<InvocationInstance>>> instances = threadInstances.get();
        IdentityHashMap<Class, List<InvocationInstance>> stacks = instances.get( method );
        if( stacks == null )
        {
            stacks = new IdentityHashMap<Class, List<InvocationInstance>>();
            instances.put( method, stacks );
        }
        List<InvocationInstance> pool = stacks.get( bindingType );
        if( pool == null )
        {
            pool = new ArrayList<InvocationInstance>();
            stacks.put( bindingType, pool );
        }

        try
        {
            return pool.remove( pool.size() - 1 );
        }
        catch( ArrayIndexOutOfBoundsException e )
        {
            // Can not happen.
        }

        return newInstance( method, bindingType, mixin, pool );
    }

    public InvocationInstance newInstance( Method method, Class bindingType, Object mixin, List<InvocationInstance> aPool )
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        Class<?> declaringClass = method.getDeclaringClass();
        ModifierInstance interfaceInstance = factory.newInstance( declaringClass, bindingType, mixin.getClass(), proxyHandler );
        ModifierInstance mixinInstance = factory.newInstance( declaringClass, mixin.getClass(), mixin.getClass(), proxyHandler );

        return new InvocationInstanceImpl( interfaceInstance, mixinInstance, proxyHandler, aPool );
    }

    public List<InvocationInstance> getPool( Method method, Class modifierType )
    {
        IdentityHashMap<Method, IdentityHashMap<Class, List<InvocationInstance>>> instances = threadInstances.get();
        IdentityHashMap<Class, List<InvocationInstance>> stacks = instances.get( method );
        if( stacks == null )
        {
            stacks = new IdentityHashMap<Class, List<InvocationInstance>>();
            instances.put( method, stacks );
        }
        List<InvocationInstance> pool = stacks.get( modifierType );
        if( pool == null )
        {
            pool = new ArrayList<InvocationInstance>();
            stacks.put( modifierType, pool );
        }

        return pool;
    }

    public IdentityHashMap<Method, List<InvocationInstance>> getPool( Class bindingType )
    {
        IdentityHashMap<Method, List<InvocationInstance>> pool = pools.get( bindingType );
        if( pool == null )
        {
            pool = new IdentityHashMap<Method, List<InvocationInstance>>();
            pools.put( bindingType, pool );
        }

        return pool;
    }
}
