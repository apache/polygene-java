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
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.serialization.Deserializer;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.spi.module.ModuleSpi;
import org.w3c.dom.Node;

/**
 * {@literal javax.xml} deserializer.
 */
public interface XmlDeserializer extends Deserializer
{
    <T> T fromXml( ModuleDescriptor module, ValueType valueType, @Optional Node state );

    default <T> Function<Node, T> fromXmlFunction( ModuleDescriptor module, ValueType valueType )
    {
        return state -> fromXml( module, valueType, state );
    }

    default <T> Stream<T> fromXmlEach( ModuleDescriptor module, ValueType valueType, Stream<Node> states )
    {
        return states.map( fromXmlFunction( module, valueType ) );
    }

    default <T> Stream<T> fromXmlEach( ModuleDescriptor module, ValueType valueType, Iterable<Node> states )
    {
        return fromXmlEach( module, valueType, StreamSupport.stream( states.spliterator(), false ) );
    }

    default <T> Stream<T> fromXmlEach( ModuleDescriptor module, ValueType valueType, Node... states )
    {
        return fromXmlEach( module, valueType, Stream.of( states ) );
    }

    default <T> T fromXml( ModuleDescriptor module, Class<T> type, @Optional Node state )
    {
        // TODO Remove (ModuleSpi) cast
        ValueType valueType = ( (ModuleSpi) module.instance() ).valueTypeFactory().valueTypeOf( module, type );
        return fromXml( module, valueType, state );
    }

    default <T> Function<Node, T> fromXml( ModuleDescriptor module, Class<T> type )
    {
        return state -> fromXml( module, type, state );
    }

    default <T> Stream<T> fromXmlEach( ModuleDescriptor module, Class<T> valueType, Stream<Node> states )
    {
        return states.map( fromXml( module, valueType ) );
    }

    default <T> Stream<T> fromXmlEach( ModuleDescriptor module, Class<T> valueType, Iterable<Node> states )
    {
        return fromXmlEach( module, valueType, StreamSupport.stream( states.spliterator(), false ) );
    }

    default <T> Stream<T> fromXmlEach( ModuleDescriptor module, Class<T> valueType, Node... states )
    {
        return fromXmlEach( module, valueType, Stream.of( states ) );
    }
}
