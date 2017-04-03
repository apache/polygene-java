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

import java.util.Collections;
import java.util.Map;
import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.JsonReaderFactory;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
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

            Map<String, ?> parserProperties = Collections.singletonMap( "org.apache.johnzon.supports-comments", true );
            parserFactory = jsonProvider.createParserFactory( parserProperties );
            readerFactory = jsonProvider.createReaderFactory( parserProperties );

            Map<String, ?> generatorProperties = Collections.singletonMap( JsonGenerator.PRETTY_PRINTING, false );
            generatorFactory = jsonProvider.createGeneratorFactory( generatorProperties );
            builderFactory = jsonProvider.createBuilderFactory( generatorProperties );
            writerFactory = jsonProvider.createWriterFactory( generatorProperties );
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
    }
}
