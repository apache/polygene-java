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
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;

@Mixins( MessagePackSerializationService.Activation.class )
public interface MessagePackSerializationService extends MessagePackSerialization, ServiceActivation
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
        private MessagePackAdapters adapters;

        private boolean registrationDone = false;

        @Override
        public void activateService()
        {
            if( !registrationDone )
            {
                applySettings();
                registerBuiltInConverters();
                registerBaseMessagePackAdapters();
                registrationDone = true;
            }
        }

        @Override
        public void passivateService() {}

        private void applySettings()
        {
            MessagePackSettings settings
                = MessagePackSettings.orDefault( descriptor.metaInfo( MessagePackSettings.class ) );
            settings.getConverters()
                    .forEach( ( type, converter ) -> converters.registerConverter( type, converter ) );
            settings.getAdapters()
                    .forEach( ( type, adapter ) -> adapters.registerAdapter( type, adapter ) );
        }

        private void registerBuiltInConverters()
        {
            builtInConverters.registerBuiltInConverters( converters );
        }

        private void registerBaseMessagePackAdapters()
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

        private static abstract class ToStringAdapter<T> implements MessagePackAdapter<T>
        {
            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newString( object.toString() );
            }
        }

        private static class StringAdapter extends ToStringAdapter<String>
        {
            @Override
            public Class<String> type() { return String.class; }

            @Override
            public String deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asStringValue().asString();
            }
        }

        private static class CharacterAdapter extends ToStringAdapter<Character>
        {
            @Override
            public Class<Character> type() { return Character.class; }

            @Override
            public Character deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                String string = value.asStringValue().asString();
                return string.isEmpty() ? null : string.charAt( 0 );
            }
        }

        private static class BooleanAdapter implements MessagePackAdapter<Boolean>
        {
            @Override
            public Class<Boolean> type() { return Boolean.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newBoolean( (Boolean) object );
            }

            @Override
            public Boolean deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asBooleanValue().getBoolean();
            }
        }

        private static class IntegerAdapter implements MessagePackAdapter<Integer>
        {
            @Override
            public Class<Integer> type() { return Integer.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newInteger( (Integer) object );
            }

            @Override
            public Integer deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asIntegerValue().asInt();
            }
        }

        private static class LongAdapter implements MessagePackAdapter<Long>
        {
            @Override
            public Class<Long> type() { return Long.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newInteger( (Long) object );
            }

            @Override
            public Long deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asIntegerValue().asLong();
            }
        }

        private static class ShortAdapter implements MessagePackAdapter<Short>
        {
            @Override
            public Class<Short> type() { return Short.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newInteger( (Short) object );
            }

            @Override
            public Short deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asIntegerValue().asShort();
            }
        }

        private static class ByteAdapter implements MessagePackAdapter<Byte>
        {
            @Override
            public Class<Byte> type() { return Byte.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newInteger( (Byte) object );
            }

            @Override
            public Byte deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asIntegerValue().asByte();
            }
        }

        private static class FloatAdapter implements MessagePackAdapter<Float>
        {
            @Override
            public Class<Float> type() { return Float.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newFloat( (Float) object );
            }

            @Override
            public Float deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asFloatValue().toFloat();
            }
        }

        private static class DoubleAdapter implements MessagePackAdapter<Double>
        {
            @Override
            public Class<Double> type() { return Double.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newFloat( (Double) object );
            }

            @Override
            public Double deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asFloatValue().toDouble();
            }
        }
    }
}
