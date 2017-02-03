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

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.polygene.api.type.ValueType;
import org.msgpack.value.Value;

/**
 * Adapter for MessagePack (de)serialization.
 *
 * @param <T> the adapted type
 */
public interface MessagePackAdapter<T>
{
    /**
     * @return the adapted type
     */
    Class<T> type();

    /**
     * Serialize.
     *
     * @param object Object to serialize, never null
     * @param serializeFunction Serialization function for nested structure serialization
     * @return MessagePack Value
     */
    Value serialize( Object object, Function<Object, Value> serializeFunction )
        throws IOException;

    /**
     * Deserialize.
     *
     * @param value MessagePack value
     * @param deserializeFunction Deserialization function for nested structure deserialization
     * @return Deserialized object
     */
    T deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
        throws IOException;
}
