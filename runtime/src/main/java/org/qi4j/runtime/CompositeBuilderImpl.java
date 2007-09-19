/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.PropertyValue;
import org.qi4j.api.model.Binding;
import org.qi4j.api.model.CompositeContext;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.CompositeState;
import org.qi4j.api.model.InjectionKey;
import org.qi4j.api.persistence.Lifecycle;
import org.qi4j.runtime.resolution.MixinResolution;
import org.qi4j.spi.dependency.MixinDependencyInjectionContext;

/**
 *
 */
public class CompositeBuilderImpl<T extends Composite>
    implements CompositeBuilder<T>
{
    private static final Method CREATE_METHOD;

    private Class<T> compositeInterface;
    private CompositeContextImpl<T> context;
    private InstanceFactory fragmentFactory;

    private Map<InjectionKey, Object> adaptContext;
    private Map<InjectionKey, Object> decorateContext;
    private Map<MixinResolution, Map<InjectionKey, Object>> propertyContext;

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

    CompositeBuilderImpl( CompositeContextImpl<T> context, InstanceFactory fragmentFactory )
    {
        this.fragmentFactory = fragmentFactory;
        this.context = context;
        this.compositeInterface = context.getCompositeModel().getCompositeClass();
    }

    public CompositeContext<T> getContext()
    {
        return context;
    }

    public void adapt( Object adaptedObject )
    {
        if( adaptedObject instanceof Binding )
        {
            Binding binding = (Binding) adaptedObject;
            getAdaptContext().put( binding.getKey(), binding.getValue() );
        }
        else
        {
            InjectionKey key = new InjectionKey( adaptedObject.getClass(), null, null );
            getAdaptContext().put( key, adaptedObject );
        }
    }

    public void decorate( Object decoratedObject )
    {
        if( decoratedObject instanceof Binding )
        {
            Binding binding = (Binding) decoratedObject;
            getDecorateContext().put( binding.getKey(), binding.getValue() );
        }
        else
        {
            InjectionKey key = new InjectionKey( decoratedObject.getClass(), null, null );
            getDecorateContext().put( key, decoratedObject );
        }
    }

    public <K, T extends K> void properties( Class<K> mixinType, Object... properties )
    {
        MixinResolution resolution = context.getCompositeResolution().getMixinForInterface( mixinType );

        Map<MixinResolution, Map<InjectionKey, Object>> context = getPropertyContext();
        Map<InjectionKey, Object> mixinContext = new LinkedHashMap<InjectionKey, Object>();
        for( Object property : properties )
        {
            InjectionKey key = null;
            if( property instanceof Binding )
            {
                Binding binding = (Binding) property;
                key = binding.getKey();
                property = binding.getValue();
            }

            String name = null;
            if( property instanceof PropertyValue )
            {
                PropertyValue value = (PropertyValue) property;
                name = value.getName();
                property = value.getValue();
            }

            if( key == null )
            {
                key = new InjectionKey( property.getClass(), name, resolution.getMixinModel().getModelClass() );
            }
            mixinContext.put( key, property );
        }
        context.put( resolution, mixinContext );
    }

    public T properties()
    {
        // Instantiate proxy for given composite interface
        try
        {
            PropertiesInvocationHandler handler = new PropertiesInvocationHandler();
            ClassLoader proxyClassloader = compositeInterface.getClassLoader();
            Class[] interfaces = new Class[]{ compositeInterface };
            return compositeInterface.cast( Proxy.newProxyInstance( proxyClassloader, interfaces, handler ) );
        }
        catch( Exception e )
        {
            throw new CompositeInstantiationException( e );
        }
    }

    public T newInstance()
    {
        CompositeInvocationHandler compositeInvocationHandler = new CompositeInvocationHandler( context );

        // Instantiate composite proxy
        T composite = newInstance( compositeInvocationHandler );

        // Instantiate all mixins
        newMixins( composite, compositeInvocationHandler );

        // Invoke lifecycle create() method
//        invokeCreate( composite, state );

        // Return
        return composite;
    }

    private T newInstance( CompositeInvocationHandler handler )
        throws CompositeInstantiationException
    {
        // Instantiate proxy for given composite interface
        try
        {
            CompositeModel compositeModel = handler.getContext().getCompositeModel();
            Class<? extends T> proxyClass = compositeModel.getProxyClass();
            return proxyClass.getConstructor( InvocationHandler.class ).newInstance( handler );
        }
        catch( Exception e )
        {
            throw new CompositeInstantiationException( e );
        }
    }


    private void newMixins( T composite, CompositeState state )
    {
        Map states = new HashMap<Class, Object>();
        states.put( Lifecycle.class, LifecycleImpl.INSTANCE );

        Map<InjectionKey, Object> adapt = adaptContext == null ? Collections.EMPTY_MAP : adaptContext;
        Map<InjectionKey, Object> decorate = decorateContext == null ? Collections.EMPTY_MAP : decorateContext;

        Set<MixinResolution> usedMixins = context.getCompositeResolution().getUsedMixinModels();
        Object[] mixins = state.getMixins();
        int i = 0;
        for( MixinResolution resolution : usedMixins )
        {
            Map<InjectionKey, Object> props = propertyContext == null ? Collections.EMPTY_MAP : propertyContext.get( resolution );
            if( props == null )
            {
                props = Collections.EMPTY_MAP;
            }
            MixinDependencyInjectionContext injectionContext = new MixinDependencyInjectionContext( context, composite, props, adapt, decorate );
            Object mixin = fragmentFactory.newInstance( resolution, injectionContext );
            mixins[ i++ ] = mixin;
        }
    }

    private void invokeCreate( T composite, CompositeInvocationHandler state )
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

    // Private ------------------------------------------------------
    private Map<InjectionKey, Object> getAdaptContext()
    {
        if( adaptContext == null )
        {
            adaptContext = new LinkedHashMap<InjectionKey, Object>();
        }
        return adaptContext;
    }

    private Map<InjectionKey, Object> getDecorateContext()
    {
        if( decorateContext == null )
        {
            decorateContext = new LinkedHashMap<InjectionKey, Object>();
        }
        return decorateContext;
    }

    private Map<MixinResolution, Map<InjectionKey, Object>> getPropertyContext()
    {
        if( propertyContext == null )
        {
            propertyContext = new LinkedHashMap<MixinResolution, Map<InjectionKey, Object>>();
        }
        return propertyContext;
    }

    private class PropertiesInvocationHandler implements InvocationHandler
    {
        public PropertiesInvocationHandler()
        {
        }

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            if( method.getName().startsWith( "set" ) )
            {
                Object propertyValue = objects[ 0 ];

                // Find get method
                BeanInfo info = Introspector.getBeanInfo( method.getDeclaringClass() );
                PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
                for( PropertyDescriptor descriptor : descriptors )
                {
                    Method writeMethod = descriptor.getWriteMethod();
                    if( writeMethod != null && writeMethod.equals( method ) )
                    {
                        MixinResolution resolution = context.getCompositeResolution().getMixinForInterface( method.getDeclaringClass() );

                        Map<MixinResolution, Map<InjectionKey, Object>> context = getPropertyContext();
                        Map<InjectionKey, Object> mixinContext = context.get( resolution );
                        if( mixinContext == null )
                        {
                            mixinContext = new LinkedHashMap<InjectionKey, Object>();
                            context.put( resolution, mixinContext );
                        }

                        InjectionKey key = new InjectionKey( propertyValue.getClass(), descriptor.getName(), resolution.getMixinModel().getModelClass() );
                        mixinContext.put( key, propertyValue );
                        break;
                    }
                }
            }

            return method.getDefaultValue();
        }
    }
}
