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

import java.util.function.BiFunction;
import java.util.function.Function;
import javax.json.JsonBuilderFactory;
import javax.json.JsonValue;
import org.apache.polygene.api.type.ValueType;

/**
 * Adapter for JSON (de)serialization.
 *
 * @param <T> the adapted type
 */
public interface JavaxJsonAdapter<T>
{
    /**
     * @return the adapted type
     */
    Class<T> type();

    /**
     * Serialize.
     *
     * @param builderFactory Factory to create JSON
     * @param object Object to serialize, never null
     * @param serialize Serialization function for nested structure serialization
     * @return Serialized JSON representation
     */
    JsonValue serialize( JsonBuilderFactory builderFactory, Object object, Function<Object, JsonValue> serialize );

    /**
     * Deserialize.
     *
     * @param json JSON to deserialize from, never null
     * @param deserialize Deserialization function for nested structure deserialization
     * @return Deserialized object
     */
    T deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserialize );
}
