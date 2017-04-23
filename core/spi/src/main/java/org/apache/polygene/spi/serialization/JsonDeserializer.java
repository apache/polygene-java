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
package org.apache.polygene.spi.serialization;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.json.JsonValue;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.serialization.Deserializer;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.spi.module.ModuleSpi;

/**
 * {@literal javax.json} deserializer.
 */
public interface JsonDeserializer extends Deserializer
{
    <T> T fromJson( ModuleDescriptor module, ValueType valueType, @Optional JsonValue state );

    default <T> Function<JsonValue, T> fromJsonFunction( ModuleDescriptor module, ValueType valueType )
    {
        return state -> fromJson( module, valueType, state );
    }

    default <T> Stream<T> fromJsonEach( ModuleDescriptor module, ValueType valueType, Stream<JsonValue> states )
    {
        return states.map( fromJsonFunction( module, valueType ) );
    }

    default <T> Stream<T> fromJsonEach( ModuleDescriptor module, ValueType valueType, Iterable<JsonValue> states )
    {
        return fromJsonEach( module, valueType, StreamSupport.stream( states.spliterator(), false ) );
    }

    default <T> Stream<T> fromJsonEach( ModuleDescriptor module, ValueType valueType, JsonValue... states )
    {
        return fromJsonEach( module, valueType, Stream.of( states ) );
    }

    default <T> T fromJson( ModuleDescriptor module, Class<T> type, @Optional JsonValue state )
    {
        // TODO Remove (ModuleSpi) cast
        ValueType valueType = ( (ModuleSpi) module.instance() ).valueTypeFactory().valueTypeOf( module, type );
        return fromJson( module, valueType, state );
    }

    default <T> Function<JsonValue, T> fromJson( ModuleDescriptor module, Class<T> type )
    {
        return state -> fromJson( module, type, state );
    }

    default <T> Stream<T> fromJsonEach( ModuleDescriptor module, Class<T> valueType, Stream<JsonValue> states )
    {
        return states.map( fromJson( module, valueType ) );
    }

    default <T> Stream<T> fromJsonEach( ModuleDescriptor module, Class<T> valueType, Iterable<JsonValue> states )
    {
        return fromJsonEach( module, valueType, StreamSupport.stream( states.spliterator(), false ) );
    }

    default <T> Stream<T> fromJsonEach( ModuleDescriptor module, Class<T> valueType, JsonValue... states )
    {
        return fromJsonEach( module, valueType, Stream.of( states ) );
    }
}
