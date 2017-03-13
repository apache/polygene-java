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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
import org.apache.polygene.api.serialization.Deserializer;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.serialization.javaxjson.JavaxJson;
import org.apache.polygene.spi.module.ModuleSpi;

import static java.util.stream.Collectors.joining;

public interface JsonDeserializer extends Deserializer
{
    <T> T fromJson( ModuleDescriptor module, ValueType valueType, JsonValue state );

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

    default <T> T fromJson( ModuleDescriptor module, Class<T> type, JsonValue state )
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

    @Override
    default <T> T deserialize( ModuleDescriptor module, ValueType valueType, Reader state )
    {
        // JSR-353 Does not allow reading "out of structure" values
        // See https://www.jcp.org/en/jsr/detail?id=353
        // And commented JsonReader#readValue() method in the javax.json API
        // BUT, it will be part of the JsonReader contract in the next version
        // See https://www.jcp.org/en/jsr/detail?id=374
        // Implementation by provider is optional though, so we'll always need a default implementation here.
        // Fortunately, JsonParser has new methods allowing to read structures while parsing so it will be easy to do.
        // In the meantime, a poor man's implementation reading the json into memory will do.
        // TODO Revisit values out of structure JSON deserialization when JSR-374 is out
        String stateString;
        try( BufferedReader buffer = new BufferedReader( state ) )
        {
            stateString = buffer.lines().collect( joining( "\n" ) );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
        // We want plain Strings, BigDecimals, BigIntegers to be deserialized even when unquoted
        Function<String, T> plainValueFunction = string ->
        {
            String poorMans = "{\"value\":" + string + "}";
            JsonObject poorMansJson = Json.createReader( new StringReader( poorMans ) ).readObject();
            JsonValue value = poorMansJson.get( "value" );
            return fromJson( module, valueType, value );
        };
        Function<String, T> outOfStructureFunction = string ->
        {
            // Is this an unquoted plain value?
            try
            {
                return plainValueFunction.apply( '"' + string + '"' );
            }
            catch( JsonParsingException ex )
            {
                return plainValueFunction.apply( string );
            }
        };
        try( JsonParser parser = Json.createParser( new StringReader( stateString ) ) )
        {
            if( parser.hasNext() )
            {
                JsonParser.Event e = parser.next();
                switch( e )
                {
                    case VALUE_NULL:
                        return null;
                    case START_ARRAY:
                    case START_OBJECT:
                        // JSON Structure
                        try( JsonReader reader = Json.createReader( new StringReader( stateString ) ) )
                        {
                            return fromJson( module, valueType, reader.read() );
                        }
                    default:
                        // JSON Value out of structure
                        return outOfStructureFunction.apply( stateString );
                }
            }
        }
        catch( JsonParsingException ex )
        {
            return outOfStructureFunction.apply( stateString );
        }
        // Empty state string?
        return fromJson( module, valueType, JavaxJson.EMPTY_STRING );
    }
}
