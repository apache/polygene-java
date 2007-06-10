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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.InvocationContext;
import org.qi4j.api.ObjectFactory;
import org.qi4j.api.ObjectInstantiationException;
import org.qi4j.api.model.CompositeInterface;
import org.qi4j.api.model.Modifier;

/**
 * TODO
 */
public final class ObjectContext
{
    private CompositeInterface composite;
    private ObjectFactory objectFactory;
    private FragmentFactory fragmentFactory;
    private ConcurrentHashMap<Method, List<InvocationInstance>> invocationInstancePool;


    public ObjectContext( CompositeInterface aComposite, ObjectFactory aObjectFactory, FragmentFactory aFragmentFactory )
    {
        composite = aComposite;
        objectFactory = aObjectFactory;
        fragmentFactory = aFragmentFactory;
        invocationInstancePool = new ConcurrentHashMap<Method, List<InvocationInstance>>();
    }

    public CompositeInterface getCompositeInterface()
    {
        return composite;
    }

    public ObjectFactory getObjectFactory()
    {
        return objectFactory;
    }

    public FragmentFactory getFragmentFactory()
    {
        return fragmentFactory;
    }

    public InvocationInstance getInvocationInstance( Method method )
    {
        List<InvocationInstance> instances = invocationInstancePool.get( method );

        if( instances == null )
        {
            instances = new ArrayList<InvocationInstance>();
            invocationInstancePool.put( method, instances );
        }

        InvocationInstance invocationInstance;
        int size = instances.size();
        if( size > 0 )
        {
            invocationInstance = instances.remove( size - 1 );
        }
        else
        {
            invocationInstance = newInstance( method );
        }

        return invocationInstance;
    }

    public InvocationInstance newInstance( Method method )
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
                Object modifierInstance = fragmentFactory.newFragment( modifier, composite );

                if( firstModifier == null )
                {
                    firstModifier = modifierInstance;
                }

                // @Uses
                for( Field usesField : modifier.getUsesFields() )
                {
                    Object usesProxy = Proxy.newProxyInstance( usesField.getType().getClassLoader(), new Class[]{ usesField.getType() }, proxyHandler );
                    usesField.set( modifierInstance, usesProxy );
                }

                // @Dependency
                for( Field dependencyField : modifier.getDependencyFields() )
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
                    else if( dependencyField.getType().equals( Method.class ) )
                    {
                        Class<? extends Object> fragmentClass = composite.getMixin( method.getDeclaringClass() ).getFragmentClass();
                        Method dependencyMethod;
                        if( InvocationHandler.class.isAssignableFrom( fragmentClass ) )
                        {
                            dependencyMethod = method;
                        }
                        else
                        {
                            try
                            {
                                dependencyMethod = fragmentClass.getMethod( method.getName(), method.getParameterTypes() );
                            }
                            catch( NoSuchMethodException e )
                            {
                                throw new ObjectInstantiationException( "Could not resolve @Dependency to method in mixin " + fragmentClass.getName() + " for composite " + composite.getCompositeInterface().getName(), e );
                            }
                        }
                        dependencyField.set( modifierInstance, dependencyMethod );
                    }
                }

                // @Modifies
                if( previousModifies != null )
                {
                    if( lastModifier instanceof InvocationHandler )
                    {
                        if( modifierInstance instanceof InvocationHandler )
                        {
                            // IH to another IH
                            previousModifies.set( lastModifier, modifierInstance );
                        }
                        else
                        {
                            // IH to an object modifier
                            FragmentInvocationHandler handler = new FragmentInvocationHandler( modifierInstance );
                            previousModifies.set( lastModifier, handler );
                        }
                    }
                    else
                    {
                        if( modifierInstance instanceof InvocationHandler )
                        {
                            // Object modifier to IH modifier
                            Object modifierProxy = Proxy.newProxyInstance( previousModifies.getType().getClassLoader(), new Class[]{ previousModifies.getType() }, (InvocationHandler) modifierInstance );
                            previousModifies.set( lastModifier, modifierProxy );
                        }
                        else
                        {
                            // Object modifier to another object modifier
                            previousModifies.set( lastModifier, modifierInstance );
                        }
                    }
                }

                lastModifier = modifierInstance;
                previousModifies = modifier.getModifiesField();
            }


            FragmentInvocationHandler mixinInvocationHandler = null;
            if( previousModifies != null )
            {
                mixinInvocationHandler = new FragmentInvocationHandler();
                if( lastModifier instanceof InvocationHandler )
                {
                    previousModifies.set( lastModifier, mixinInvocationHandler );
                }
                else
                {
                    Object mixinProxy = Proxy.newProxyInstance( composite.getMixin( method.getDeclaringClass() ).getFragmentClass().getClassLoader(), new Class[]{ previousModifies.getType() }, mixinInvocationHandler );
                    previousModifies.set( lastModifier, mixinProxy );
                }
            }

            return new InvocationInstance( firstModifier, mixinInvocationHandler, proxyHandler, invocationInstancePool.get( method ) );
        }
        catch( IllegalAccessException e )
        {
            throw new ObjectInstantiationException( "Could not create invocation instance", e );
        }
    }

}
