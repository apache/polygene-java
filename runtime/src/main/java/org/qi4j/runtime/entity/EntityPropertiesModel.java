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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.composite.ValueConstraintsModel;
import org.qi4j.runtime.property.AbstractPropertiesModel;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.util.Annotations;

/**
 * Model for Properties in Entities
 */
public final class EntityPropertiesModel
    extends AbstractPropertiesModel<EntityPropertyModel>
{
    public EntityPropertiesModel( ConstraintsModel constraints,
                                  PropertyDeclarations propertyDeclarations,
                                  boolean immutable
    )
    {
        super( constraints, propertyDeclarations, immutable );
    }

    public Set<PropertyType> propertyTypes()
    {
        Set<PropertyType> propertyTypes = new LinkedHashSet<PropertyType>();
        for( EntityPropertyModel propertyModel : properties() )
        {
            propertyTypes.add( propertyModel.propertyType() );
        }
        return propertyTypes;
    }

    public <T> Property<T> newInstance( Method accessor, EntityState entityState )
    {
        return mapMethodPropertyModel.get( accessor ).newInstance( entityState );
    }

    protected EntityPropertyModel newPropertyModel( Method method, Class compositeType )
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
        Object defaultValue = propertyDeclarations.getInitialValue( method );
        boolean immutable = this.immutable || metaInfo.get( Immutable.class ) != null;
        EntityPropertyModel propertyModel = new EntityPropertyModel( method, compositeType, immutable, valueConstraintsInstance, metaInfo, defaultValue );
        return propertyModel;
    }

    public EntityPropertiesInstance newInstance( EntityState entityState )
    {
        return new EntityPropertiesInstance( this, entityState );
    }
}
