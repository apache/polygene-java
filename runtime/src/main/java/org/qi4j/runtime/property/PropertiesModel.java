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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.composite.ConstraintViolationException;
import org.qi4j.composite.State;
import org.qi4j.property.AbstractPropertyInstance;
import org.qi4j.property.Property;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.composite.ValueConstraintsModel;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.util.MetaInfo;

/**
 * TODO
 */
public final class PropertiesModel
{
    final Set<Class> mixinTypes = new HashSet<Class>();
    final List<PropertyModel> propertyModels = new ArrayList<PropertyModel>();
    final Map<String, Method> accessors = new HashMap<String, Method>();
    private final ConstraintsModel constraints;
    private PropertyDeclarations propertyDeclarations;

    public PropertiesModel( ConstraintsModel constraints, PropertyDeclarations propertyDeclarations )
    {
        this.constraints = constraints;
        this.propertyDeclarations = propertyDeclarations;
    }

    public void addPropertiesFor( Class mixinType )
    {
        if( !mixinTypes.contains( mixinType ) )
        {
            for( Method method : mixinType.getMethods() )
            {
                if( Property.class.isAssignableFrom( method.getReturnType() ) )
                {
                    ValueConstraintsModel valueConstraintsModel = constraints.constraintsFor( method.getAnnotations(), AbstractPropertyInstance.getPropertyType( method ) );
                    ValueConstraintsInstance valueConstraintsInstance = null;
                    if( valueConstraintsModel.isConstrained() )
                    {
                        valueConstraintsInstance = valueConstraintsModel.newInstance();
                    }
                    MetaInfo metaInfo = propertyDeclarations.getMetaInfo( method );
                    Object defaultValue = propertyDeclarations.getDefaultValue( method );
                    PropertyModel propertyModel = new PropertyModel( method, valueConstraintsInstance, metaInfo, defaultValue ); //TODO Take default value from assembly
                    propertyModels.add( propertyModel );
                    accessors.put( propertyModel.qualifiedName(), propertyModel.accessor() );
                }
            }
        }
    }


    public List<PropertyDescriptor> properties()
    {
        return new ArrayList<PropertyDescriptor>( propertyModels );
    }


    public PropertiesInstance newDefaultInstance()
    {
        Map<Method, Property> properties = new HashMap<Method, Property>();
        for( PropertyModel propertyModel : propertyModels )
        {
            Property property = propertyModel.newInstance();
            properties.put( propertyModel.accessor(), property );
        }

        return new PropertiesInstance( properties );
    }

    public PropertiesInstance newInstance( State state )
    {
        Map<Method, Property> properties = new HashMap<Method, Property>();
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
}
