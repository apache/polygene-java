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

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.util.Annotations;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.composite.ValueConstraintsModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;

import static org.qi4j.api.util.Annotations.isType;
import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.first;

/**
 * Model for properties in Transient Composites
 */
public final class PropertiesModel
    extends AbstractPropertiesModel<PropertyModel>
{
    public PropertiesModel( ConstraintsModel constraints,
                            PropertyDeclarations propertyDeclarations,
                            boolean immutable)
    {
        super( constraints, propertyDeclarations, immutable );
    }

    protected PropertyModel newPropertyModel( AccessibleObject accessor)
    {
        Iterable<Annotation> annotations = Annotations.getAccessorAndTypeAnnotations( accessor );
        boolean optional = first( filter( isType( Optional.class ), annotations ) ) != null;
        ValueConstraintsModel valueConstraintsModel = constraints.constraintsFor( annotations, GenericPropertyInfo.getPropertyType( accessor ), ((Member)accessor)
            .getName(), optional );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = propertyDeclarations.getMetaInfo( accessor );
        Object initialValue = propertyDeclarations.getInitialValue( accessor );
        boolean immutable = this.immutable || metaInfo.get( Immutable.class ) != null;
        PropertyModel propertyModel = new PropertyModel( accessor, immutable, valueConstraintsInstance, metaInfo, initialValue );
        return propertyModel;
    }
}
