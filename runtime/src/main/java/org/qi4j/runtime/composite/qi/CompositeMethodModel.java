/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
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

package org.qi4j.runtime.composite.qi;

import java.lang.reflect.Method;
import org.qi4j.runtime.composite.CompositeMethodInstance;
import org.qi4j.runtime.composite.CompositeMethodInstancePool;
import org.qi4j.runtime.composite.FragmentInvocationHandler;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.composite.SynchronizedCompositeMethodInstancePool;
import org.qi4j.runtime.structure.qi.ModuleInstance;

/**
 * TODO
 */
public final class CompositeMethodModel
{
    // Model
    private Method method;
    private ParametersModel parameters;

    // Resolution
    private CompositeModel composite;

    // Context
    private CompositeMethodInstancePool instancePool = new SynchronizedCompositeMethodInstancePool();

    public CompositeMethodModel( Method method, ParametersModel parameters, CompositeModel composite )
    {
        this.method = method;
        this.parameters = parameters;
        this.composite = composite;
    }

    // Model
    public Method method()
    {
        return method;
    }

    public ParametersModel parameters()
    {
        return parameters;
    }

    // Resolution
    public CompositeModel composite()
    {
        return composite;
    }

    // Binding
    public void bind( BindingContext bindingContext )
    {
        // Bind constraints
        // Bind concerns
        // Bind side-effects
    }

    // Context
    public Object invoke( Object composite, Object[] params, Object[] mixins, ModuleInstance moduleInstance ) throws Throwable
    {
        parameters.checkConstraints( params );

        CompositeMethodInstance methodInstance = getInstance( moduleInstance );
        return composite().mixins().invoke( composite, params, mixins, methodInstance );
    }

    private CompositeMethodInstance getInstance( ModuleInstance moduleInstance )
    {
        CompositeMethodInstance methodInstance = newCompositeMethodInstance( moduleInstance, instancePool );

        return methodInstance;
    }

    private CompositeMethodInstance newCompositeMethodInstance( ModuleInstance moduleInstance, CompositeMethodInstancePool compositeMethodInstancePool )
        throws org.qi4j.composite.InstantiationException
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();

        FragmentInvocationHandler mixinInvocationHandler = composite.mixins().mixinFor( method ).newInvocationHandler( method.getDeclaringClass() );

        // Instantiate and link concerns
        Object previousConcern = mixinInvocationHandler;
/*
        {
            List<ConcernBinding> concernBindings = compositeMethodBinding.getConcernBindings();
            for( int i = concernBindings.size() - 1; i >= 0; i-- )
            {
                ConcernBinding concernBinding = concernBindings.get( i );

                Object modifies = getModifies( method, classloader, previousConcern, concernBinding.getConcernResolution().getConcernModel() );

                ModifierInjectionContext modifierContext = new ModifierInjectionContext( moduleInstance.structureContext(),
                                                                                         moduleInstance.module(),
                                                                                         moduleInstance.moduleContext().getModuleBinding(),
                                                                                         compositeBinding,
                                                                                         proxyHandler,
                                                                                         modifies,
                                                                                         compositeMethodBinding,
                                                                                         compositeMethodBinding.getMixinBinding()
                );
                previousConcern = runtime.getInstanceFactory().newInstance( concernBinding, modifierContext );

            }
        }
*/

        // Instantiate constraints for parameters
/*
        Iterable<org.qi4j.spi.composite.ParameterBinding> parameterBindings = compositeMethodBinding.getParameterBindings();
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
*/

        Object[] sideEffects = new Object[0];
/*
        SideEffectInvocationHandlerResult sideEffectResult = new SideEffectInvocationHandlerResult();
        List<SideEffectBinding> sideEffectBindings = compositeMethodBinding.getSideEffectBindings();

        // Instantiate and link side-effects
        Object[] sideEffects = new Object[sideEffectBindings.size()];
        int i = 0;
        for( SideEffectBinding sideEffectBinding : sideEffectBindings )
        {
            Object modifies = getModifies( method, classloader, sideEffectResult, sideEffectBinding.getSideEffectResolution().getSideEffectModel() );

            ModifierInjectionContext modifierContext = new ModifierInjectionContext( moduleInstance.structureContext(),
                                                                                     moduleInstance.module(),
                                                                                     moduleInstance.moduleContext().getModuleBinding(),
                                                                                     compositeBinding,
                                                                                     proxyHandler,
                                                                                     modifies,
                                                                                     compositeMethodBinding,
                                                                                     compositeMethodBinding.getMixinBinding()
            );
            sideEffects[ i++ ] = runtime.getInstanceFactory().newInstance( sideEffectBinding, modifierContext );
        }
*/

        return new CompositeMethodInstance( previousConcern,
                                            sideEffects,
                                            null,
                                            mixinInvocationHandler,
                                            proxyHandler,
                                            compositeMethodInstancePool,
                                            method,
                                            method.getDeclaringClass() );
    }

}
