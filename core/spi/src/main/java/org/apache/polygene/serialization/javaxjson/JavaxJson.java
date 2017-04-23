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

import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

/**
 * javax.json utilities.
 */
public class JavaxJson
{
    /**
     * Get a {@link String} out of a {@link JsonValue}.
     *
     * @param jsonValue the JSON value
     * @return the String
     */
    public static String asString( JsonValue jsonValue )
    {
        return jsonValue instanceof JsonString ? ( (JsonString) jsonValue ).getString() : jsonValue.toString();
    }

    /**
     * Require a {@link JsonValue} to be a {@link JsonStructure}.
     *
     * @param json the JSON value
     * @return the JSON structure
     * @throws JsonException if it is not
     */
    public static JsonStructure requireJsonStructure( JsonValue json )
    {
        if( json.getValueType() != JsonValue.ValueType.OBJECT && json.getValueType() != JsonValue.ValueType.ARRAY )
        {
            throw new JsonException( "Expected a JSON object or array but got " + json );
        }
        return (JsonStructure) json;
    }

    /**
     * Require a {@link JsonValue} to be a {@link JsonObject}.
     *
     * @param json the JSON value
     * @return the JSON object
     * @throws JsonException if it is not
     */
    public static JsonObject requireJsonObject( JsonValue json )
    {
        if( json.getValueType() != JsonValue.ValueType.OBJECT )
        {
            throw new JsonException( "Expected a JSON object but got " + json );
        }
        return (JsonObject) json;
    }

    /**
     * Require a {@link JsonValue} to be a {@link JsonArray}.
     *
     * @param json the JSON value
     * @return the JSON array
     * @throws JsonException if it is not
     */
    public static JsonArray requireJsonArray( JsonValue json )
    {
        if( json.getValueType() != JsonValue.ValueType.ARRAY )
        {
            throw new JsonException( "Expected a JSON array but got " + json );
        }
        return (JsonArray) json;
    }

    private JavaxJson() {}
}
