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
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.serialization.Converters;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.spi.serialization.BuiltInConverters;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@Mixins( JavaxXmlSerializationService.Activation.class )
public interface JavaxXmlSerializationService extends JavaxXmlSerialization, ServiceActivation
{
    class Activation implements ServiceActivation
    {
        @Uses
        private ServiceDescriptor descriptor;

        @This
        private BuiltInConverters builtInConverters;

        @This
        private Converters converters;

        @This
        private JavaxXmlAdapters adapters;

        private boolean registrationDone = false;

        @Override
        public void activateService()
        {
            if( !registrationDone )
            {
                applySettings();
                registerBuiltInConverters();
                registerBaseJavaxXmlAdapters();
                registrationDone = true;
            }
        }

        @Override
        public void passivateService() {}

        private void applySettings()
        {
            JavaxXmlSettings settings
                = JavaxXmlSettings.orDefault( descriptor.metaInfo( JavaxXmlSettings.class ) );
            settings.getConverters()
                    .forEach( ( type, converter ) -> converters.registerConverter( type, converter ) );
            settings.getAdapters()
                    .forEach( ( type, adapter ) -> adapters.registerAdapter( type, adapter ) );
        }

        private void registerBuiltInConverters()
        {
            builtInConverters.registerBuiltInConverters( converters );
        }

        private void registerBaseJavaxXmlAdapters()
        {
            // Primitive Value types
            adapters.registerAdapter( ValueType.STRING, new StringAdapter() );
            adapters.registerAdapter( ValueType.CHARACTER, new CharacterAdapter() );
            adapters.registerAdapter( ValueType.BOOLEAN, new BooleanAdapter() );
            adapters.registerAdapter( ValueType.INTEGER, new IntegerAdapter() );
            adapters.registerAdapter( ValueType.LONG, new LongAdapter() );
            adapters.registerAdapter( ValueType.SHORT, new ShortAdapter() );
            adapters.registerAdapter( ValueType.BYTE, new ByteAdapter() );
            adapters.registerAdapter( ValueType.FLOAT, new FloatAdapter() );
            adapters.registerAdapter( ValueType.DOUBLE, new DoubleAdapter() );
        }

        private static abstract class ToStringTextNodeAdapter<T> implements JavaxXmlAdapter<T>
        {
            @Override
            public Node serialize( Document document, Object object, Function<Object, Node> serialize )
            {
                return document.createTextNode( object.toString() );
            }
        }

        private static class StringAdapter extends ToStringTextNodeAdapter<String>
        {
            @Override
            public Class<String> type() { return String.class; }

            @Override
            public String deserialize( Node node, BiFunction<Node, ValueType, Object> deserialize )
            {
                return node.getNodeValue();
            }
        }

        private static class CharacterAdapter extends ToStringTextNodeAdapter<Character>
        {
            @Override
            public Class<Character> type() { return Character.class; }

            @Override
            public Character deserialize( Node node, BiFunction<Node, ValueType, Object> deserialize )
            {
                String string = node.getNodeValue();
                return string.isEmpty() ? null : string.charAt( 0 );
            }
        }

        private static class BooleanAdapter extends ToStringTextNodeAdapter<Boolean>
        {
            @Override
            public Class<Boolean> type() { return Boolean.class; }

            @Override
            public Boolean deserialize( Node node, BiFunction<Node, ValueType, Object> deserialize )
            {
                return Boolean.valueOf( node.getNodeValue() );
            }
        }

        private static class IntegerAdapter extends ToStringTextNodeAdapter<Integer>
        {
            @Override
            public Class<Integer> type() { return Integer.class; }

            @Override
            public Integer deserialize( Node node, BiFunction<Node, ValueType, Object> deserialize )
            {
                return Integer.valueOf( node.getNodeValue() );
            }
        }

        private static class LongAdapter extends ToStringTextNodeAdapter<Long>
        {
            @Override
            public Class<Long> type() { return Long.class; }

            @Override
            public Long deserialize( Node node, BiFunction<Node, ValueType, Object> deserialize )
            {
                return Long.valueOf( node.getNodeValue() );
            }
        }

        private static class ShortAdapter extends ToStringTextNodeAdapter<Short>
        {
            @Override
            public Class<Short> type() { return Short.class; }

            @Override
            public Short deserialize( Node node, BiFunction<Node, ValueType, Object> deserialize )
            {
                return Short.valueOf( node.getNodeValue() );
            }
        }

        private static class ByteAdapter extends ToStringTextNodeAdapter<Byte>
        {
            @Override
            public Class<Byte> type() { return Byte.class; }

            @Override
            public Byte deserialize( Node node, BiFunction<Node, ValueType, Object> deserialize )
            {
                return Byte.valueOf( node.getNodeValue() );
            }
        }

        private static class FloatAdapter extends ToStringTextNodeAdapter<Float>
        {
            @Override
            public Class<Float> type() { return Float.class; }

            @Override
            public Float deserialize( Node node, BiFunction<Node, ValueType, Object> deserialize )
            {
                return Float.valueOf( node.getNodeValue() );
            }
        }

        private static class DoubleAdapter extends ToStringTextNodeAdapter<Double>
        {
            @Override
            public Class<Double> type() { return Double.class; }

            @Override
            public Double deserialize( Node node, BiFunction<Node, ValueType, Object> deserialize )
            {
                return Double.valueOf( node.getNodeValue() );
            }
        }
    }
}
