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

package org.qi4j.spi.property;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.property.Property;

/**
 * TODO
 */
public class PropertyType
    implements Serializable
{
    public enum PropertyTypeEnum
    {
        MUTABLE, IMMUTABLE, COMPUTED
    }

    private final String qualifiedName;
    private final ValueType type;
    private final String uri;
    private String rdf;
    private final boolean queryable;
    private final PropertyTypeEnum propertyType;

    public PropertyType( final String qualifiedName,
                         final Type type,
                         final String uri,
                         final String rdf,
                         final boolean queryable,
                         final PropertyTypeEnum propertyType )
    {
        this.qualifiedName = qualifiedName;
        this.type = createValueType(type);
        this.uri = uri;
        this.rdf = rdf;
        this.queryable = queryable;
        this.propertyType = propertyType;
    }

    public String qualifiedName()
    {
        return qualifiedName;
    }

    public ValueType type()
    {
        return type;
    }

    public PropertyTypeEnum propertyType()
    {
        return propertyType;
    }

    public String uri()
    {
        return uri;
    }

    public String rdf()
    {
        return rdf;
    }

    public boolean queryable()
    {
        return queryable;
    }

    @Override public String toString()
    {
        return qualifiedName + "(" + type + "," + uri + ")";
    }

    private ValueType createValueType(Type type)
    {
        ValueType valueType = null;
        if (CollectionType.isCollection( type ))
        {
            ParameterizedType pt = (ParameterizedType) type;
            valueType = new CollectionType(((Class)pt.getRawType()).getName(), createValueType( pt.getActualTypeArguments()[0] ));
        } else if (CompoundType.isCompound( type ))
        {
            Class valueTypeClass = (Class)type;
            List<ValueType> types = new ArrayList<ValueType>( ); 
            for( Method method : valueTypeClass.getMethods() )
            {
                Type returnType = method.getGenericReturnType();
                if (returnType instanceof ParameterizedType && ((ParameterizedType)returnType).getRawType().equals( Property.class))
                {
                    Type propType = ((ParameterizedType)returnType).getActualTypeArguments()[0];
                    types.add(createValueType( propType ));
                }
            }
            valueType = new CompoundType(valueTypeClass.getName(), types);
        } else if (PrimitiveType.isPrimitive( type ))
        {
            valueType = new PrimitiveType(((Class)type).getName());
        } else
        {
            valueType = new SerializableType(((Class)type).getName());
        }

        return valueType;
    }
}
