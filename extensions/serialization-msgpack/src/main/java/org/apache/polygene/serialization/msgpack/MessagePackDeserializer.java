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
package org.apache.polygene.serialization.msgpack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
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
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.serialization.Deserializer;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.CollectionType;
import org.apache.polygene.api.type.EnumType;
import org.apache.polygene.api.type.MapType;
import org.apache.polygene.api.type.ValueCompositeType;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.spi.serialization.AbstractBinaryDeserializer;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.BinaryValue;
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
        private MessagePackAdapters adapters;

        @Override
        public <T> T deserialize( ModuleDescriptor module, ValueType valueType, InputStream state )
        {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker( state );
            try
            {
                if( !unpacker.hasNext() )
                {
                    return null;
                }
                ImmutableValue value = unpacker.unpackValue();
                return doDeserialize( module, valueType, value );
            }
            catch( IOException e )
            {
                throw new SerializationException( "Unable to deserialize " + valueType );
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
                MessagePackAdapter<?> adapter = adapters.adapterFor( valueType );
                if( adapter != null )
                {
                    return (T) adapter.deserialize( value, ( val, type ) -> doDeserialize( module, valueType, val ) );
                }
                if( EnumType.class.isAssignableFrom( valueType.getClass() ) )
                {
                    return (T) Enum.valueOf( (Class) valueType.primaryType(), value.asStringValue().asString() );
                }
                if( CollectionType.class.isAssignableFrom( valueType.getClass() ) )
                {
                    return (T) deserializeCollection( module, (CollectionType) valueType, value.asArrayValue() );
                }
                if( MapType.class.isAssignableFrom( valueType.getClass() ) )
                {
                    return (T) deserializeMap( module, (MapType) valueType, value.asMapValue() );
                }
                if( ValueCompositeType.class.isAssignableFrom( valueType.getClass() ) )
                {
                    return (T) deserializeValueComposite( module, (ValueCompositeType) valueType, value.asMapValue() );
                }
                return (T) doGuessDeserialize( module, valueType, value );
            }
            catch( IOException | ClassNotFoundException ex )
            {
                throw new SerializationException( "Unable to deserialize " + valueType + " from: " + value );
            }
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

        private Object deserializeValueComposite( ModuleDescriptor module, ValueCompositeType valueType,
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
                ValueDescriptor descriptor = module.valueDescriptor( typeInfo );
                if( descriptor == null )
                {
                    throw new SerializationException(
                        "_type: " + typeInfo + " could not be resolved while deserializing " + value );
                }
                valueType = descriptor.valueType();
            }

            ValueBuilder builder = module.instance().newValueBuilderWithState(
                valueType.primaryType(),
                propertyFunction( module, namedValues ),
                associationFunction( module, namedValues ),
                manyAssociationFunction( module, namedValues ),
                namedAssociationFunction( module, namedValues ) );
            return builder.newInstance();
        }

        private Function<PropertyDescriptor, Object> propertyFunction( ModuleDescriptor module,
                                                                       Map<String, Value> namedValues )
        {
            return property ->
            {
                Value value = namedValues.get( property.qualifiedName().name() );
                if( value != null )
                {
                    Object propertyValue = doDeserialize( module, property.valueType(), value );
                    if( property.isImmutable() )
                    {
                        if( propertyValue instanceof Set )
                        {
                            return unmodifiableSet( (Set<?>) propertyValue );
                        }
                        else if( propertyValue instanceof List )
                        {
                            return unmodifiableList( (List<?>) propertyValue );
                        }
                        else if( propertyValue instanceof Map )
                        {
                            return unmodifiableMap( (Map<?, ?>) propertyValue );
                        }
                    }
                    return propertyValue;
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
                case BINARY:
                    return deserializeJava( value.asBinaryValue() );
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
                        ValueDescriptor valueDescriptor = module.valueDescriptor( typeInfo.get() );
                        if( valueDescriptor != null )
                        {
                            return deserializeValueComposite( module, valueDescriptor.valueType(), mapValue );
                        }
                    }
                default:
                    throw new SerializationException( "Don't know how to deserialize " + valueType + " from " + value
                                                      + " (" + value.getValueType() + ")" );
            }
        }

        private Object deserializeJava( BinaryValue value )
            throws IOException, ClassNotFoundException
        {
            byte[] bytes = value.asByteArray();
            try( ObjectInputStream oin = new ObjectInputStream( new ByteArrayInputStream( bytes ) ) )
            {
                return oin.readObject();
            }
        }
    }
}
