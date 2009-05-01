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

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import static org.qi4j.api.common.TypeName.*;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.entity.RDF;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.entity.SchemaVersion;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.util.PeekableStringTokenizer;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * JAVADOC
 */
public abstract class ValueType
    implements Serializable
{
    public static ValueType newValueType( Type type )
    {
        return newValueType( null, type );
    }

    public static ValueType newValueType( Map<Type, ValueType> typeMap, Type type )
    {
        ValueType valueType = null;
        if( CollectionType.isCollection( type ) )
        {
            if( type instanceof ParameterizedType )
            {
                ParameterizedType pt = (ParameterizedType) type;
                valueType = new CollectionType( nameOf( type ), newValueType( typeMap, pt.getActualTypeArguments()[ 0 ] ) );
            }
            else
            {
                valueType = new CollectionType( nameOf( type ), newValueType( typeMap, Object.class ) );
            }
        }
        else if( ValueCompositeType.isValueComposite( type ) )
        {
            if( typeMap != null )
            {
                valueType = typeMap.get( type );
            }

            if( valueType == null )
            {
                Class valueTypeClass = (Class) type;

                List<PropertyType> types = new ArrayList<PropertyType>();
                valueType = new ValueCompositeType( nameOf( valueTypeClass ), types );
                if( typeMap == null )
                {
                    typeMap = new HashMap<Type, ValueType>();
                }
                typeMap.put( type, valueType );
                for( Method method : valueTypeClass.getMethods() )
                {
                    Type returnType = method.getGenericReturnType();
                    if( returnType instanceof ParameterizedType && ( (ParameterizedType) returnType ).getRawType().equals( Property.class ) )
                    {
                        Type propType = ( (ParameterizedType) returnType ).getActualTypeArguments()[ 0 ];
                        RDF rdfAnnotation = method.getAnnotation( RDF.class );
                        String rdf = rdfAnnotation == null ? null : rdfAnnotation.value();
                        Queryable queryableAnnotation = method.getAnnotation( Queryable.class );
                        boolean queryable = queryableAnnotation == null || queryableAnnotation.value();
                        ValueType propValueType = newValueType( typeMap, propType );
                        PropertyType propertyType = new PropertyType( QualifiedName.fromMethod( method ), propValueType, rdf, queryable, PropertyType.PropertyTypeEnum.IMMUTABLE );
                        types.add( propertyType );
                    }
                }
                Collections.sort( types ); // Sort by property name
            }
        }
        else if( EnumType.isEnum( type ) )
        {
            valueType = new EnumType( nameOf( type ) );
        }
        else if( StringType.isString( type ) )
        {
            valueType = new StringType( nameOf( type ) );
        }
        else if( NumberType.isNumber( type ) )
        {
            valueType = new NumberType( nameOf( type ) );
        }
        else if( BooleanType.isBoolean( type ) )
        {
            valueType = new BooleanType( nameOf( type ) );
        }
        else
        {
            // TODO: shouldn't we check that the type is a Serializable?
            valueType = new SerializableType( nameOf( type ) );
        }

        return valueType;
    }

    public abstract TypeName type();

    public abstract void versionize( SchemaVersion schemaVersion );

    public abstract void toJSON( Object value, StringBuilder json );

    public abstract Object fromJSON( PeekableStringTokenizer json, Module module );

    public String toQueryParameter( Object value )
    {
        if( value == null )
        {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        toJSON( value, builder );
        return builder.toString();
    }

    public Object fromQueryParameter( String parameter, Module module )
    {
        if( parameter == null )
        {
            return null;
        }

        return fromJSON( new PeekableStringTokenizer( parameter ), module );
    }
}
