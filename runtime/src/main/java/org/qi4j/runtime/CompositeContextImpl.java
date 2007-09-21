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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.model.CompositeContext;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.runtime.resolution.CompositeResolution;
import org.qi4j.runtime.resolution.MixinResolution;
import org.qi4j.runtime.resolution.ModifierResolution;
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
        methodDescriptors = new HashMap<Method, MethodDescriptor>( 127 );
        Map<MixinResolution, Integer> mixinIndices = new HashMap<MixinResolution, Integer>();
        Method[] methods = compositeModel.getCompositeClass().getMethods();
        invocationInstancePool = new InvocationInstancePool[methods.length];

        // Assign index to each mixin resolution
        int currentMixinIndex = 0;
        Iterable<MixinResolution> mixinResolutions = compositeResolution.getUsedMixinModels();
        for( MixinResolution mixinResolution : mixinResolutions )
        {
            mixinIndices.put( mixinResolution, new Integer( currentMixinIndex++ ) );
        }

        int methodIndex = 0;
        for( Method method : methods )
        {
            MixinResolution mixinResolution = compositeResolution.getMixinForInterface( method.getDeclaringClass() );
            int index = mixinIndices.get( mixinResolution );
            invocationInstancePool[ methodIndex ] = new InvocationInstancePool();

            MethodDescriptor mi = new MethodDescriptor( method, methodIndex, index, invocationInstancePool[ methodIndex ] );
            if( methodDescriptors.get( method ) != null )
            {
                System.out.println( "COLLISION!" );
            }
            methodDescriptors.put( method, mi );
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

        if( descriptor == null )
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

    public InvocationInstance<T> getInvocationInstance( MethodDescriptor methodDescriptor )
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
        Class[] intfaces = new Class[]{ compositeClass };
        T thisCompositeProxy = (T) Proxy.newProxyInstance( classloader, intfaces, proxyHandler );

        List<ModifierResolution> modifierResolutions = compositeResolution.getModifiersForMethod( method.getMethod() );
        FragmentInvocationHandler mixinInvocationHandler = new FragmentInvocationHandler();
        Object previous = mixinInvocationHandler;

        // Instantiate and link modifiers
        for( int i = modifierResolutions.size() - 1; i >= 0; i-- )
        {
            ModifierResolution modifier = modifierResolutions.get( i );

            Object modifies = getModifies( method.getMethod(), classloader, previous, (ModifierModel) modifier.getFragmentModel() );

            ModifierDependencyInjectionContext modifierContext = new ModifierDependencyInjectionContext( this, thisCompositeProxy, modifies, method.getMethod(), proxyHandler );
            previous = fragmentFactory.newInstance( modifier, modifierContext );
        }

        return new InvocationInstance( previous, mixinInvocationHandler, proxyHandler, invocationInstancePool[ method.getInvocationInstanceIndex() ], method.getMethod(), method.getMethod().getDeclaringClass() );
    }

    private Object getModifies( Method method, ClassLoader classloader, Object previous, ModifierModel modifier )
    {
        Object modifies;
        if( modifier.isGeneric() )
        {
            if( previous instanceof InvocationHandler )
            {
                modifies = previous;
            }
            else
            {
                InvocationHandler modifiesHandler = new FragmentInvocationHandler( previous );
                modifies = Proxy.newProxyInstance( classloader, new Class[]{ method.getDeclaringClass() }, modifiesHandler );
            }
        }
        else
        {
            if( previous instanceof InvocationHandler )
            {
                modifies = Proxy.newProxyInstance( classloader, new Class[]{ method.getDeclaringClass() }, (InvocationHandler) previous );
            }
            else
            {
                modifies = previous;
            }
        }
        return modifies;
    }
}
