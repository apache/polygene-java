/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.property;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;
import org.qi4j.property.PropertyInfo;
import org.qi4j.spi.composite.PropertyResolution;

/**
 * TODO
 */
public final class PropertyBinding
    implements PropertyInfo
{
    private PropertyResolution propertyResolution;
    private Map<Class, Serializable> propertyInfo;
    private Object defaultValue;

    public PropertyBinding( PropertyResolution propertyResolution, Map<Class, Serializable> propertyInfo, Object defaultValue )
    {
        this.defaultValue = defaultValue;
        this.propertyInfo = propertyInfo;
        this.propertyResolution = propertyResolution;
    }

    public PropertyResolution getPropertyResolution()
    {
        return propertyResolution;
    }

    public <T> T getPropertyInfo( Class<T> infoClass )
    {
        return infoClass.cast( propertyInfo.get( infoClass ) );
    }

    public String getName()
    {
        return propertyResolution.getPropertyModel().getName();
    }

    public String getQualifiedName()
    {
        return propertyResolution.getPropertyModel().getQualifiedName();
    }

    public Type getPropertyType()
    {
        return propertyResolution.getPropertyModel().getType();
    }

    public Object getDefaultValue()
    {
        return defaultValue;
    }
}
