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
import java.lang.reflect.Method;
import java.util.*;

import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.structure.Module;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.property.Property;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.entity.SchemaVersion;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.util.PeekableStringTokenizer;

/**
 * JAVADOC
 */
public class ValueCompositeType
        implements ValueType
{
    public static boolean isValueComposite(Type type)
    {
        return type instanceof Class && ((Class) type).isInterface();
    }

    private final TypeName type;
    private List<PropertyType> types;

    public ValueCompositeType(TypeName type, List<PropertyType> types)
    {
        this.type = type;
        Collections.sort(types); // Sort by property name
        this.types = types;
    }

    public TypeName type()
    {
        return type;
    }

    public List<PropertyType> types()
    {
        return types;
    }

    public PropertyType propertyWithVersion(String version)
    {
        for (PropertyType propertyType : types)
        {
            if (propertyType.stateName().version().equals(version))
                return propertyType;
        }

        return null;
    }

    public void versionize(SchemaVersion schemaVersion)
    {
        schemaVersion.versionize(type);
        for (PropertyType propertyType : types)
        {
            propertyType.versionize(schemaVersion);
        }
    }

    @Override
    public String toString()
    {
        return type.toString();
    }

    public void toJSON(Object value, StringBuilder json, Qi4jSPI spi)
    {
        json.append('{');
        ValueComposite valueComposite = (ValueComposite) value;
        StateHolder state = spi.getState(valueComposite);
        final Map<QualifiedName, Object> values = new HashMap<QualifiedName, Object>();
        state.visitProperties(new StateHolder.StateVisitor()
        {
            public void visitProperty(QualifiedName name, Object value)
            {
                values.put(name, value);
            }
        });

        String comma = "";
        for (PropertyType propertyType : types)
        {
            json.append(comma);
            json.append(propertyType.qualifiedName().name()).append(':');

            Object propertyValue = values.get( propertyType.qualifiedName() );
            if (propertyValue == null)
            {
                json.append("null");
            } else
            {
                propertyType.type().toJSON( propertyValue, json, spi);
            }
            comma = ",";
        }
        json.append('}');
    }

    public Object fromJSON(PeekableStringTokenizer json, Module module)
    {
        String token = json.nextToken("{");

        final Map<QualifiedName, Object> values = new HashMap<QualifiedName, Object>();
        for (PropertyType propertyType : types)
        {
            String name = json.nextToken(":");
            token = json.nextToken(",:");

            token = json.peekNextToken(",}");
            Object value;
            if (token.equals( "null" ))
                value = null;
            else
                value = propertyType.type().fromJSON(json, module);

            if (!name.equals(propertyType.qualifiedName().name()))
                throw new IllegalStateException("Could not deserialize value. Expected '"+propertyType.qualifiedName()+"' but got '"+name);

            values.put(propertyType.qualifiedName(), value);
            token = json.nextToken(",}");
        }

        try
        {
            ValueBuilder valueBuilder = module.valueBuilderFactory().newValueBuilder(module.classLoader().loadClass(type.name()));
            valueBuilder.withState(new StateHolder()
            {
                public <T> Property<T> getProperty(Method propertyMethod)
                {
                    return null;
                }

                public void visitProperties(StateVisitor visitor)
                {
                    for (Map.Entry<QualifiedName, Object> qualifiedNameObjectEntry : values.entrySet())
                    {
                        visitor.visitProperty(qualifiedNameObjectEntry.getKey(), qualifiedNameObjectEntry.getValue());
                    }
                }
            });

            return valueBuilder.newInstance();
        } catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("Could not deserialize value", e);
        }
    }
}
