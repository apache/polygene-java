/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.composite.ValueConstraintsModel;
import org.qi4j.runtime.util.Annotations;
import org.qi4j.spi.property.PropertyDescriptor;

/**
 * TODO
 */
public final class PropertiesModel
{
    final Set<Method> methods = new HashSet<Method>();
    final List<PropertyModel> propertyModels = new ArrayList<PropertyModel>();
    final Map<String, Method> accessors = new HashMap<String, Method>();
    private final ConstraintsModel constraints;
    private PropertyDeclarations propertyDeclarations;
    private boolean immutable;

    public PropertiesModel( ConstraintsModel constraints, PropertyDeclarations propertyDeclarations, boolean immutable )
    {
        this.constraints = constraints;
        this.propertyDeclarations = propertyDeclarations;
        this.immutable = immutable;
    }

    public void addPropertyFor( Method method )
    {
        if( !methods.contains( method ) )
        {
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                PropertyModel propertyModel = newPropertyModel( method );
                propertyModels.add( propertyModel );
                accessors.put( propertyModel.qualifiedName(), propertyModel.accessor() );
            }
        }
    }

    public List<PropertyDescriptor> properties()
    {
        return new ArrayList<PropertyDescriptor>( propertyModels );
    }

    public PropertiesInstance newBuilderInstance()
    {
        Map<Method, Property<?>> properties = new HashMap<Method, Property<?>>();
        for( PropertyModel propertyModel : propertyModels )
        {
            Property property = propertyModel.newBuilderInstance();
            properties.put( propertyModel.accessor(), property );
        }

        return new PropertiesInstance( properties );
    }

    public PropertiesInstance newDefaultInstance()
    {
        Map<Method, Property<?>> properties = new HashMap<Method, Property<?>>();
        for( PropertyModel propertyModel : propertyModels )
        {
            Property property = propertyModel.newDefaultInstance();
            properties.put( propertyModel.accessor(), property );
        }

        return new PropertiesInstance( properties );
    }

    public PropertiesInstance newInstance( StateHolder state )
    {
        Map<Method, Property<?>> properties = new HashMap<Method, Property<?>>();
        for( PropertyModel propertyModel : propertyModels )
        {
            Property property = propertyModel.newInstance( state.getProperty( propertyModel.accessor() ).get() );
            properties.put( propertyModel.accessor(), property );
        }

        return new PropertiesInstance( properties );
    }

    public Method accessorFor( String qualifiedName )
    {
        return accessors.get( qualifiedName );
    }

    public PropertyDescriptor getPropertyByName( String name )
    {
        for( PropertyModel propertyModel : propertyModels )
        {
            if( propertyModel.name().equals( name ) )
            {
                return propertyModel;
            }
        }
        return null;
    }

    public PropertyDescriptor getPropertyByQualifiedName( String name )
    {
        for( PropertyModel propertyModel : propertyModels )
        {
            if( propertyModel.qualifiedName().equals( name ) )
            {
                return propertyModel;
            }
        }
        return null;
    }

    public void checkConstraints( PropertiesInstance properties )
        throws ConstraintViolationException
    {
        for( PropertyModel propertyModel : propertyModels )
        {
            Property property = properties.propertyFor( propertyModel.accessor() );
            if( !propertyModel.isComputed() )
            {
                propertyModel.checkConstraints( property.get() );
            }
        }
    }

    private PropertyModel newPropertyModel( Method method )
    {
        Annotation[] annotations = Annotations.getMethodAndTypeAnnotations( method );
        boolean optional = Annotations.getAnnotationOfType( annotations, Optional.class ) != null;
        ValueConstraintsModel valueConstraintsModel = constraints.constraintsFor( annotations, GenericPropertyInfo.getPropertyType( method ), method.getName(), optional );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = propertyDeclarations.getMetaInfo( method );
        Object initialValue = propertyDeclarations.getInitialValue( method );        boolean immutable = this.immutable || metaInfo.get( Immutable.class ) != null;
        PropertyModel propertyModel = new PropertyModel( method, immutable, valueConstraintsInstance, metaInfo, initialValue );
        return propertyModel;
    }
}
