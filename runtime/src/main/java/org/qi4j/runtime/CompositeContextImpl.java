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
import org.qi4j.api.CompositeModelFactory;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.ModifierDependencyInjectionContext;
import org.qi4j.api.model.CompositeContext;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.CompositeResolution;
import org.qi4j.api.model.MixinResolution;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.ModifierResolution;

/**
 * TODO
 */
public final class CompositeContextImpl<T extends Composite>
    implements CompositeContext<T>
{
    private CompositeModel<T> compositeModel;
    private CompositeResolution<T> compositeResolution;
    private FragmentFactory fragmentFactory;
    private CompositeBuilderFactoryImpl builderFactory;
    private InvocationInstancePool[] invocationInstancePool;
    private CompositeModelFactory modelFactory;
    private HashMap<Integer, MethodDescriptor> methodDescriptors;
//    private Method oldMethod;
//    private MethodDescriptor oldDescriptor;

    public CompositeContextImpl( CompositeResolution<T> compositeResolution, CompositeModelFactory modelFactory, CompositeBuilderFactoryImpl builderFactory, FragmentFactory fragmentFactory )
    {
        this.fragmentFactory = fragmentFactory;
        this.modelFactory = modelFactory;
        this.compositeResolution = compositeResolution;
        this.compositeModel = compositeResolution.getCompositeModel();
        this.builderFactory = builderFactory;

        // Create index of method to mixin and invocation instance pools
        methodDescriptors = new HashMap<Integer, MethodDescriptor>( 127 );
        Map<MixinResolution, Integer> mixinIndices = new HashMap<MixinResolution, Integer>();
        Method[] methods = compositeModel.getCompositeClass().getMethods();
        invocationInstancePool = new InvocationInstancePool[methods.length];

        // Assign index to each mixin resolution
        int currentMixinIndex = 0;
        Iterable<MixinResolution> mixinResolutions = compositeResolution.getUsedMixinModels();
        for( MixinResolution mixinResolution : mixinResolutions )
        {
            mixinIndices.put( mixinResolution, currentMixinIndex++ );
        }

        int methodIndex = 0;
        for( Method method : methods )
        {
            MixinResolution mixinResolution = compositeResolution.getMixinForInterface( method.getDeclaringClass() );
            int index = mixinIndices.get( mixinResolution );
            invocationInstancePool[ methodIndex ] = new InvocationInstancePool();

            MethodDescriptor mi = new MethodDescriptor( method, methodIndex, index, invocationInstancePool[ methodIndex ] );
            int hashCode = method.hashCode();
            if( methodDescriptors.get( hashCode ) != null )
            {
                System.out.println( "COLLISSION!" );
            }
            methodDescriptors.put( hashCode, mi );
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

    public CompositeModelFactory getCompositeModelFactory()
    {
        return modelFactory;
    }

    public CompositeBuilderFactory getCompositeBuilderFactory()
    {
        return builderFactory;
    }

    public MethodDescriptor getMethodDescriptor( Method method )
    {
//        if (method == oldMethod)
//            return oldDescriptor;

        MethodDescriptor descriptor = methodDescriptors.get( method.hashCode() );

        if( descriptor == null )
        {
//            oldMethod = null;
            for( MethodDescriptor method1 : methodDescriptors.values() )
            {
                if( method1.getMethod().toGenericString().equals( method.toGenericString() ) )
                {
                    descriptor = methodDescriptors.remove( method1.hashCode() );
                    descriptor = new MethodDescriptor( method, descriptor.getInvocationInstanceIndex(), descriptor.getMixinIndex(), descriptor.getInvocationInstances() );
                }
            }
            methodDescriptors.put( method.hashCode(), descriptor );
        }

//        oldDescriptor = descriptor;
//        oldMethod = method;
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

        //noinspection unchecked
        return instance;
    }

    private InvocationInstance newInstance( MethodDescriptor method )
        throws CompositeInstantiationException
    {
        ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
        Class compositeClass = compositeModel.getCompositeClass();
        ClassLoader classloader = compositeClass.getClassLoader();
        Class[] intfaces = new Class[]{ compositeClass };
        //noinspection unchecked
        T thisCompositeProxy = (T) Proxy.newProxyInstance( classloader, intfaces, proxyHandler );

        List<ModifierResolution> modifierResolutions = compositeResolution.getModifiersForMethod( method.getMethod() );
        FragmentInvocationHandler mixinInvocationHandler = new FragmentInvocationHandler();
        Object previous = mixinInvocationHandler;

        // Instantiate and link modifiers
        for( int i = modifierResolutions.size() - 1; i >= 0; i-- )
        {
            ModifierResolution modifier = modifierResolutions.get( i );
            Object modifies = getModifies( method.getMethod(), classloader, previous, (ModifierModel) modifier.getFragmentModel() );
            ModifierDependencyInjectionContext modifierContext = new ModifierDependencyInjectionContext( this, thisCompositeProxy, modifies, method.getMethod() );
            previous = fragmentFactory.newFragment( modifier, modifierContext );
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
