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

package org.qi4j.spi.structure;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * TODO
 */
public final class PropertyDescriptor
{
    private Class valueType;
    private Map<Class, Serializable> propertyInfos;
    private Method accessor;
    private Object defaultValue;

    public PropertyDescriptor( Class valueType, Map<Class, Serializable> propertyInfos, Method accessor, Object defaultValue )
    {
        this.valueType = valueType;
        this.propertyInfos = propertyInfos;
        this.accessor = accessor;
        this.defaultValue = defaultValue;
    }

    public Class getValueType()
    {
        return valueType;
    }

    public Map<Class, Serializable> getPropertyInfos()
    {
        return propertyInfos;
    }

    public Method getAccessor()
    {
        return accessor;
    }

    public Object getDefaultValue()
    {
        return defaultValue;
    }
}
