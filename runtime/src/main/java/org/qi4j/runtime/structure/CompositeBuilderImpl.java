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
import org.qi4j.association.AbstractAssociation;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeInstantiationException;
import org.qi4j.composite.PropertyValue;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.property.PropertyVetoException;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.CompositeInstance;
import org.qi4j.runtime.property.AssociationContext;
import org.qi4j.runtime.property.PropertyContext;
import org.qi4j.spi.composite.AssociationModel;
import org.qi4j.spi.composite.AssociationResolution;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.property.AssociationBinding;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyInstance;
import org.qi4j.spi.property.PropertyModel;

/**
 *
 */
public class CompositeBuilderImpl<T extends Composite>
    implements CompositeBuilder<T>
{
    protected Class<? extends T> compositeInterface;
    protected ModuleInstance moduleInstance;
    protected CompositeContext context;

    protected Set adaptContext;
    protected Object decoratedObject;
    protected Map<String, Object> propertyValues;
    protected Map<String, AbstractAssociation> associationValues;

    public CompositeBuilderImpl( ModuleInstance moduleInstance, CompositeContext context )
    {
        this.moduleInstance = moduleInstance;
        this.context = context;
        this.compositeInterface = (Class<? extends T>) context.getCompositeBinding().getCompositeResolution().getCompositeModel().getCompositeClass();
    }

    public void adapt( Object adaptedObject )
    {
        getAdaptContext().add( adaptedObject );
    }

    public <K, T extends K> void decorate( K decoratedObject )
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
        // Calculate total set of Properties for this Composite
        Map<String, Property> properties = new HashMap<String, Property>();
        for( PropertyContext propertyContext : context.getPropertyContexts() )
        {
            Object value;
            String propertyName = propertyContext.getPropertyBinding().getQualifiedName();
            if( propertyValues != null && propertyValues.containsKey( propertyName ) )
            {
                value = propertyValues.get( propertyName );
            }
            else
            {
                value = propertyContext.getPropertyBinding().getDefaultValue();
            }

            Property property = propertyContext.newInstance( moduleInstance, value );
            PropertyBinding binding = propertyContext.getPropertyBinding();
            PropertyResolution propertyResolution = binding.getPropertyResolution();
            PropertyModel propertyModel = propertyResolution.getPropertyModel();
            String qualifiedName = propertyModel.getQualifiedName();
            properties.put( qualifiedName, property );
        }

        // Calculate total set of Associations for this Composite
        Map<String, AbstractAssociation> associations = new HashMap<String, AbstractAssociation>();
        for( AssociationContext mixinAssociation : context.getAssociationContexts() )
        {
            Object value = null;
            if( associationValues != null && associationValues.containsKey( mixinAssociation ) )
            {
                value = associationValues.get( mixinAssociation );
            }

            AbstractAssociation association = mixinAssociation.newInstance( moduleInstance, value );
            AssociationBinding binding = mixinAssociation.getAssociationBinding();
            AssociationResolution associationResolution = binding.getAssociationResolution();
            AssociationModel associationModel = associationResolution.getAssociationModel();
            String qualifiedName = associationModel.getQualifiedName();
            associations.put( qualifiedName, association );
        }

        CompositeInstance compositeInstance = context.newCompositeInstance( moduleInstance, adaptContext, decoratedObject, properties, associations );
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

    private Set getAdaptContext()
    {
        if( adaptContext == null )
        {
            adaptContext = new LinkedHashSet();
        }
        return adaptContext;
    }

    protected Map<String, Object> getPropertyValues()
    {
        if( propertyValues == null )
        {
            propertyValues = new HashMap<String, Object>();
        }
        return propertyValues;
    }

    protected Map<String, AbstractAssociation> getAssociationValues()
    {
        if( associationValues == null )
        {
            associationValues = new HashMap<String, AbstractAssociation>();
        }
        return associationValues;
    }

    private void setProperty( PropertyContext propertyContext, Object property )
    {
        Map<String, Object> compositeProperties = getPropertyValues();
        compositeProperties.put( propertyContext.getPropertyBinding().getQualifiedName(), property );
    }

    private class PropertiesInvocationHandler
        implements InvocationHandler
    {
        public PropertiesInvocationHandler()
        {
        }

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            final PropertyContext propertyContext = context.getMethodDescriptor( method ).getCompositeMethodContext().getPropertyContext();
            if( propertyContext != null )
            {
                Object defValue = propertyContext.getPropertyBinding().getDefaultValue();
                PropertyBinding binding = propertyContext.getPropertyBinding();
                PropertyInstance<Object> propertyInstance = new ImmutablePropertySupport( binding, defValue, propertyContext );
                return propertyInstance;
            }
            else
            {
                throw new IllegalArgumentException( "Method is not a property: " + method );
            }
        }

    }

    private class ImmutablePropertySupport extends PropertyInstance<Object>
        implements ImmutableProperty<Object>
    {
        private final PropertyContext propertyContext;

        public ImmutablePropertySupport( PropertyBinding binding, Object defValue, PropertyContext propertyContext )
            throws IllegalArgumentException
        {
            super( binding, defValue );
            this.propertyContext = propertyContext;
        }

        @Override public void set( Object newValue ) throws PropertyVetoException
        {
            super.set( newValue );
            setProperty( propertyContext, newValue );
        }
    }
}
