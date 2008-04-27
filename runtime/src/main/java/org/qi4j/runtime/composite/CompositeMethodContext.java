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

package org.qi4j.runtime.composite;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.qi4j.composite.Constraint;
import org.qi4j.composite.InstantiationException;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.runtime.entity.association.AssociationContext;
import org.qi4j.runtime.property.PropertyContext;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeMethodBinding;
import org.qi4j.spi.composite.ConcernBinding;
import org.qi4j.spi.composite.ConstraintBinding;
import org.qi4j.spi.composite.ConstraintModel;
import org.qi4j.spi.composite.ConstraintResolution;
import org.qi4j.spi.composite.ConstraintsBinding;
import org.qi4j.spi.composite.ModifierModel;
import org.qi4j.spi.composite.ParameterBinding;
import org.qi4j.spi.composite.SideEffectBinding;
import org.qi4j.spi.injection.ModifierInjectionContext;
import org.qi4j.spi.structure.ApplicationBinding;

/**
 * TODO
 */
public final class CompositeMethodContext
{
    private CompositeMethodBinding compositeMethodBinding;
    private ApplicationBinding applicationBinding;
    private CompositeBinding compositeBinding;
    private Qi4jRuntime runtime;
    private PropertyContext propertyContext;
    private AssociationContext associationContext;

    public CompositeMethodContext( CompositeMethodBinding compositeMethodBinding, ApplicationBinding applicationBinding, CompositeBinding compositeBinding, Qi4jRuntime runtime, PropertyContext propertyContext, AssociationContext associationContext )
    {
        this.associationContext = associationContext;
        this.propertyContext = propertyContext;
        this.compositeMethodBinding = compositeMethodBinding;
        this.applicationBinding = applicationBinding;
        this.compositeBinding = compositeBinding;
        this.runtime = runtime;
    }

    public CompositeMethodBinding getCompositeMethodBinding()
    {
        return compositeMethodBinding;
    }

    public PropertyContext getPropertyContext()
    {
        return propertyContext;
    }

    public AssociationContext getAssociationContext()
    {
        return associationContext;
    }

    CompositeMethodInstance newCompositeMethodInstance( ModuleInstance moduleInstance, CompositeMethodInstancePool compositeMethodInstancePool )
        throws InstantiationException
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        Method method = compositeMethodBinding.getCompositeMethodResolution().getCompositeMethodModel().getMethod();
        ClassLoader classloader = method.getDeclaringClass().getClassLoader();

        FragmentInvocationHandler mixinInvocationHandler;
        Class modelClass = compositeMethodBinding.getMixinBinding().getMixinResolution().getMixinModel().getModelClass();
        if( InvocationHandler.class.isAssignableFrom( modelClass ) && !method.getDeclaringClass().isAssignableFrom( modelClass ) )
        {
            mixinInvocationHandler = new GenericFragmentInvocationHandler();
        }
        else
        {
            mixinInvocationHandler = new TypedFragmentInvocationHandler();
        }

        // Instantiate and link concerns
        Object previousConcern = mixinInvocationHandler;
        {
            List<ConcernBinding> concernBindings = compositeMethodBinding.getConcernBindings();
            for( int i = concernBindings.size() - 1; i >= 0; i-- )
            {
                ConcernBinding concernBinding = concernBindings.get( i );

                Object modifies = getModifies( method, classloader, previousConcern, concernBinding.getConcernResolution().getConcernModel() );

                ModifierInjectionContext modifierContext = new ModifierInjectionContext( moduleInstance.getStructureContext(),
                                                                                         moduleInstance.getModule(),
                                                                                         compositeBinding,
                                                                                         proxyHandler,
                                                                                         modifies,
                                                                                         compositeMethodBinding,
                                                                                         compositeMethodBinding.getMixinBinding()
                );
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
            ConstraintsBinding constraintsBinding = parameterBinding.getConstraintsBinding();

            if( constraintsBinding == null )
            {
                continue;
            }

            Map<Annotation, ConstraintBinding> constraintBindings = constraintsBinding.getConstraintBindings();
            for( Map.Entry<Annotation, ConstraintBinding> entry : constraintBindings.entrySet() )
            {
                ConstraintBinding constraintBinding = entry.getValue();
                ConstraintResolution constraintResolution = constraintBinding.getConstraintResolution();
                ConstraintModel constraintModel = constraintResolution.getConstraintModel();
                Class<? extends Constraint> constraintType = constraintModel.getConstraintType();
                try
                {
                    Constraint constraintInstance = constraintType.newInstance();
                    constraintInstances.add( new ConstraintInstance( constraintInstance, entry.getKey() ) );
                    hasConstraints = true;
                }
                catch( Exception e )
                {
                    throw new InstantiationException( "Could not instantiate constraint " + constraintType.getName(), e );
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

            ModifierInjectionContext modifierContext = new ModifierInjectionContext( moduleInstance.getStructureContext(),
                                                                                     moduleInstance.getModule(),
                                                                                     compositeBinding,
                                                                                     proxyHandler,
                                                                                     modifies,
                                                                                     compositeMethodBinding,
                                                                                     compositeMethodBinding.getMixinBinding()
            );
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
                modifies = new TypedFragmentInvocationHandler( next );
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
