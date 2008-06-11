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
import org.qi4j.composite.State;
import org.qi4j.property.AbstractPropertyInstance;
import org.qi4j.property.Property;
import org.qi4j.runtime.composite.qi.ConstraintsModel;
import org.qi4j.runtime.composite.qi.ValueConstraintsInstance;
import org.qi4j.runtime.composite.qi.ValueConstraintsModel;
import org.qi4j.spi.structure.PropertyDescriptor;
import org.qi4j.util.MetaInfo;

/**
 * TODO
 */
public class PropertiesModel
{
    Set<Class> mixinTypes = new HashSet<Class>();
    List<PropertyModel> propertyModels = new ArrayList<PropertyModel>();
    Map<String, Method> accessors = new HashMap<String, Method>();
    private ConstraintsModel constraints;

    public PropertiesModel( ConstraintsModel constraints )
    {
        this.constraints = constraints;
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
                        valueConstraintsInstance = valueConstraintsModel.newInstance( method );
                    }
                    PropertyModel propertyModel = new PropertyModel( method, valueConstraintsInstance, new MetaInfo(), null ); //TODO Take default value from assembly
                    propertyModels.add( propertyModel );
                    accessors.put( propertyModel.qualifiedName(), propertyModel.accessor() );
                }
            }
        }
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
}
