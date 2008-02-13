/*  Copyright 2007 Rickard Öberg.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.PropertyValue;
import org.qi4j.property.Property;
import org.qi4j.runtime.composite.ObjectContext;
import org.qi4j.runtime.property.AssociationContext;
import org.qi4j.runtime.property.PropertyContext;
import org.qi4j.spi.composite.AssociationModel;
import org.qi4j.spi.composite.AssociationResolution;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.property.AssociationBinding;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyModel;

/**
 *
 */
public final class ObjectBuilderImpl<T>
    implements ObjectBuilder<T>
{
    private ObjectContext objectContext;
    private ModuleInstance moduleInstance;

    private Set<Object> adaptContext;
    private Object decoratedObject;
    private Map<String, Object> propertyValues;
    private Map<String, AbstractAssociation> associationValues;

    ObjectBuilderImpl( ModuleInstance moduleInstance, ObjectContext objectBinding )
    {
        this.objectContext = objectBinding;
        this.moduleInstance = moduleInstance;

    }

    public void adapt( Object anAdaptedObject )
    {
        Set<Object> context = getAdaptContext();
        context.add( anAdaptedObject );
    }

    public void decorate( Object aDecoratedObject )
    {
        decoratedObject = aDecoratedObject;
    }

    public void properties( PropertyValue... properties )
    {
        Map<String, Object> props = getPropertyValues();
        for( PropertyValue property : properties )
        {
            props.put( property.getName(), property );
        }
    }

    public T newInstance()
    {
        Map<String, Property> properties = new HashMap<String, Property>();
        Map<String, AbstractAssociation> associations = new HashMap<String, AbstractAssociation>();

        // Calculate total set of Properties for this Composite
        for( PropertyContext propertyContext : objectContext.getPropertyContexts() )
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
        for( AssociationContext mixinAssociation : objectContext.getAssociationContexts() )
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


        return (T) objectContext.newObjectInstance( moduleInstance, adaptContext, decoratedObject, properties, associations );
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

    public void inject( T instance )
    {
        Map<String, Property> properties = new HashMap<String, Property>();
        Map<String, AbstractAssociation> associations = new HashMap<String, AbstractAssociation>();

        // Inject existing object
        objectContext.inject( instance, moduleInstance, adaptContext, decoratedObject, properties, associations );
    }

    // Private ------------------------------------------------------
    private Set<Object> getAdaptContext()
    {
        if( adaptContext == null )
        {
            adaptContext = new LinkedHashSet<Object>();
        }

        return adaptContext;
    }

    private Map<String, Object> getPropertyValues()
    {
        if( propertyValues == null )
        {
            propertyValues = new LinkedHashMap<String, Object>();
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

}