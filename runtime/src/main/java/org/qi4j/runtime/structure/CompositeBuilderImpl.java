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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.InstantiationException;
import org.qi4j.composite.InvalidApplicationException;
import org.qi4j.composite.State;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.property.Property;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.CompositeInstance;
import org.qi4j.runtime.property.PropertyContext;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.property.ImmutablePropertyInstance;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyModel;

/**
 *
 */
public class CompositeBuilderImpl<T>
    implements CompositeBuilder<T>
{
    protected Class<? extends T> compositeInterface;
    protected ModuleInstance moduleInstance;
    protected CompositeContext context;

    protected Set<Object> uses;
    protected Map<Method, Property> propertyValues;
    protected Map<Method, AbstractAssociation> associationValues;

    public CompositeBuilderImpl( ModuleInstance moduleInstance, CompositeContext context )
    {
        this.moduleInstance = moduleInstance;
        this.context = context;
        this.compositeInterface = (Class<? extends T>) context.getCompositeBinding().getCompositeResolution().getCompositeModel().getCompositeType();
    }

    public CompositeBuilder<T> use( Object... usedObjects )
    {
        Set<Object> useSet = getUses();
        for( Object usedObject : usedObjects )
        {
            useSet.add( usedObject );
        }

        return this;
    }

    public T stateOfComposite()
    {
        // Instantiate proxy for given composite interface
        try
        {
            StateInvocationHandler handler = newStateInvocationHandler();
            ClassLoader proxyClassloader = compositeInterface.getClassLoader();
            Class[] interfaces = new Class[]{ compositeInterface };
            return compositeInterface.cast( Proxy.newProxyInstance( proxyClassloader, interfaces, handler ) );
        }
        catch( Exception e )
        {
            throw new InstantiationException( e );
        }
    }

    public <K> K stateFor( Class<K> mixinType )
    {
        // Instantiate proxy for given interface
        try
        {
            StateInvocationHandler handler = newStateInvocationHandler();
            ClassLoader proxyClassloader = mixinType.getClassLoader();
            Class[] interfaces = new Class[]{ mixinType };
            return mixinType.cast( Proxy.newProxyInstance( proxyClassloader, interfaces, handler ) );
        }
        catch( Exception e )
        {
            throw new InstantiationException( e );
        }
    }

    public T newInstance()
    {
        // Calculate total set of Properties for this Composite
        Map<Method, Property> properties = new HashMap<Method, Property>();
        for( PropertyContext propertyContext : context.getPropertyContexts() )
        {
            Object value;
            Method accessor = propertyContext.getPropertyBinding().getPropertyResolution().getPropertyModel().getAccessor();
            if( propertyValues != null && propertyValues.containsKey( accessor ) )
            {
                value = propertyValues.get( accessor ).get();
            }
            else
            {
                value = propertyContext.getPropertyBinding().getDefaultValue();
            }

            Property property = propertyContext.newInstance( moduleInstance, value );
            PropertyBinding binding = propertyContext.getPropertyBinding();
            PropertyResolution propertyResolution = binding.getPropertyResolution();
            PropertyModel propertyModel = propertyResolution.getPropertyModel();
            properties.put( propertyModel.getAccessor(), property );
        }

        CompositeInstance compositeInstance = context.newCompositeInstance( moduleInstance,
                                                                            uses,
                                                                            new CompositeBuilderState( properties ) );
        return compositeInterface.cast( compositeInstance.getProxy() );
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

    protected StateInvocationHandler newStateInvocationHandler()
    {
        return new StateInvocationHandler();
    }

    protected Set<Object> getUses()
    {
        if( uses == null )
        {
            uses = new LinkedHashSet<Object>();
        }
        return uses;
    }

    protected Map<Method, Property> getProperties()
    {
        if( propertyValues == null )
        {
            propertyValues = new HashMap<Method, Property>();
        }
        return propertyValues;
    }

    protected Map<Method, AbstractAssociation> getAssociations()
    {
        if( associationValues == null )
        {
            associationValues = new HashMap<Method, AbstractAssociation>();
        }
        return associationValues;
    }

    protected class StateInvocationHandler
        implements InvocationHandler
    {
        public StateInvocationHandler()
        {
        }

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                Property<Object> propertyInstance = getProperties().get( method );
                if( propertyInstance == null )
                {
                    PropertyContext propertyContext = context.getMethodDescriptor( method ).getCompositeMethodContext().getPropertyContext();
                    propertyInstance = propertyContext.newInstance( moduleInstance, ImmutablePropertyInstance.UNSET );
                    getProperties().put( method, propertyInstance );
                }
                return propertyInstance;
            }
            else
            {
                throw new IllegalArgumentException( "Method does not represent state: " + method );
            }
        }

    }

    static class CompositeBuilderState
        implements State
    {
        Map<Method, Property> properties;

        public CompositeBuilderState( Map<Method, Property> properties )
        {
            this.properties = properties;
        }

        public Property getProperty( Method method )
        {
            return properties.get( method );
        }

        public AbstractAssociation getAssociation( Method qualifiedName )
        {
            throw new InvalidApplicationException( "May not use Associations in Composites that are not accessed through a UnitOfWork" );
        }
    }
}
