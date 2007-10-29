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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.Constraint;
import org.qi4j.api.model.CompositeContext;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.ConcernModel;
import org.qi4j.api.model.ConstraintDeclarationModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.SideEffectModel;
import org.qi4j.runtime.resolution.CompositeResolution;
import org.qi4j.runtime.resolution.ConcernResolution;
import org.qi4j.runtime.resolution.ConstraintResolution;
import org.qi4j.runtime.resolution.MethodResolution;
import org.qi4j.runtime.resolution.MixinResolution;
import org.qi4j.runtime.resolution.ParameterConstraintResolution;
import org.qi4j.runtime.resolution.SideEffectResolution;
import org.qi4j.spi.dependency.ModifierDependencyInjectionContext;

/**
 * TODO
 */
public final class CompositeContextImpl<T extends Composite>
    implements CompositeContext<T>
{
    private CompositeModel<T> compositeModel;
    private CompositeResolution<T> compositeResolution;
    private InstanceFactory fragmentFactory;
    private CompositeBuilderFactoryImpl builderFactory;
    private InvocationInstancePool[] invocationInstancePool;
    private HashMap<Method, MethodDescriptor> methodDescriptors;

    public CompositeContextImpl( CompositeResolution<T> compositeResolution, CompositeBuilderFactoryImpl builderFactory, InstanceFactory instanceFactory )
    {
        this.fragmentFactory = instanceFactory;
        this.compositeResolution = compositeResolution;
        this.compositeModel = compositeResolution.getCompositeModel();
        this.builderFactory = builderFactory;

        // Create index of method to mixin and invocation instance pools
        methodDescriptors = new HashMap<Method, MethodDescriptor>( compositeModel.getMethodModels().size() );
        Map<MixinResolution, Integer> mixinIndices = new HashMap<MixinResolution, Integer>();
        invocationInstancePool = new InvocationInstancePool[compositeResolution.getMethodResolutions().size()];

        // Assign index to each mixin resolution
        int currentMixinIndex = 0;
        Iterable<MixinResolution> mixinResolutions = compositeResolution.getResolvedMixinModels();
        for( MixinResolution mixinResolution : mixinResolutions )
        {
            mixinIndices.put( mixinResolution, currentMixinIndex++ );
        }

        int methodIndex = 0;
        for( MethodResolution method : compositeResolution.getMethodResolutions() )
        {
            MixinResolution mixinResolution = method.getMixinResolution();
            int index = mixinIndices.get( mixinResolution );
            invocationInstancePool[ methodIndex ] = new InvocationInstancePool();

            MethodDescriptor mi = new MethodDescriptor( method.getMethodModel().getMethod(), methodIndex, index, invocationInstancePool[ methodIndex ] );
            if( methodDescriptors.get( method ) != null )
            {
                System.out.println( "COLLISION!" );
            }
            methodDescriptors.put( method.getMethodModel().getMethod(), mi );
            methodIndex++;
        }

    }

    public CompositeModel<T> getCompositeModel()
    {
        return compositeModel;
    }

    public CompositeResolution<T> getCompositeResolution()
    {
        return compositeResolution;
    }

    public CompositeBuilderFactory getCompositeBuilderFactory()
    {
        return builderFactory;
    }

    public MethodDescriptor getMethodDescriptor( Method method )
    {
        MethodDescriptor descriptor = methodDescriptors.get( method );

        if( descriptor == null && !method.getDeclaringClass().equals( Object.class ) )
        {
            for( MethodDescriptor method1 : methodDescriptors.values() )
            {
                if( method1.getMethod().toGenericString().equals( method.toGenericString() ) )
                {
                    descriptor = methodDescriptors.remove( method1 );
                    descriptor = new MethodDescriptor( method, descriptor.getInvocationInstanceIndex(), descriptor.getMixinIndex(), descriptor.getInvocationInstances() );

                    methodDescriptors.put( method, descriptor );

                    break;
                }
            }

        }

        return descriptor;
    }

    public InvocationInstance getInvocationInstance( MethodDescriptor methodDescriptor )
    {
        InvocationInstancePool instances = methodDescriptor.getInvocationInstances();

        InvocationInstance instance = instances.getInstance();

        if( instance == null )
        {
            instance = newInstance( methodDescriptor );
        }

        return instance;
    }

    private InvocationInstance newInstance( MethodDescriptor method )
        throws CompositeInstantiationException
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        Class compositeClass = compositeModel.getCompositeClass();
        ClassLoader classloader = compositeClass.getClassLoader();

        MethodResolution methodResolution = compositeResolution.getMethodResolution( method.getMethod() );

        FragmentInvocationHandler mixinInvocationHandler = new FragmentInvocationHandler();

        // Instantiate and link concerns
        Object previousConcern = mixinInvocationHandler;
        {
            List<ConcernResolution> concernResolutions = methodResolution.getConcerns();
            for( int i = concernResolutions.size() - 1; i >= 0; i-- )
            {
                ConcernResolution concern = concernResolutions.get( i );

                Object modifies = getModifies( method.getMethod(), classloader, previousConcern, (ConcernModel) concern.getFragmentModel() );

                ModifierDependencyInjectionContext modifierContext = new ModifierDependencyInjectionContext( this, proxyHandler, modifies, method.getMethod(), methodResolution.getMixinResolution().getMixinModel(), proxyHandler );
                previousConcern = fragmentFactory.newInstance( concern, modifierContext );
            }
        }

        // Instantiate constraints for parameters
        Iterable<ParameterConstraintResolution> paramConstraintResolutions = methodResolution.getMethodConstraintResolution().getParameterConstraintResolutions();
        List<List<ConstraintInstance>> parameterConstraintInstances = new ArrayList<List<ConstraintInstance>>();
        boolean hasConstraints = false;
        for( ParameterConstraintResolution paramConstraintResolution : paramConstraintResolutions )
        {
            List<ConstraintInstance> constraintInstances = new ArrayList<ConstraintInstance>();
            Iterable<ConstraintResolution> constraints = paramConstraintResolution.getConstraints();
            for( ConstraintResolution constraintResolution : constraints )
            {
                ConstraintDeclarationModel model = constraintResolution.getConstraintDeclarationModel();
                Class aClass = model.getConstraintType();
                Class<? extends Constraint> constraintType = aClass;
                try
                {
                    Constraint constraintInstance = constraintType.newInstance();
                    constraintInstances.add( new ConstraintInstance( constraintInstance, constraintResolution.getConstraintAnnotation() ) );
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
        List<SideEffectResolution> sideEffectResolutions = methodResolution.getSideEffects();

        // Instantiate and link side-effects
        Object[] sideEffects = new Object[sideEffectResolutions.size()];
        int i = 0;
        for( SideEffectResolution sideEffectResolution : sideEffectResolutions )
        {
            Object modifies = getModifies( method.getMethod(), classloader, sideEffectResult, (SideEffectModel) sideEffectResolution.getFragmentModel() );

            ModifierDependencyInjectionContext modifierContext = new ModifierDependencyInjectionContext( this, proxyHandler, modifies, method.getMethod(), methodResolution.getMixinResolution().getMixinModel(), proxyHandler );
            sideEffects[ i++ ] = fragmentFactory.newInstance( sideEffectResolution, modifierContext );
        }

        return new InvocationInstance( previousConcern, sideEffects, sideEffectResult, mixinInvocationHandler, proxyHandler, invocationInstancePool[ method.getInvocationInstanceIndex() ], method.getMethod(), method.getMethod().getDeclaringClass() );
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
