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
package org.apache.polygene.api.serialization;

import java.io.InputStream;
import java.io.Reader;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ValueType;

/**
 * Deserializer.
 *
 * Provides methods and functions to deserialize objects and set of objects.
 */
public interface Deserializer
{
    <T> T deserialize( ModuleDescriptor module, ValueType valueType, InputStream state );

    <T> T deserialize( ModuleDescriptor module, ValueType valueType, Reader state );

    <T> T deserialize( ModuleDescriptor module, ValueType valueType, String state );

    <T> Function<String, T> deserializeFunction( ModuleDescriptor module, ValueType valueType );

    <T> Stream<T> deserializeEach( ModuleDescriptor module, ValueType valueType, Iterable<String> states );

    <T> Stream<T> deserializeEach( ModuleDescriptor module, ValueType valueType, String... states );

    <T> T fromBytes( ModuleDescriptor module, ValueType valueType, byte[] bytes );

    <T> Function<byte[], T> fromBytesFunction( ModuleDescriptor module, ValueType valueType );

    <T> Stream<T> fromBytesEach( ModuleDescriptor module, ValueType valueType, Iterable<byte[]> states );

    <T> Stream<T> fromBytesEach( ModuleDescriptor module, ValueType valueType, byte[]... states );

    <T> T deserialize( ModuleDescriptor module, Class<T> type, InputStream state );

    <T> T deserialize( ModuleDescriptor module, Class<T> type, Reader state );

    <T> T deserialize( ModuleDescriptor module, Class<T> type, String state );

    <T> Function<String, T> deserializeFunction( ModuleDescriptor module, Class<T> type );

    <T> Stream<T> deserializeEach( ModuleDescriptor module, Class<T> type, Iterable<String> states );

    <T> Stream<T> deserializeEach( ModuleDescriptor module, Class<T> type, String... states );

    <T> T fromBytes( ModuleDescriptor module, Class<T> type, byte[] bytes );

    <T> Function<byte[], T> fromBytesFunction( ModuleDescriptor module, Class<T> type );

    <T> Stream<T> fromBytesEach( ModuleDescriptor module, Class<T> type, Iterable<byte[]> states );

    <T> Stream<T> fromBytesEach( ModuleDescriptor module, Class<T> type, byte[]... states );
}
