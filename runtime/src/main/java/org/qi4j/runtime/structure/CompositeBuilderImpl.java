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
package org.qi4j.runtime.structure;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeInstantiationException;
import org.qi4j.composite.PropertyValue;
import org.qi4j.entity.EntitySession;
import org.qi4j.entity.property.PropertyChange;
import org.qi4j.entity.property.PropertyChangeObserver;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.entity.property.NullPropertyContainer;
import org.qi4j.runtime.entity.property.PropertyContext;
import org.qi4j.runtime.entity.property.PropertyInstance;
import org.qi4j.spi.composite.MixinResolution;

/**
 *
 */
public class CompositeBuilderImpl<T extends Composite>
    implements CompositeBuilder<T>
{
    private Class<? extends T> compositeInterface;
    private ModuleContext moduleContext;
    private CompositeContext context;

    private EntitySession entitySession;

    private Set adaptContext;
    private Object decoratedObject;
    private Map<MixinResolution, Map<PropertyContext, Object>> propertyContext;

    CompositeBuilderImpl( ModuleContext moduleContext, CompositeContext context )
    {
        this.moduleContext = moduleContext;
        this.context = context;
        this.compositeInterface = context.getCompositeBinding().getCompositeResolution().getCompositeModel().getCompositeClass();
    }

    public void adapt( Object adaptedObject )
    {
        getAdaptContext().add( adaptedObject );
    }

    public void decorate( Object decoratedObject )
    {
        this.decoratedObject = decoratedObject;
    }

    public <K> void properties( Class<K> mixinType, PropertyValue... properties )
    {
        for( PropertyValue property : properties )
        {
            PropertyContext propertyContext = context.getPropertyContext( mixinType, property.getName() );
            if( propertyContext == null )
            {
                throw new CompositeInstantiationException( "No property named " + property.getName() + " found in mixin for type " + mixinType.getName() );
            }
            setProperty( propertyContext, property.getValue() );
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
        return compositeInterface.cast( context.newCompositeInstance( moduleContext, adaptContext, decoratedObject, propertyContext, entitySession ).getProxy() );
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

    void attach( EntitySession entitySession )
    {
        this.entitySession = entitySession;
    }

    // Private ------------------------------------------------------
    private Set getAdaptContext()
    {
        if( adaptContext == null )
        {
            adaptContext = new LinkedHashSet();
        }
        return adaptContext;
    }

    private Map<MixinResolution, Map<PropertyContext, Object>> getPropertyContext()
    {
        if( propertyContext == null )
        {
            propertyContext = new LinkedHashMap<MixinResolution, Map<PropertyContext, Object>>();
        }
        return propertyContext;
    }

    private void setProperty( PropertyContext propertyContext, Object property )
    {
        Map<MixinResolution, Map<PropertyContext, Object>> compositeProperties = getPropertyContext();
        MixinResolution mixinResolution = propertyContext.getPropertyBinding().getPropertyResolution().getMixinResolution();
        Map<PropertyContext, Object> mixinProperties = compositeProperties.get( mixinResolution );
        if( mixinProperties == null )
        {
            mixinProperties = new HashMap<PropertyContext, Object>();
            compositeProperties.put( mixinResolution, mixinProperties );
        }

        mixinProperties.put( propertyContext, property );
    }

    private class PropertiesInvocationHandler implements InvocationHandler
    {
        public PropertiesInvocationHandler()
        {
        }

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            {
                final PropertyContext propertyContext = context.getMethodDescriptor( method ).getCompositeMethodContext().getPropertyContext();
                if( propertyContext != null )
                {
                    PropertyInstance<Object> mutableProperty = new PropertyInstance<Object>( new NullPropertyContainer<Object>(), propertyContext.getPropertyBinding().getDefaultValue() );
                    mutableProperty.addChangeObserver( new PropertyChangeObserver<Object>()
                    {
                        public void onChange( PropertyChange<Object> propertyChange )
                        {
                            setProperty( propertyContext, propertyChange.getNewValue() );
                        }
                    } );
                    return mutableProperty;
                }
            }

            // TODO: This is for getters. Should it be deprecated?
            PropertyContext propertyContext = context.getPropertyContext( method.getDeclaringClass(), method.getName().substring( 3 ) );
            if( propertyContext != null )
            {
                PropertyValue propertyValue = PropertyValue.property( propertyContext.getPropertyBinding().getPropertyResolution().getPropertyModel().getName(), objects[ 0 ] );
                setProperty( propertyContext, propertyValue );
            }
            else
            {
                throw new IllegalArgumentException( "Method is not a property" );
            }

            return method.getDefaultValue();
        }
    }
}
