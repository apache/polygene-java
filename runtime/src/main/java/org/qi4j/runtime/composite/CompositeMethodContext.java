/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.runtime.composite;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.qi4j.CompositeInstantiationException;
import org.qi4j.ParameterConstraint;
import org.qi4j.runtime.CompositeMethodInstancePool;
import org.qi4j.runtime.ConstraintInstance;
import org.qi4j.runtime.ConstraintInvocationHandler;
import org.qi4j.runtime.FragmentInvocationHandler;
import org.qi4j.runtime.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.runtime.SideEffectInvocationHandlerResult;
import org.qi4j.runtime.structure.ModuleContext;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeMethodBinding;
import org.qi4j.spi.composite.ConcernBinding;
import org.qi4j.spi.composite.ConstraintBinding;
import org.qi4j.spi.composite.ModifierModel;
import org.qi4j.spi.composite.ParameterBinding;
import org.qi4j.spi.composite.ParameterConstraintsBinding;
import org.qi4j.spi.composite.SideEffectBinding;
import org.qi4j.spi.dependency.ModifierInjectionContext;
import org.qi4j.spi.structure.ApplicationBinding;

/**
 * TODO
 */
public class CompositeMethodContext
{
    private CompositeMethodBinding compositeMethodBinding;
    private ApplicationBinding applicationBinding;
    private CompositeBinding compositeBinding;
    private Qi4jRuntime runtime;

    public CompositeMethodContext( CompositeMethodBinding compositeMethodBinding, ApplicationBinding applicationBinding, CompositeBinding compositeBinding, Qi4jRuntime runtime )
    {
        this.compositeMethodBinding = compositeMethodBinding;
        this.applicationBinding = applicationBinding;
        this.compositeBinding = compositeBinding;
        this.runtime = runtime;
    }

    public CompositeMethodBinding getCompositeMethodBinding()
    {
        return compositeMethodBinding;
    }

    CompositeMethodInstance newCompositeMethodInstance( ModuleContext moduleContext, CompositeMethodInstancePool compositeMethodInstancePool )
        throws CompositeInstantiationException
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        Method method = compositeMethodBinding.getCompositeMethodResolution().getCompositeMethodModel().getMethod();
        ClassLoader classloader = method.getDeclaringClass().getClassLoader();

        FragmentInvocationHandler mixinInvocationHandler = new FragmentInvocationHandler();

        // Instantiate and link concerns
        Object previousConcern = mixinInvocationHandler;
        {
            List<ConcernBinding> concernBindings = compositeMethodBinding.getConcernBindings();
            for( int i = concernBindings.size() - 1; i >= 0; i-- )
            {
                ConcernBinding concernBinding = concernBindings.get( i );

                Object modifies = getModifies( method, classloader, previousConcern, concernBinding.getConcernResolution().getConcernModel() );

                ModifierInjectionContext modifierContext = new ModifierInjectionContext( moduleContext.getCompositeBuilderFactory(),
                                                                                         moduleContext.getObjectBuilderFactory(),
                                                                                         moduleContext.getModuleBinding(),
                                                                                         compositeBinding,
                                                                                         proxyHandler,
                                                                                         modifies,
                                                                                         compositeMethodBinding.getCompositeMethodResolution().getCompositeMethodModel().getMethod(),
                                                                                         compositeMethodBinding.getMixinBinding(),
                                                                                         proxyHandler );
                previousConcern = runtime.getInstanceFactory().newInstance( concernBinding, modifierContext );
            }
        }

        // Instantiate constraints for parameters
        Iterable<ParameterBinding> parameterBindings = compositeMethodBinding.getParameterBindings();
        List<List<ConstraintInstance>> parameterConstraintInstances = new ArrayList<List<ConstraintInstance>>();
        boolean hasConstraints = false;
        for( ParameterBinding parameterBinding : parameterBindings )
        {
            List<ConstraintInstance> constraintInstances = new ArrayList<ConstraintInstance>();
            ParameterConstraintsBinding parameterConstraintsBinding = parameterBinding.getParameterConstraintsBinding();
            Map<Annotation, ConstraintBinding> constraintBindings = parameterConstraintsBinding.getConstraintBindings();
            for( Map.Entry<Annotation, ConstraintBinding> entry : constraintBindings.entrySet() )
            {
                Class<? extends ParameterConstraint> constraintType = entry.getValue().getConstraintResolution().getConstraintModel().getConstraintType();
                try
                {
                    ParameterConstraint constraintInstance = constraintType.newInstance();
                    constraintInstances.add( new ConstraintInstance( constraintInstance, entry.getKey() ) );
                    hasConstraints = true;
                }
                catch( Exception e )
                {
                    throw new CompositeInstantiationException( "Could not instantiate constraint " + constraintType.getName(), e );
                }
            }
            parameterConstraintInstances.add( constraintInstances );
        }
        if( hasConstraints )
        {
            previousConcern = new ConstraintInvocationHandler( proxyHandler, parameterConstraintInstances, previousConcern );
        }

        SideEffectInvocationHandlerResult sideEffectResult = new SideEffectInvocationHandlerResult();
        List<SideEffectBinding> sideEffectBindings = compositeMethodBinding.getSideEffectBindings();

        // Instantiate and link side-effects
        Object[] sideEffects = new Object[sideEffectBindings.size()];
        int i = 0;
        for( SideEffectBinding sideEffectBinding : sideEffectBindings )
        {
            Object modifies = getModifies( method, classloader, sideEffectResult, sideEffectBinding.getSideEffectResolution().getSideEffectModel() );

            ModifierInjectionContext modifierContext = new ModifierInjectionContext( moduleContext.getCompositeBuilderFactory(),
                                                                                     moduleContext.getObjectBuilderFactory(),
                                                                                     moduleContext.getModuleBinding(),
                                                                                     compositeBinding,
                                                                                     proxyHandler,
                                                                                     modifies,
                                                                                     method,
                                                                                     compositeMethodBinding.getMixinBinding(),
                                                                                     proxyHandler );
            sideEffects[ i++ ] = runtime.getInstanceFactory().newInstance( sideEffectBinding, modifierContext );
        }

        return new CompositeMethodInstance( previousConcern,
                                            sideEffects,
                                            sideEffectResult,
                                            mixinInvocationHandler,
                                            proxyHandler,
                                            compositeMethodInstancePool,
                                            method,
                                            method.getDeclaringClass() );
    }

    private Object getModifies( Method method, ClassLoader classloader, Object next, ModifierModel modifierModel )
    {
        Object modifies;
        if( modifierModel.isGeneric() )
        {
            if( next instanceof InvocationHandler )
            {
                modifies = next;
            }
            else
            {
                modifies = new FragmentInvocationHandler( next );
            }
        }
        else
        {
            if( next instanceof InvocationHandler )
            {
                modifies = Proxy.newProxyInstance( classloader, new Class[]{ method.getDeclaringClass() }, (InvocationHandler) next );
            }
            else
            {
                modifies = next;
            }
        }
        return modifies;
    }
}
