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

import java.util.Map;
import java.util.Stack;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Method;

public final class InvocationInstancePool
{
    ThreadLocal<IdentityHashMap<Method, IdentityHashMap<Class, ArrayList<InvocationInstance>>>> threadInstances = new ThreadLocal<IdentityHashMap<Method, IdentityHashMap<Class, ArrayList<InvocationInstance>>>>()
    {

        protected IdentityHashMap<Method, IdentityHashMap<Class, ArrayList<InvocationInstance>>> initialValue()
        {
            return new IdentityHashMap<Method, IdentityHashMap<Class, ArrayList<InvocationInstance>>>();
        }
    };
    Map<Class, IdentityHashMap<Method, ArrayList<InvocationInstance>>> pools = new HashMap<Class, IdentityHashMap<Method, ArrayList<InvocationInstance>>>();
//    Map<Method, Map<Class, LinkedList<InvocationInstance>>> instances = new IdentityHashMap<Method, Map<Class, LinkedList<InvocationInstance>>>();

    ModifierInstanceFactory factory;

    public InvocationInstancePool( ModifierInstanceFactory aFactory )
    {
        factory = aFactory;
    }

    public InvocationInstance get( Method method, Class bindingType , Object mixin)
    {
        IdentityHashMap<Method, IdentityHashMap<Class, ArrayList<InvocationInstance>>> instances = threadInstances.get();
        IdentityHashMap<Class, ArrayList<InvocationInstance>> stacks = instances.get( method );
        if (stacks == null)
        {
            stacks = new IdentityHashMap<Class, ArrayList<InvocationInstance>>( );
            instances.put( method, stacks);
        }
        ArrayList<InvocationInstance> pool = stacks.get( bindingType);
        if( pool == null )
        {
            pool = new ArrayList<InvocationInstance>();
            stacks.put( bindingType, pool);
        }

        try
        {
            return pool.remove( pool.size()-1);
        }
        catch( ArrayIndexOutOfBoundsException e )
        {
        }

        return newInstance( method, bindingType, mixin, pool );
    }

    public InvocationInstance newInstance( Method method, Class bindingType, Object mixin, ArrayList<InvocationInstance> aPool )
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        Class<?> declaringClass = method.getDeclaringClass();
        ModifierInstance interfaceInstance = factory.newInstance( declaringClass, bindingType, mixin.getClass(), proxyHandler );
        ModifierInstance mixinInstance = factory.newInstance( declaringClass, mixin.getClass(), mixin.getClass(), proxyHandler );

        return new InvocationInstance(interfaceInstance, mixinInstance, proxyHandler, aPool );
    }

    public ArrayList<InvocationInstance> getPool( Method method, Class modifierType)
    {
        IdentityHashMap<Method, IdentityHashMap<Class, ArrayList<InvocationInstance>>> instances = threadInstances.get();
        IdentityHashMap<Class, ArrayList<InvocationInstance>> stacks = instances.get( method );
        if (stacks == null)
        {
            stacks = new IdentityHashMap<Class, ArrayList<InvocationInstance>>( );
            instances.put( method, stacks);
        }
        ArrayList<InvocationInstance> pool = stacks.get( modifierType);
        if( pool == null )
        {
            pool = new ArrayList<InvocationInstance>();
            stacks.put( modifierType, pool);
        }

        return pool;
    }
    public IdentityHashMap<Method, ArrayList<InvocationInstance>> getPool( Class bindingType)
    {
        IdentityHashMap<Method, ArrayList<InvocationInstance>> pool = pools.get( bindingType );
        if( pool == null )
        {
            pool = new IdentityHashMap<Method, ArrayList<InvocationInstance>>();
            pools.put( bindingType, pool);
        }

        return pool;
    }
}
