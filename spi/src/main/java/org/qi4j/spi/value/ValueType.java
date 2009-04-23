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

import org.qi4j.api.common.TypeName;
import org.qi4j.api.common.QualifiedName;
import static org.qi4j.api.common.TypeName.nameOf;
import org.qi4j.api.structure.Module;
import org.qi4j.api.property.Property;
import org.qi4j.api.entity.RDF;
import org.qi4j.api.entity.Queryable;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.entity.SchemaVersion;
import org.qi4j.spi.util.PeekableStringTokenizer;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

/**
 * JAVADOC
 */
public abstract class ValueType
    implements Serializable
{
    public static ValueType newValueType( Type type )
    {
        ValueType valueType;
        if( CollectionType.isCollection( type ) )
        {
            if( type instanceof ParameterizedType )
            {
                ParameterizedType pt = (ParameterizedType) type;
                valueType = new CollectionType( nameOf( type ), newValueType( pt.getActualTypeArguments()[ 0 ] ) );
            }
            else
            {
                valueType = new CollectionType( nameOf( type ), newValueType( Object.class ) );
            }
        }
        else if( ValueCompositeType.isValueComposite( type ) )
        {
            Class valueTypeClass = (Class) type;
            List<PropertyType> types = new ArrayList<PropertyType>();
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
                    PropertyType propertyType = new PropertyType( QualifiedName.fromMethod( method ), newValueType( propType ), rdf, queryable, PropertyType.PropertyTypeEnum.IMMUTABLE );
                    types.add( propertyType );
                }
            }
            valueType = new ValueCompositeType( nameOf( valueTypeClass ), types );
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

    public abstract void toJSON( Object value, StringBuilder json, Qi4jSPI spi );

    public abstract Object fromJSON( PeekableStringTokenizer json, Module module );
}
