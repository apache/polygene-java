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

package org.qi4j.runtime.entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.ConstraintViolationException;
import org.qi4j.property.AbstractPropertyInstance;
import org.qi4j.property.Property;
import org.qi4j.runtime.composite.qi.ConstraintsModel;
import org.qi4j.runtime.composite.qi.ValueConstraintsInstance;
import org.qi4j.runtime.composite.qi.ValueConstraintsModel;
import org.qi4j.runtime.property.PropertiesInstance;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.structure.PropertyDescriptor;
import org.qi4j.util.MetaInfo;

/**
 * TODO
 */
public class EntityPropertiesModel
{
    Set<Class> mixinTypes = new HashSet<Class>();
    List<EntityPropertyModel> propertyModels = new ArrayList<EntityPropertyModel>();
    Map<Method, EntityPropertyModel> mapMethodPropertyModel = new HashMap<Method, EntityPropertyModel>();
    Map<String, Method> accessors = new HashMap<String, Method>();
    private ConstraintsModel constraints;

    public EntityPropertiesModel( ConstraintsModel constraints )
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
                    EntityPropertyModel propertyModel = new EntityPropertyModel( method, valueConstraintsInstance, new MetaInfo(), null ); //TODO Take default value from assembly
                    propertyModels.add( propertyModel );
                    accessors.put( propertyModel.qualifiedName(), propertyModel.accessor() );
                    mapMethodPropertyModel.put( method, propertyModel );
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

    public Property<?> newInstance( Method accessor, EntityState state )
    {
        return mapMethodPropertyModel.get( accessor ).newEntityInstance( state );
    }

    public PropertyDescriptor getPropertyByName( String name )
    {
        for( EntityPropertyModel propertyModel : propertyModels )
        {
            if( propertyModel.name().equals( name ) )
            {
                return propertyModel;
            }
        }
        return null;
    }

    public void setState( PropertiesInstance properties, EntityState entityState )
        throws ConstraintViolationException
    {
        for( Map.Entry<Method, EntityPropertyModel> methodEntityPropertyModelEntry : mapMethodPropertyModel.entrySet() )
        {
            Property property = properties.propertyFor( methodEntityPropertyModelEntry.getKey() );
            methodEntityPropertyModelEntry.getValue().setState( property, entityState );
        }
    }
}
