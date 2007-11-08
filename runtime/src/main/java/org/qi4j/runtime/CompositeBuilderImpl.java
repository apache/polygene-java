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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.qi4j.Composite;
import org.qi4j.CompositeBuilder;
import org.qi4j.CompositeInstantiationException;
import org.qi4j.PropertyValue;
import org.qi4j.dependency.InjectionKey;
import org.qi4j.dependency.MixinDependencyInjectionContext;
import org.qi4j.entity.Lifecycle;
import org.qi4j.model.CompositeContext;
import org.qi4j.model.CompositeModel;
import org.qi4j.runtime.resolution.MixinResolution;

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
    private Map<MixinResolution, Map<InjectionKey, PropertyValue>> propertyContext;

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
        InjectionKey key = new InjectionKey( adaptedObject.getClass(), null, null );
        getAdaptContext().put( key, adaptedObject );
    }

    public void decorate( Object decoratedObject )
    {
        InjectionKey key = new InjectionKey( decoratedObject.getClass(), null, null );
        getDecorateContext().put( key, decoratedObject );
    }

    public <K> void properties( Class<K> mixinType, PropertyValue... properties )
    {
        Set<MixinResolution> resolutions = context.getCompositeResolution().getMixinsForInterface( mixinType );

        for( MixinResolution mixinResolution : resolutions )
        {
            Map<MixinResolution, Map<InjectionKey, PropertyValue>> context = getPropertyContext();
            Map<InjectionKey, PropertyValue> mixinContext = context.get( mixinResolution );
            if( mixinContext == null )
            {
                context.put( mixinResolution, mixinContext = new LinkedHashMap<InjectionKey, PropertyValue>() );
            }

            for( PropertyValue property : properties )
            {
                String name = property.getName();
                Object value = property.getValue();
                InjectionKey key = new InjectionKey( value.getClass(), name, mixinResolution.getMixinModel().getModelClass() );
                mixinContext.put( key, property );
            }
        }
    }

    public T propertiesOfComposite()
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

    public <K> K propertiesFor( Class<K> mixinType )
    {
        // Instantiate proxy for given interface
        try
        {
            PropertiesInvocationHandler handler = new PropertiesInvocationHandler();
            ClassLoader proxyClassloader = mixinType.getClassLoader();
            Class[] interfaces = new Class[]{ mixinType };
            return mixinType.cast( Proxy.newProxyInstance( proxyClassloader, interfaces, handler ) );
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
        if( composite instanceof Lifecycle )
        {
            invokeCreate( composite, compositeInvocationHandler );
        }

        // Return
        return composite;
    }


    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            public boolean hasNext()
            {
                return true;
            }

            public T next()
            {
                return newInstance();
            }

            public void remove()
            {
            }
        };
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


    private void newMixins( T composite, CompositeInvocationHandler handler )
    {
        Map states = new HashMap<Class, Object>();
        states.put( Lifecycle.class, LifecycleImpl.INSTANCE );

        Map<InjectionKey, Object> adapt = adaptContext == null ? Collections.EMPTY_MAP : adaptContext;
        Map<InjectionKey, Object> decorate = decorateContext == null ? Collections.EMPTY_MAP : decorateContext;

        Set<MixinResolution> usedMixins = context.getCompositeResolution().getResolvedMixinModels();
        Object[] mixins = handler.getMixins();
        int i = 0;
        for( MixinResolution resolution : usedMixins )
        {
            Map<InjectionKey, PropertyValue> props = propertyContext == null ? Collections.EMPTY_MAP : propertyContext.get( resolution );
            if( props == null )
            {
                props = Collections.EMPTY_MAP;
            }
            MixinDependencyInjectionContext injectionContext = new MixinDependencyInjectionContext( context, handler, props, adapt, decorate );
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

    private Map<MixinResolution, Map<InjectionKey, PropertyValue>> getPropertyContext()
    {
        if( propertyContext == null )
        {
            propertyContext = new LinkedHashMap<MixinResolution, Map<InjectionKey, PropertyValue>>();
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
                        MixinResolution mixinResolution = context.getCompositeResolution().getMethodResolution( method ).getMixinResolution();

                        Map<MixinResolution, Map<InjectionKey, PropertyValue>> context = getPropertyContext();
                        Map<InjectionKey, PropertyValue> mixinContext = context.get( mixinResolution );
                        if( mixinContext == null )
                        {
                            context.put( mixinResolution, mixinContext = new LinkedHashMap<InjectionKey, PropertyValue>() );
                        }

                        InjectionKey key = new InjectionKey( propertyValue.getClass(), descriptor.getName(), mixinResolution.getMixinModel().getModelClass() );
                        mixinContext.put( key, PropertyValue.property( descriptor.getName(), propertyValue ) );
                        break;
                    }
                }
            }

            return method.getDefaultValue();
        }
    }
}
