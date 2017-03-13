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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Initializable;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.serialization.Converters;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.spi.serialization.BuiltInConverters;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.apache.polygene.api.type.HasTypesCollectors.closestType;
import static org.apache.polygene.serialization.javaxxml.JavaxXmlSettings.orDefault;

@Mixins( JavaxXmlAdapters.Mixin.class )
public interface JavaxXmlAdapters
{
    void registerAdapter( ValueType valueType, JavaxXmlAdapter<?> adapter );

    <T> JavaxXmlAdapter<T> adapterFor( ValueType valueType );

    default <T> JavaxXmlAdapter<T> adapterFor( Class<T> type )
    {
        return adapterFor( ValueType.of( type ) );
    }

    class Mixin implements JavaxXmlAdapters, Initializable
    {
        private Map<ValueType, JavaxXmlAdapter<?>> adapters = new LinkedHashMap<>();

        @Uses
        private ServiceDescriptor descriptor;

        @This
        private BuiltInConverters builtInConverters;

        @This
        private Converters converters;

        @Override
        public void initialize() throws Exception
        {
            JavaxXmlSettings settings = orDefault( descriptor.metaInfo( JavaxXmlSettings.class ) );
            settings.getConverters()
                    .forEach( ( type, converter ) -> converters.registerConverter( type, converter ) );
            builtInConverters.registerBuiltInConverters( converters );
            settings.getAdapters().forEach( adapters::put );
            registerBaseJavaxXmlAdapters();
        }

        @Override
        public void registerAdapter( final ValueType valueType, final JavaxXmlAdapter<?> adapter )
        {
            adapters.put( valueType, adapter );
        }

        @Override
        public <T> JavaxXmlAdapter<T> adapterFor( final ValueType valueType )
        {
            return castAdapter( adapters.keySet().stream()
                                        .collect( closestType( valueType ) )
                                        .map( adapters::get )
                                        .orElse( null ) );
        }

        @SuppressWarnings( "unchecked" )
        private <T> JavaxXmlAdapter<T> castAdapter( JavaxXmlAdapter<?> adapter )
        {
            return (JavaxXmlAdapter<T>) adapter;
        }

        private void registerBaseJavaxXmlAdapters()
        {
            // Primitive Value types
            adapters.put( ValueType.STRING, new StringAdapter() );
            adapters.put( ValueType.CHARACTER, new CharacterAdapter() );
            adapters.put( ValueType.BOOLEAN, new BooleanAdapter() );
            adapters.put( ValueType.INTEGER, new IntegerAdapter() );
            adapters.put( ValueType.LONG, new LongAdapter() );
            adapters.put( ValueType.SHORT, new ShortAdapter() );
            adapters.put( ValueType.BYTE, new ByteAdapter() );
            adapters.put( ValueType.FLOAT, new FloatAdapter() );
            adapters.put( ValueType.DOUBLE, new DoubleAdapter() );
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
