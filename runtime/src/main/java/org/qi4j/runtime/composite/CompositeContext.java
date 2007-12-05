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
package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.CompositeInstantiationException;
import org.qi4j.composite.PropertyValue;
import org.qi4j.entity.Lifecycle;
import org.qi4j.runtime.CompositeMethodInstancePool;
import org.qi4j.runtime.InstanceFactory;
import org.qi4j.runtime.LifecycleImpl;
import org.qi4j.runtime.MethodDescriptor;
import org.qi4j.runtime.structure.ModuleContext;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.CompositeResolution;
import org.qi4j.spi.composite.MixinBinding;
import org.qi4j.spi.composite.MixinResolution;
import org.qi4j.spi.dependency.MixinInjectionContext;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public final class CompositeContext
{
    private static final Method CREATE_METHOD;

    static
    {
        try
        {
            CREATE_METHOD = Lifecycle.class.getMethod( "create" );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Lifecycle class is corrupt." );
        }
    }

    private CompositeBinding compositeBinding;
    private InstanceFactory instanceFactory;
    private ModuleBinding moduleBinding;
    private CompositeMethodInstancePool[] compositeMethodInstancePools;
    private HashMap<Method, MethodDescriptor> methodDescriptors;

    public CompositeContext( CompositeBinding compositeBinding, List<CompositeMethodContext> compositeMethodContexts, ModuleBinding moduleBinding, InstanceFactory instanceFactory )
    {
        this.moduleBinding = moduleBinding;
        this.compositeBinding = compositeBinding;
        this.instanceFactory = instanceFactory;

        // Create index of method to mixin and invocation instance pools
        methodDescriptors = new HashMap<Method, MethodDescriptor>();
        Map<MixinBinding, Integer> mixinIndices = new HashMap<MixinBinding, Integer>();
        compositeMethodInstancePools = new CompositeMethodInstancePool[compositeBinding.getCompositeMethodBindings().size()];

        // Assign index to each mixin binding
        int currentMixinIndex = 0;
        Iterable<MixinBinding> mixinBindinghs = compositeBinding.getMixinBindings();
        for( MixinBinding mixinBinding : mixinBindinghs )
        {
            mixinIndices.put( mixinBinding, currentMixinIndex++ );
        }

        int methodIndex = 0;
        for( CompositeMethodContext compositeMethodContext : compositeMethodContexts )
        {
            MixinBinding mixinBinding = compositeMethodContext.getCompositeMethodBinding().getMixinBinding();
            int index = mixinIndices.get( mixinBinding );
            compositeMethodInstancePools[ methodIndex ] = new CompositeMethodInstancePool();

            MethodDescriptor methodDescriptor = new MethodDescriptor( compositeMethodContext, methodIndex, index, compositeMethodInstancePools[ methodIndex ] );
            Method method = compositeMethodContext.getCompositeMethodBinding().getCompositeMethodResolution().getCompositeMethodModel().getMethod();
            methodDescriptors.put( method, methodDescriptor );
            methodIndex++;
        }

    }

    public CompositeModel getCompositeModel()
    {
        return compositeBinding.getCompositeResolution().getCompositeModel();
    }

    public CompositeResolution getCompositeResolution()
    {
        return compositeBinding.getCompositeResolution();
    }

    public CompositeBinding getCompositeBinding()
    {
        return compositeBinding;
    }

    public ModuleBinding getModuleBinding()
    {
        return moduleBinding;
    }

    public CompositeInstance newCompositeInstance( ModuleContext moduleContext, Set adapt, Object decoratedObject, Map<MixinResolution, Map<String, PropertyValue>> compositeProperties )
    {
        CompositeInstance compositeInstance = new CompositeInstance( this, moduleContext );

        // Instantiate composite proxy
        Object proxy = newProxy( compositeInstance );
        compositeInstance.setProxy( proxy );

        // Instantiate all mixins
        newMixins( moduleContext, compositeInstance, adapt, decoratedObject, compositeProperties );

        // Invoke lifecycle create() method
        if( proxy instanceof Lifecycle )
        {
            invokeCreate( proxy, compositeInstance );
        }

        // Return
        return compositeInstance;
    }

    public MethodDescriptor getMethodDescriptor( Method method )
    {
        return methodDescriptors.get( method );
    }

    public CompositeMethodInstance getMethodInstance( MethodDescriptor methodDescriptor, ModuleContext moduleContext )
    {
        CompositeMethodInstancePool instances = methodDescriptor.getMethodInstances();

        CompositeMethodInstance instance = instances.getInstance();

        if( instance == null )
        {
            instance = methodDescriptor.getCompositeMethodContext().newCompositeMethodInstance( moduleContext, instances );
        }

        return instance;
    }


    private Object newProxy( CompositeInstance handler )
        throws CompositeInstantiationException
    {
        // Instantiate proxy for given composite interface
        try
        {
            CompositeModel compositeModel = handler.getContext().getCompositeBinding().getCompositeResolution().getCompositeModel();
            Class proxyClass = compositeModel.getProxyClass();
            return proxyClass.getConstructor( InvocationHandler.class ).newInstance( handler );
        }
        catch( Exception e )
        {
            throw new CompositeInstantiationException( e );
        }
    }


    private void newMixins( ModuleContext moduleContext, CompositeInstance compositeInstance, Set adaptContext, Object decoratedObject, Map<MixinResolution, Map<String, PropertyValue>> compositeProperties )
    {
        Map states = new HashMap<Class, Object>();
        states.put( Lifecycle.class, LifecycleImpl.INSTANCE );

        Set adapt = adaptContext == null ? Collections.EMPTY_SET : adaptContext;

        Set<MixinBinding> usedMixins = compositeBinding.getMixinBindings();
        Object[] mixins = compositeInstance.getMixins();
        int i = 0;
        for( MixinBinding mixinBinding : usedMixins )
        {
            Map<String, PropertyValue> props = compositeProperties == null ? Collections.EMPTY_MAP : compositeProperties.get( mixinBinding.getMixinResolution() );
            if( props == null )
            {
                props = Collections.EMPTY_MAP;
            }
            MixinInjectionContext injectionContext = new MixinInjectionContext( moduleContext.getCompositeBuilderFactory(),
                                                                                moduleContext.getObjectBuilderFactory(),
                                                                                moduleContext.getModuleBinding(),
                                                                                compositeBinding,
                                                                                compositeInstance,
                                                                                props,
                                                                                adapt,
                                                                                decoratedObject );
            Object mixin = instanceFactory.newInstance( mixinBinding, injectionContext );
            mixins[ i++ ] = mixin;
        }
    }

    private void invokeCreate( Object composite, CompositeInstance state )
    {
        try
        {
            state.invoke( composite, CREATE_METHOD, null );
        }
        catch( InvocationTargetException e )
        {
            Throwable t = e.getTargetException();
            throw new CompositeInstantiationException( t );
        }
        catch( UndeclaredThrowableException e )
        {
            Throwable t = e.getUndeclaredThrowable();
            throw new CompositeInstantiationException( t );
        }
        catch( RuntimeException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            throw new CompositeInstantiationException( e );
        }
    }
}
