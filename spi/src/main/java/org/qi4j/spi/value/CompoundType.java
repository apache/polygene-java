/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.value;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.property.PropertyType;

/**
 * TODO
 */
public class CompoundType
    implements ValueType
{
    public static boolean isCompound( Type type)
    {
        return type instanceof Class && ValueComposite.class.isAssignableFrom((Class)type);
    }

    private String type;
    private List<PropertyType> types;

    public CompoundType( String type, List<PropertyType> types )
    {
        this.type = type;
        Collections.sort(types); // Sort by property name
        this.types = types;
    }

    public String type()
    {
        return type;
    }

    public List<PropertyType> types()
    {
        return types;
    }

    @Override public String toString()
    {
        return type;
    }
}
