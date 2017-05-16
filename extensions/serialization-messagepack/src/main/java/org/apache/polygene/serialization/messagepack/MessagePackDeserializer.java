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
package org.apache.polygene.serialization.messagepack;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.composite.CompositeDescriptor;
import org.apache.polygene.api.composite.StatefulAssociationCompositeDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.serialization.Converter;
import org.apache.polygene.api.serialization.Converters;
import org.apache.polygene.api.serialization.Deserializer;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ArrayType;
import org.apache.polygene.api.type.CollectionType;
import org.apache.polygene.api.type.EnumType;
import org.apache.polygene.api.type.MapType;
import org.apache.polygene.api.type.StatefulAssociationValueType;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.spi.serialization.AbstractBinaryDeserializer;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.ImmutableValue;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.apache.polygene.api.util.Collectors.toMap;

@Mixins( MessagePackDeserializer.Mixin.class )
public interface MessagePackDeserializer extends Deserializer
{
    class Mixin extends AbstractBinaryDeserializer
    {
        @This
        private Converters converters;

        @This
        private MessagePackAdapters adapters;

        @Override
        public <T> T deserialize( ModuleDescriptor module, ValueType valueType, InputStream state )
        {
            try( MessageUnpacker unpacker = MessagePack.newDefaultUnpacker( state ) )
            {
                if( !unpacker.hasNext() )
                {
                    return null;
                }
                ImmutableValue value = unpacker.unpackValue();
                return doDeserialize( module, valueType, value );
            }
            catch( IOException ex )
            {
                throw new SerializationException( "Unable to deserialize " + valueType, ex );
            }
        }

        @SuppressWarnings( "unchecked" )
        private <T> T doDeserialize( ModuleDescriptor module, ValueType valueType, Value value )
        {
            try
            {
                if( value == null || value.isNilValue() )
                {
                    return null;
                }
                Converter<Object> converter = converters.converterFor( valueType );
                if( converter != null )
                {
                    return (T) converter.fromString( doDeserialize( module, ValueType.STRING, value ).toString() );
                }
                MessagePackAdapter<?> adapter = adapters.adapterFor( valueType );
                if( adapter != null )
                {
                    return (T) adapter.deserialize( value, ( val, type ) -> doDeserialize( module, valueType, val ) );
                }
                Class<? extends ValueType> valueTypeClass = valueType.getClass();
                if( EnumType.class.isAssignableFrom( valueTypeClass ) )
                {
                    return (T) Enum.valueOf( (Class) valueType.primaryType(), value.asStringValue().asString() );
                }
                if( ArrayType.class.isAssignableFrom( valueTypeClass ) )
                {
                    return (T) deserializeArray( module, (ArrayType) valueType, value );
                }
                if( CollectionType.class.isAssignableFrom( valueTypeClass ) )
                {
                    return (T) deserializeCollection( module, (CollectionType) valueType, value.asArrayValue() );
                }
                if( MapType.class.isAssignableFrom( valueTypeClass ) )
                {
                    return (T) deserializeMap( module, (MapType) valueType, value.asMapValue() );
                }
                if( StatefulAssociationValueType.class.isAssignableFrom( valueTypeClass ) )
                {
                    return (T) deserializeStatefulAssociationValue( module,
                                                                    (StatefulAssociationValueType<?>) valueType,
                                                                    value.asMapValue() );
                }
                return (T) doGuessDeserialize( module, valueType, value );
            }
            catch( IOException | ClassNotFoundException ex )
            {
                throw new SerializationException( "Unable to deserialize " + valueType + " from: " + value );
            }
        }

        private Object deserializeArray( ModuleDescriptor module, ArrayType arrayType, Value value ) throws IOException
        {
            if( arrayType.isArrayOfPrimitiveBytes() )
            {
                return value.asBinaryValue().asByteArray();
            }
            CollectionType collectionType = CollectionType.listOf( arrayType.collectedType() );
            List collection = (List) deserializeCollection( module, collectionType, value.asArrayValue() );
            Object array = Array.newInstance( arrayType.collectedType().primaryType(), collection.size() );
            for( int idx = 0; idx < collection.size(); idx++ )
            {
                Array.set( array, idx, collection.get( idx ) );
            }
            return array;
        }

        private Collection<?> deserializeCollection( ModuleDescriptor module, CollectionType collectionType,
                                                     ArrayValue value ) throws IOException
        {
            Collection<?> collection = collectionType.isSet() ? new LinkedHashSet( value.size() )
                                                              : new ArrayList( value.size() );
            for( Value element : value.list() )
            {
                collection.add( doDeserialize( module, collectionType.collectedType(), element ) );
            }
            return collection;
        }

        private Map<Object, Object> deserializeMap( ModuleDescriptor module, MapType mapType, MapValue value )
            throws IOException
        {
            Map<Object, Object> map = new LinkedHashMap<>( value.size() );
            for( Map.Entry<Value, Value> entry : value.entrySet() )
            {
                Object key = doDeserialize( module, mapType.keyType(), entry.getKey() );
                Object val = doDeserialize( module, mapType.valueType(), entry.getValue() );
                map.put( key, val );
            }
            return map;
        }

        private Object deserializeStatefulAssociationValue( ModuleDescriptor module,
                                                            StatefulAssociationValueType<?> valueType,
                                                            MapValue value ) throws IOException
        {
            Map<String, Value> namedValues = value.map().entrySet().stream().map(
                entry ->
                {
                    String key = doDeserialize( module, ValueType.STRING, entry.getKey() );
                    return new AbstractMap.SimpleImmutableEntry<>( key, entry.getValue() );
                }
            ).collect( toMap( HashMap::new ) );

            String typeInfo = null;
            if( namedValues.containsKey( "_type" ) )
            {
                typeInfo = doDeserialize( module, ValueType.STRING, namedValues.get( "_type" ) );
            }
            if( typeInfo != null )
            {
                // TODO What to do with typeInfo? Value or Entity ?
                StatefulAssociationCompositeDescriptor descriptor = statefulCompositeDescriptorFor( module, typeInfo );
                if( descriptor == null )
                {
                    throw new SerializationException(
                        "_type: " + typeInfo + " could not be resolved while deserializing " + value );
                }
                valueType = descriptor.valueType();
            }

            ValueBuilder builder = module.instance().newValueBuilderWithState(
                valueType.primaryType(),
                propertyFunction( valueType.module(), namedValues ),
                associationFunction( valueType.module(), namedValues ),
                manyAssociationFunction( valueType.module(), namedValues ),
                namedAssociationFunction( valueType.module(), namedValues ) );
            return builder.newInstance();
        }

        private Function<PropertyDescriptor, Object> propertyFunction( ModuleDescriptor module,
                                                                       Map<String, Value> namedValues )
        {
            return property ->
            {
                Value messagePackValue = namedValues.get( property.qualifiedName().name() );
                if( messagePackValue != null )
                {
                    Object value;
                    Converter<Object> converter = converters.converterFor( property );
                    if( converter != null )
                    {
                        value = converter.fromString( doDeserialize( module, ValueType.STRING, messagePackValue ) );
                    }
                    else
                    {
                        value = doDeserialize( module, property.valueType(), messagePackValue );
                    }
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
                                                                                      Map<String, Value> namedValues )
        {
            return association -> doDeserialize( module, ValueType.ENTITY_REFERENCE,
                                                 namedValues.get( association.qualifiedName().name() ) );
        }

        private Function<AssociationDescriptor, Stream<EntityReference>> manyAssociationFunction(
            ModuleDescriptor module, Map<String, Value> namedValues )
        {
            return association ->
            {
                List list = doDeserialize( module, ENTITY_REF_LIST_VALUE_TYPE,
                                           namedValues.get( association.qualifiedName().name() ) );
                return list == null ? Stream.empty() : list.stream();
            };
        }

        private Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociationFunction(
            ModuleDescriptor module, Map<String, Value> namedValues )
        {
            return association ->
            {
                Map map = doDeserialize( module, ENTITY_REF_MAP_VALUE_TYPE,
                                         namedValues.get( association.qualifiedName().name() ) );
                return map == null ? Stream.empty() : map.entrySet().stream();
            };
        }

        private Object doGuessDeserialize( ModuleDescriptor module, ValueType valueType, Value value )
            throws IOException, ClassNotFoundException
        {
            switch( value.getValueType() )
            {
                case MAP:
                    MapValue mapValue = value.asMapValue();
                    Optional<String> typeInfo = mapValue
                        .entrySet().stream()
                        .filter( entry -> entry.getKey().isStringValue() )
                        .map( entry ->
                              {
                                  String key = doDeserialize( module, ValueType.STRING, entry.getKey() );
                                  return new AbstractMap.SimpleImmutableEntry<>( key, entry.getValue() );
                              } )
                        .filter( entry -> "_type".equals( entry.getKey() ) )
                        .findFirst()
                        .map( entry -> doDeserialize( module, ValueType.STRING, entry.getValue() ) );
                    if( typeInfo.isPresent() )
                    {
                        StatefulAssociationCompositeDescriptor descriptor = statefulCompositeDescriptorFor(
                            module, typeInfo.get() );
                        if( descriptor != null )
                        {
                            return deserializeStatefulAssociationValue( ( (CompositeDescriptor) descriptor ).module(),
                                                                        descriptor.valueType(),
                                                                        mapValue );
                        }
                    }
                default:
                    throw new SerializationException( "Don't know how to deserialize " + valueType + " from " + value
                                                      + " (" + value.getValueType() + ")" );
            }
        }
    }
}
