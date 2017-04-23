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

import java.util.Arrays;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParserFactory;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Initializable;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceDescriptor;

@Mixins( JavaxJsonFactories.Mixin.class )
public interface JavaxJsonFactories
{
    JsonParserFactory parserFactory();

    JsonReaderFactory readerFactory();

    JsonGeneratorFactory generatorFactory();

    JsonBuilderFactory builderFactory();

    JsonWriterFactory writerFactory();

    /**
     * Creates a {@link JsonString} with the {@link Object#toString()} result on the given object.
     *
     * @param object the object
     * @return the JsonString
     */
    JsonString toJsonString( Object object );

    /**
     * Creates a {@link JsonObjectBuilder} populated with the state of a {@link JsonObject}.
     *
     * @param jsonObject the JsonObject
     * @return the builder
     */
    JsonObjectBuilder cloneBuilder( JsonObject jsonObject );

    /**
     * Creates a {@link JsonObjectBuilder} populated with the state of a {@link JsonObject}, including only some keys.
     *
     * @param jsonObject the JsonObject
     * @param keys the keys to include
     * @return the builder
     */
    JsonObjectBuilder cloneBuilderInclude( JsonObject jsonObject, String... keys );

    /**
     * Creates a {@link JsonObjectBuilder} populated with the state of a {@link JsonObject}, excluding some keys.
     *
     * @param jsonObject the JsonObject
     * @param keys the keys to exclude
     * @return the builder
     */
    JsonObjectBuilder cloneBuilderExclude( JsonObject jsonObject, String... keys );

    /**
     * Creates a {@link JsonArrayBuilder} populated with the state of a {@link JsonArray}.
     *
     * @param jsonArray the JsonArray
     * @return the builder
     */
    JsonArrayBuilder cloneBuilder( JsonArray jsonArray );

    /**
     * Creates a {@link JsonArrayBuilder} populated with the state of a {@link JsonArray}, excluding some values.
     *
     * @param jsonArray the JsonArray
     * @param values the values to exclude
     * @return the builder
     */
    JsonArrayBuilder cloneBuilderExclude( JsonArray jsonArray, JsonValue... values );

    class Mixin implements JavaxJsonFactories, Initializable
    {
        @Uses
        private ServiceDescriptor descriptor;

        private JsonParserFactory parserFactory;
        private JsonReaderFactory readerFactory;
        private JsonGeneratorFactory generatorFactory;
        private JsonBuilderFactory builderFactory;
        private JsonWriterFactory writerFactory;

        @Override
        public void initialize() throws Exception
        {
            JavaxJsonSettings settings = JavaxJsonSettings.orDefault( descriptor.metaInfo( JavaxJsonSettings.class ) );

            String jsonProviderClassName = settings.getJsonProviderClassName();
            JsonProvider jsonProvider;
            if( jsonProviderClassName == null )
            {
                jsonProvider = JsonProvider.provider();
            }
            else
            {
                try
                {
                    Class<?> clazz = Class.forName( jsonProviderClassName );
                    jsonProvider = (JsonProvider) clazz.newInstance();
                }
                catch( ClassNotFoundException ex )
                {
                    throw new JsonException( "Provider " + jsonProviderClassName + " not found", ex );
                }
                catch( Exception ex )
                {
                    throw new JsonException( "Provider " + jsonProviderClassName + " could not be instantiated", ex );
                }
            }

            parserFactory = jsonProvider.createParserFactory( settings.getJsonParserProperties() );
            readerFactory = jsonProvider.createReaderFactory( settings.getJsonParserProperties() );

            generatorFactory = jsonProvider.createGeneratorFactory( settings.getJsonGeneratorProperties() );
            builderFactory = jsonProvider.createBuilderFactory( settings.getJsonGeneratorProperties() );
            writerFactory = jsonProvider.createWriterFactory( settings.getJsonGeneratorProperties() );
        }

        @Override
        public JsonParserFactory parserFactory()
        {
            return parserFactory;
        }

        @Override
        public JsonReaderFactory readerFactory()
        {
            return readerFactory;
        }

        @Override
        public JsonGeneratorFactory generatorFactory()
        {
            return generatorFactory;
        }

        @Override
        public JsonBuilderFactory builderFactory()
        {
            return builderFactory;
        }

        @Override
        public JsonWriterFactory writerFactory()
        {
            return writerFactory;
        }

        @Override
        public JsonString toJsonString( Object object )
        {
            return builderFactory.createObjectBuilder().add( "value", object.toString() ).build()
                                 .getJsonString( "value" );
        }

        @Override
        public JsonObjectBuilder cloneBuilder( JsonObject jsonObject )
        {
            JsonObjectBuilder builder = builderFactory.createObjectBuilder();
            for( String key : jsonObject.keySet() )
            {
                builder.add( key, jsonObject.get( key ) );
            }
            return builder;
        }

        @Override
        public JsonObjectBuilder cloneBuilderInclude( JsonObject jsonObject, String... keys )
        {
            List<String> includes = Arrays.asList( keys );
            JsonObjectBuilder builder = builderFactory.createObjectBuilder();
            for( String include : includes )
            {
                if( jsonObject.containsKey( include ) )
                {
                    builder.add( include, jsonObject.get( include ) );
                }
            }
            return builder;
        }

        @Override
        public JsonObjectBuilder cloneBuilderExclude( JsonObject jsonObject, String... keys )
        {
            List<String> excludes = Arrays.asList( keys );
            JsonObjectBuilder builder = builderFactory.createObjectBuilder();
            for( String key : jsonObject.keySet() )
            {
                if( !excludes.contains( key ) )
                {
                    builder.add( key, jsonObject.get( key ) );
                }
            }
            return builder;
        }

        @Override
        public JsonArrayBuilder cloneBuilder( JsonArray jsonArray )
        {
            JsonArrayBuilder builder = builderFactory.createArrayBuilder();
            for( JsonValue entry : jsonArray )
            {
                builder.add( entry );
            }
            return builder;
        }

        @Override
        public JsonArrayBuilder cloneBuilderExclude( JsonArray jsonArray, JsonValue... values )
        {
            List<JsonValue> excludes = Arrays.asList( values );
            JsonArrayBuilder job = builderFactory.createArrayBuilder();
            for( JsonValue entry : jsonArray )
            {
                if( !excludes.contains( entry ) )
                {
                    job.add( entry );
                }
            }
            return job;
        }
    }
}
