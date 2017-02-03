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
package org.apache.polygene.serialization.javaxxml;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.polygene.api.type.ValueType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Adapter for XML (de)serialization.
 *
 * @param <T> the adapted type
 */
public interface JavaxXmlAdapter<T>
{
    /**
     * @return the adapted type
     */
    Class<T> type();

    /**
     * Serialize.
     *
     * @param document the Document to use as a Node factory
     * @param object Object to serialize, never null
     * @param serializationFunction Serialization function for nested structure serialization
     * @return Serialized XML representation
     */
    Node serialize( Document document, Object object, Function<Object, Node> serializationFunction );

    /**
     * Deserialize.
     *
     * @param node XML to deserialize from, never null
     * @param deserializationFunction Deserialization function for nested structure deserialization
     * @return Deserialized object
     */
    T deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction );
}
