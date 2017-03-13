/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.polygene.serialization.javaxjson;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.serialization.Converter;
import org.apache.polygene.api.serialization.Converters;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ArrayType;
import org.apache.polygene.api.type.CollectionType;
import org.apache.polygene.api.type.MapType;
import org.apache.polygene.api.type.ValueCompositeType;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.spi.serialization.AbstractTextDeserializer;
import org.apache.polygene.spi.serialization.JsonDeserializer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;
import static org.apache.polygene.api.util.Collectors.toMapWithNullValues;
import static org.apache.polygene.serialization.javaxjson.JavaxJson.asString;
import static org.apache.polygene.serialization.javaxjson.JavaxJson.requireJsonArray;
import static org.apache.polygene.serialization.javaxjson.JavaxJson.requireJsonObject;
import static org.apache.polygene.serialization.javaxjson.JavaxJson.requireJsonStructure;

public class JavaxJsonDeserializer extends AbstractTextDeserializer implements JsonDeserializer
{
    @This
    private Converters converters;

    @This
    private JavaxJsonAdapters adapters;

    @Uses
    private ServiceDescriptor descriptor;

    @Override
    public <T> T fromJson( ModuleDescriptor module, ValueType valueType, JsonValue state )
    {
        return doDeserialize( module, valueType, state );
    }

    @SuppressWarnings( "unchecked" )
    private <T> T doDeserialize( ModuleDescriptor module, ValueType valueType, JsonValue json )
    {
        if( json == null || JsonValue.NULL.equals( json ) )
        {
            return null;
        }
        Converter<Object> converter = converters.converterFor( valueType );
        if( converter != null )
        {
            return (T) converter.fromString( doDeserialize( module, ValueType.STRING, json ).toString() );
        }
        JavaxJsonAdapter<?> adapter = adapters.adapterFor( valueType );
        if( adapter != null )
        {
            return (T) adapter.deserialize( json, ( jsonValue, type ) -> doDeserialize( module, type, jsonValue ) );
        }
        Class<? extends ValueType> valueTypeClass = valueType.getClass();
        if( ArrayType.class.isAssignableFrom( valueTypeClass ) )
        {
            return (T) deserializeArray( module, (ArrayType) valueType, json );
        }
        if( CollectionType.class.isAssignableFrom( valueTypeClass ) )
        {
            return (T) deserializeCollection( module, (CollectionType) valueType, requireJsonArray( json ) );
        }
        if( MapType.class.isAssignableFrom( valueTypeClass ) )
        {
            return (T) deserializeMap( module, (MapType) valueType, requireJsonStructure( json ) );
        }
        if( ValueCompositeType.class.isAssignableFrom( valueTypeClass ) )
        {
            return (T) deserializeValueComposite( module, (ValueCompositeType) valueType, requireJsonObject( json ) );
        }
        return doGuessDeserialize( module, valueType, json );
    }

    private Object deserializeArray( ModuleDescriptor module, ArrayType arrayType, JsonValue json )
    {
        if( arrayType.isArrayOfPrimitiveBytes() && json.getValueType() == JsonValue.ValueType.STRING )
        {
            byte[] bytes = asString( json ).getBytes( UTF_8 );
            return Base64.getDecoder().decode( bytes );
        }
        if( json.getValueType() == JsonValue.ValueType.ARRAY )
        {
            CollectionType collectionType = CollectionType.listOf( arrayType.collectedType() );
            List<Object> collection = (List<Object>) deserializeCollection( module,
                                                                            collectionType,
                                                                            requireJsonArray( json ) );
            Object array = Array.newInstance( arrayType.collectedType().primaryType(), collection.size() );
            for( int idx = 0; idx < collection.size(); idx++ )
            {
                Array.set( array, idx, collection.get( idx ) );
            }
            return array;
        }
        throw new SerializationException( "Don't know how to deserialize " + arrayType + " from " + json );
    }

    @SuppressWarnings( "unchecked" )
    private <T> T doGuessDeserialize( ModuleDescriptor module, ValueType valueType, JsonValue json )
    {
        switch( json.getValueType() )
        {
            case OBJECT:
                JsonObject object = (JsonObject) json;
                String typeInfo = object.getString( getTypeInfoPropertyName(), valueType.primaryType().getName() );
                ValueDescriptor valueDescriptor = module.valueDescriptor( typeInfo );
                if( valueDescriptor != null )
                {
                    return (T) deserializeValueComposite( module, valueDescriptor.valueType(), object );
                }
            case STRING:
                byte[] bytes = Base64.getDecoder().decode( asString( json ).getBytes( UTF_8 ) );
                return (T) deserializeJava( bytes );
            default:
                throw new SerializationException( "Don't know how to deserialize " + valueType + " from " + json );
        }
    }

    private <T> Collection<T> deserializeCollection( ModuleDescriptor module, CollectionType collectionType,
                                                     JsonArray json )
    {
        return (Collection<T>) json.stream()
                                   .map( item -> doDeserialize( module, collectionType.collectedType(), item ) )
                                   .collect( toCollection(
                                       () -> collectionType.isSet() ? new LinkedHashSet<>() : new ArrayList<>() ) );
    }

    /**
     * Map deserialization.
     *
     * {@literal JsonObject}s are deserialized to {@literal Map<String, ?>}.
     * {@literal JsonArray}s of key/value {@literal JsonObject}s are deserialized to {@literal Map<?, ?>}.
     */
    private Map<?, ?> deserializeMap( ModuleDescriptor module, MapType mapType, JsonStructure json )
    {
        if( json.getValueType() == JsonValue.ValueType.OBJECT )
        {
            JsonObject object = (JsonObject) json;
            return object.entrySet().stream()
                         .map( entry -> new AbstractMap.SimpleImmutableEntry<>(
                             entry.getKey(),
                             doDeserialize( module, mapType.valueType(), entry.getValue() ) ) )
                         .collect( toMapWithNullValues( LinkedHashMap::new ) );
        }
        if( json.getValueType() == JsonValue.ValueType.ARRAY )
        {
            JsonArray array = (JsonArray) json;
            return array.stream()
                        .map( JsonObject.class::cast )
                        .map( entry -> new AbstractMap.SimpleImmutableEntry<>(
                            doDeserialize( module, mapType.keyType(), entry.get( "key" ) ),
                            doDeserialize( module, mapType.valueType(), entry.get( "value" ) )
                        ) )
                        .collect( toMapWithNullValues( LinkedHashMap::new ) );
        }
        throw new SerializationException( "Don't know how to deserialize " + mapType + " from " + json );
    }

    private Object deserializeValueComposite( ModuleDescriptor module, ValueCompositeType valueType, JsonObject json )
    {
        String typeInfoName = getTypeInfoPropertyName();
        String typeInfo = json.getString( typeInfoName, null );
        if( typeInfo != null )
        {
            ValueDescriptor descriptor = module.valueDescriptor( typeInfo );
            if( descriptor == null )
            {
                throw new SerializationException(
                    typeInfoName + ": " + typeInfo + " could not be resolved while deserializing " + json );
            }
            valueType = descriptor.valueType();
        }
        ValueBuilder builder = module.instance().newValueBuilderWithState(
            valueType.primaryType(),
            propertyFunction( module, json ),
            associationFunction( module, json ),
            manyAssociationFunction( module, json ),
            namedAssociationFunction( module, json ) );
        return builder.newInstance();
    }

    private Function<PropertyDescriptor, Object> propertyFunction( ModuleDescriptor module, JsonObject object )
    {
        return property ->
        {
            JsonValue jsonValue = object.get( property.qualifiedName().name() );
            if( jsonValue != null )
            {
                Object value = doDeserialize( module, property.valueType(), jsonValue );
                if( property.isImmutable() )
                {
                    if( value instanceof Set )
                    {
                        return unmodifiableSet( (Set<?>) value );
                    }
                    else if( value instanceof List )
                    {
                        return unmodifiableList( (List<?>) value );
                    }
                    else if( value instanceof Map )
                    {
                        return unmodifiableMap( (Map<?, ?>) value );
                    }
                }
                return value;
            }
            return property.resolveInitialValue( module );
        };
    }

    private Function<AssociationDescriptor, EntityReference> associationFunction( ModuleDescriptor module,
                                                                                  JsonObject object )
    {
        return association -> doDeserialize( module, ValueType.ENTITY_REFERENCE,
                                             object.get( association.qualifiedName().name() ) );
    }

    private Function<AssociationDescriptor, Stream<EntityReference>> manyAssociationFunction( ModuleDescriptor module,
                                                                                              JsonObject object )
    {
        return association ->
        {
            List<EntityReference> list = doDeserialize( module, ENTITY_REF_LIST_VALUE_TYPE,
                                                        object.get( association.qualifiedName().name() ) );
            return list == null ? Stream.empty() : list.stream();
        };
    }

    private Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociationFunction(
        ModuleDescriptor module, JsonObject object )
    {
        return association ->
        {
            Map<String, EntityReference> map = doDeserialize( module, ENTITY_REF_MAP_VALUE_TYPE,
                                                              object.get( association.qualifiedName().name() ) );
            return map == null ? Stream.empty() : map.entrySet().stream();
        };
    }

    private String getTypeInfoPropertyName()
    {
        return JavaxJsonSettings.orDefault( descriptor.metaInfo( JavaxJsonSettings.class ) )
                                .getTypeInfoPropertyName();
    }
}
