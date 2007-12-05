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
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.structure.ModuleContext;
import org.qi4j.spi.composite.MixinResolution;
import org.qi4j.spi.composite.PropertyResolution;

/**
 *
 */
public class CompositeBuilderImpl<T extends Composite>
    implements CompositeBuilder<T>
{
    private Class<? extends T> compositeInterface;
    private ModuleContext moduleContext;
    private CompositeContext context;

    private Set adaptContext;
    private Object decoratedObject;
    private Map<MixinResolution, Map<String, PropertyValue>> propertyContext;

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
            PropertyResolution resolution = context.getCompositeResolution().getPropertyResolution( mixinType, property.getName() );
            if( resolution == null )
            {
                throw new CompositeInstantiationException( "No property named " + property.getName() + " found in mixin for type " + mixinType.getName() );
            }
            setProperty( resolution, property );
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
        return (T) context.newCompositeInstance( moduleContext, adaptContext, decoratedObject, propertyContext ).getProxy();
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

    // Private ------------------------------------------------------
    private Set getAdaptContext()
    {
        if( adaptContext == null )
        {
            adaptContext = new LinkedHashSet();
        }
        return adaptContext;
    }

    private Map<MixinResolution, Map<String, PropertyValue>> getPropertyContext()
    {
        if( propertyContext == null )
        {
            propertyContext = new LinkedHashMap<MixinResolution, Map<String, PropertyValue>>();
        }
        return propertyContext;
    }

    private <K> void setProperty( PropertyResolution resolution, PropertyValue property )
    {
        Map<MixinResolution, Map<String, PropertyValue>> compositeProperties = getPropertyContext();
        Map<String, PropertyValue> mixinProperties = compositeProperties.get( resolution.getMixinResolution() );
        if( mixinProperties == null )
        {
            mixinProperties = new HashMap<String, PropertyValue>();
            compositeProperties.put( resolution.getMixinResolution(), mixinProperties );
        }
        mixinProperties.put( property.getName(), property );
    }

    private class PropertiesInvocationHandler implements InvocationHandler
    {
        public PropertiesInvocationHandler()
        {
        }

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            PropertyResolution propertyResolution = context.getCompositeResolution().getPropertyResolution( method );
            if( propertyResolution != null )
            {
                PropertyValue propertyValue = PropertyValue.property( propertyResolution.getPropertyModel().getName(), objects[ 0 ] );
                setProperty( propertyResolution, propertyValue );
            }
            else
            {
                throw new IllegalArgumentException( "Method is not a property" );
            }

            return method.getDefaultValue();
        }
    }
}
