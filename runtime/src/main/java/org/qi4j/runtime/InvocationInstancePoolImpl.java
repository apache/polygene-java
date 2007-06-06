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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.Composite;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.InvocationContext;
import org.qi4j.api.Modifier;
import org.qi4j.api.ObjectFactory;
import org.qi4j.api.ObjectInstantiationException;
import org.qi4j.spi.object.InvocationInstance;
import org.qi4j.spi.object.InvocationInstancePool;
import org.qi4j.spi.object.ProxyReferenceInvocationHandler;

public final class InvocationInstancePoolImpl
    implements InvocationInstancePool
{
    private Map<Class, ConcurrentHashMap<Method, List<InvocationInstance>>> pools;
    private ThreadLocal<IdentityHashMap<Method, IdentityHashMap<Class, List<InvocationInstance>>>> threadInstances;
    private ObjectFactory objectFactory;
    private FragmentFactory fragmentFactory;

    public InvocationInstancePoolImpl( ObjectFactory anObjectFactory, FragmentFactory aFragmentFactory)
    {
        pools = new HashMap<Class, ConcurrentHashMap<Method, List<InvocationInstance>>>();
        objectFactory = anObjectFactory;
        fragmentFactory = aFragmentFactory;
        threadInstances = new ThreadLocal<IdentityHashMap<Method, IdentityHashMap<Class, List<InvocationInstance>>>>()
        {

            protected IdentityHashMap<Method, IdentityHashMap<Class, List<InvocationInstance>>> initialValue()
            {
                return new IdentityHashMap<Method, IdentityHashMap<Class, List<InvocationInstance>>>();
            }
        };

    }

    public InvocationInstance newInstance( Method method, Composite composite, List<InvocationInstance> aPool )
    {
        try
        {
            ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();

            List<Modifier> modifiers = composite.getModifiers( method );
            Object firstModifier = null;
            Object lastModifier = null;
            Field previousModifies = null;
            for( Modifier modifier : modifiers )
            {
                Object modifierInstance = fragmentFactory.newInstance( modifier.getFragmentClass() );

                if (firstModifier == null)
                    firstModifier = modifierInstance;

                // @Uses
                for( Field usesField : modifier.getUsesFields())
                {
                    Object usesProxy = Proxy.newProxyInstance( usesField.getType().getClassLoader(), new Class[]{ usesField.getType() }, proxyHandler );
                    usesField.set( modifierInstance, usesProxy );
                }

                // @Dependency
                for( Field dependencyField : modifier.getDependencyFields())
                {
                    if( dependencyField.getType().equals( ObjectFactory.class ) )
                    {
                        dependencyField.set( modifierInstance, objectFactory );
                    }
                    else if( dependencyField.getType().equals( FragmentFactory.class ) )
                    {
                        dependencyField.set( modifierInstance, fragmentFactory );
                    }
                    else if( dependencyField.getType().equals( InvocationContext.class ) )
                    {
                        dependencyField.set( modifierInstance, proxyHandler );
                    }
                    else if (dependencyField.getType().equals( Method.class))
                    {
                        Class<? extends Object> fragmentClass = composite.getMixin( method.getDeclaringClass() ).getFragmentClass();
                        Method dependencyMethod;
                        if (InvocationHandler.class.isAssignableFrom( fragmentClass))
                        {
                            dependencyMethod = method;
                        } else
                        {
                            try
                            {
                                dependencyMethod = fragmentClass.getMethod( method.getName(), method.getParameterTypes());
                            }
                            catch( NoSuchMethodException e )
                            {
                                throw new ObjectInstantiationException("Could not resolve @Dependency to method in mixin "+fragmentClass.getName()+" for composite "+composite.getCompositeClass().getName(), e);
                            }
                        }
                        dependencyField.set( modifierInstance, dependencyMethod);
                    }
                }

                // @Modifies
                if (previousModifies != null)
                {
                    if( lastModifier instanceof InvocationHandler)
                    {
                        if (modifierInstance instanceof InvocationHandler )
                        {
                            // One IH to another IH
                            previousModifies.set( lastModifier, modifierInstance);
                        } else
                        {
                            // IH to an object modifier
                            FragmentInvocationHandler handler = new FragmentInvocationHandler( modifierInstance );
                            previousModifies.set( lastModifier, handler );
                        }
                    }
                    else
                    {
                        // Object modifier to another object modifier
                        previousModifies.set( lastModifier, modifierInstance );
                    }
                }

                lastModifier = modifierInstance;
                previousModifies = modifier.getModifiesField();
            }


            FragmentInvocationHandler mixinInvocationHandler = null;
            if (previousModifies != null)
            {
                mixinInvocationHandler = new FragmentInvocationHandler();
                if (lastModifier instanceof InvocationHandler)
                {
                    previousModifies.set( lastModifier, mixinInvocationHandler );
                } else
                {
                    Object mixinProxy = Proxy.newProxyInstance( composite.getMixin( method.getDeclaringClass()).getFragmentClass().getClassLoader(), new Class[]{ previousModifies.getType() }, mixinInvocationHandler);
                    previousModifies.set( lastModifier, mixinProxy );
                }
            }

            return new InvocationInstanceImpl( firstModifier, lastModifier, mixinInvocationHandler, proxyHandler, aPool );
        }
        catch( IllegalAccessException e )
        {
            throw new ObjectInstantiationException("Could not create invocation instance", e);
        }
    }

    public ConcurrentHashMap<Method, List<InvocationInstance>> getPool( Class compositeClass )
    {
        ConcurrentHashMap<Method, List<InvocationInstance>> pool = pools.get( compositeClass );
        if( pool == null )
        {
            pool = new ConcurrentHashMap<Method, List<InvocationInstance>>();
            pools.put( compositeClass, pool );
        }

        return pool;
    }
}
