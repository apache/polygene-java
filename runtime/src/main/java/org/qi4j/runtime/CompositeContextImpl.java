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
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.DependencyResolver;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.InvocationContext;
import org.qi4j.api.annotation.Dependency;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.model.CompositeContext;
import org.qi4j.api.model.CompositeObject;
import org.qi4j.api.model.ModifierModel;

/**
 * TODO
 */
public final class CompositeContextImpl
    implements CompositeContext
{
    private CompositeObject compositeObject;
    private CompositeFactoryImpl compositeFactory;
    private FragmentFactory fragmentFactory;
    private ConcurrentHashMap<Method, List<InvocationInstance>> invocationInstancePool;


    public CompositeContextImpl( CompositeObject aComposite, CompositeFactoryImpl aCompositeFactory, FragmentFactory aFragmentFactory )
    {
        compositeObject = aComposite;
        compositeFactory = aCompositeFactory;
        fragmentFactory = aFragmentFactory;
        invocationInstancePool = new ConcurrentHashMap<Method, List<InvocationInstance>>();
    }

    public CompositeObject getCompositeObject()
    {
        return compositeObject;
    }

    public CompositeFactory getCompositeFactory()
    {
        return compositeFactory;
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
            instances = new ArrayList<InvocationInstance>(1);
            invocationInstancePool.put( method, instances );
        }

        synchronized( instances)
        {
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
    }

    public InvocationInstance newInstance( Method method )
    {
        try
        {
            ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();

            List<ModifierModel> modifierModels = compositeObject.getModifiers( method );
            Object firstModifier = null;
            Object lastModifier = null;
            Field previousModifies = null;
            for( ModifierModel modifierModel : modifierModels )
            {
                Object modifierInstance = fragmentFactory.newFragment( modifierModel, compositeObject );

                if( firstModifier == null )
                {
                    firstModifier = modifierInstance;
                }

                // @Uses
                for( Field usesField : modifierModel.getUsesFields() )
                {
                    if (compositeObject.isAssignableFrom( usesField.getType()))
                    {
                        Object usesProxy = Proxy.newProxyInstance( usesField.getType().getClassLoader(), new Class[]{ usesField.getType() }, proxyHandler );
                        usesField.set( modifierInstance, usesProxy );
                    } else
                    {
                        if (!usesField.getAnnotation( Uses.class).optional())
                            throw new CompositeInstantiationException( "Could not resolve @Uses field in mixin " + modifierModel.getFragmentClass().getName() + " for composite " + compositeObject.getCompositeInterface().getName());
                    }
                }

                // @Dependency
                for( Field dependencyField : modifierModel.getDependencyFields() )
                {
                    if( dependencyField.getType().equals( InvocationContext.class ) )
                    {
                        dependencyField.set( modifierInstance, proxyHandler );
                    }
                    else if( dependencyField.getType().equals( Method.class ) )
                    {
                        Class<? extends Object> fragmentClass = compositeObject.locateMixin( method.getDeclaringClass() ).getFragmentClass();
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
                                throw new CompositeInstantiationException( "Could not resolve @Dependency to method in mixin " + fragmentClass.getName() + " for composite " + compositeObject.getCompositeInterface().getName(), e );
                            }
                        }
                        dependencyField.set( modifierInstance, dependencyMethod );
                    } else
                    {
                        resolveDependency( dependencyField, modifierInstance);
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
                            // IH to an object modifierModel
                            FragmentInvocationHandler handler = new FragmentInvocationHandler( modifierInstance );
                            previousModifies.set( lastModifier, handler );
                        }
                    }
                    else
                    {
                        if( modifierInstance instanceof InvocationHandler )
                        {
                            // Object modifierModel to IH modifierModel
                            Object modifierProxy = Proxy.newProxyInstance( previousModifies.getType().getClassLoader(), new Class[]{ previousModifies.getType() }, (InvocationHandler) modifierInstance );
                            previousModifies.set( lastModifier, modifierProxy );
                        }
                        else
                        {
                            // Object modifierModel to another object modifierModel
                            previousModifies.set( lastModifier, modifierInstance );
                        }
                    }
                }

                lastModifier = modifierInstance;
                previousModifies = modifierModel.getModifiesField();
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
                    ClassLoader loader = previousModifies.getType().getClassLoader();
                    Object mixinProxy = Proxy.newProxyInstance( loader, new Class[]{ previousModifies.getType() }, mixinInvocationHandler );
                    previousModifies.set( lastModifier, mixinProxy );
                }
            }

            return new InvocationInstance( firstModifier, mixinInvocationHandler, proxyHandler, invocationInstancePool.get( method ) );
        }
        catch( IllegalAccessException e )
        {
            throw new CompositeInstantiationException( "Could not create invocation instance", e );
        }
    }

    public void resolveDependency( Field dependencyField, Object instance)
        throws CompositeInstantiationException
    {
        Object currentDependency = null;
        for( DependencyResolver resolver : compositeFactory.getDependencyResolvers() )
        {
            Object dependency = resolver.resolveDependency( dependencyField, this );
            if (currentDependency != null)
                throw new CompositeInstantiationException("Dependency "+dependencyField.getName()+" in mixin "+dependencyField.getDeclaringClass().getName() + " for composite "+compositeObject.getCompositeInterface()+" has ambiguous resolutions");
            currentDependency = dependency;
        }

        if (currentDependency == null)
        {
            // No object found, check if it's optional
            if (!dependencyField.getAnnotation( Dependency.class).optional())
                throw new CompositeInstantiationException("Dependency "+dependencyField.getName()+" in mixin "+dependencyField.getDeclaringClass().getName() + " for composite "+compositeObject.getCompositeInterface()+" could not be resolved");
        } else
        {
            // Set dependency
            try
            {
                dependencyField.set( instance, currentDependency);
            }
            catch( IllegalAccessException e )
            {
                throw new CompositeInstantiationException("Dependency "+dependencyField.getName()+" in mixin "+dependencyField.getDeclaringClass().getName() + " for composite "+compositeObject.getCompositeInterface()+" could not be set", e);
            }
        }
    }
}
