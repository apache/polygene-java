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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.type.ValueType;
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
        private MessagePackAdapters adapters;

        private boolean registrationDone = false;

        @Override
        public void activateService()
        {
            if( !registrationDone )
            {
                registerCustomAdapters();
                registerBaseAdapters();
                registrationDone = true;
            }
        }

        @Override
        public void passivateService() {}

        private void registerCustomAdapters()
        {
            MessagePackSettings.orDefault( descriptor.metaInfo( MessagePackSettings.class ) )
                               .getAdapters()
                               .forEach( ( valueType, adapter ) -> adapters.registerAdapter( valueType, adapter ) );
        }

        private void registerBaseAdapters()
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

            // Number types
            adapters.registerAdapter( ValueType.BIG_DECIMAL, new BigDecimalAdapter() );
            adapters.registerAdapter( ValueType.BIG_INTEGER, new BigIntegerAdapter() );

            // Date types
            adapters.registerAdapter( ValueType.INSTANT, new InstantAdapter() );
            adapters.registerAdapter( ValueType.ZONED_DATE_TIME, new ZonedDateTimeAdapter() );
            adapters.registerAdapter( ValueType.OFFSET_DATE_TIME, new OffsetDateTimeAdapter() );
            adapters.registerAdapter( ValueType.LOCAL_DATE_TIME, new LocalDateTimeAdapter() );
            adapters.registerAdapter( ValueType.LOCAL_DATE, new LocalDateAdapter() );
            adapters.registerAdapter( ValueType.LOCAL_TIME, new LocalTimeAdapter() );
            adapters.registerAdapter( ValueType.DURATION, new DurationAdapter() );
            adapters.registerAdapter( ValueType.PERIOD, new PeriodAdapter() );

            // Other supported types
            adapters.registerAdapter( ValueType.IDENTITY, new IdentityAdapter() );
            adapters.registerAdapter( ValueType.ENTITY_REFERENCE, new EntityReferenceAdapter() );
        }

        private static abstract class ToStringAdapter<T> implements MessagePackAdapter<T>
        {
            @Override
            public Value serialize( Object object, Function<Object, Value> serializeFunction )
            {
                return ValueFactory.newString( object.toString() );
            }
        }

        private static class StringAdapter extends ToStringAdapter<String>
        {
            @Override
            public Class<String> type() { return String.class; }

            @Override
            public String deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return value.asStringValue().asString();
            }
        }

        private static class CharacterAdapter extends ToStringAdapter<Character>
        {
            @Override
            public Class<Character> type() { return Character.class; }

            @Override
            public Character deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
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
            public Value serialize( Object object, Function<Object, Value> serializeFunction )
            {
                return ValueFactory.newBoolean( (Boolean) object );
            }

            @Override
            public Boolean deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return value.asBooleanValue().getBoolean();
            }
        }

        private static class IntegerAdapter implements MessagePackAdapter<Integer>
        {
            @Override
            public Class<Integer> type() { return Integer.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serializeFunction )
            {
                return ValueFactory.newInteger( (Integer) object );
            }

            @Override
            public Integer deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return value.asIntegerValue().asInt();
            }
        }

        private static class LongAdapter implements MessagePackAdapter<Long>
        {
            @Override
            public Class<Long> type() { return Long.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serializeFunction )
            {
                return ValueFactory.newInteger( (Long) object );
            }

            @Override
            public Long deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return value.asIntegerValue().asLong();
            }
        }

        private static class ShortAdapter implements MessagePackAdapter<Short>
        {
            @Override
            public Class<Short> type() { return Short.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serializeFunction )
            {
                return ValueFactory.newInteger( (Short) object );
            }

            @Override
            public Short deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return value.asIntegerValue().asShort();
            }
        }

        private static class ByteAdapter implements MessagePackAdapter<Byte>
        {
            @Override
            public Class<Byte> type() { return Byte.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serializeFunction )
            {
                return ValueFactory.newInteger( (Byte) object );
            }

            @Override
            public Byte deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return value.asIntegerValue().asByte();
            }
        }

        private static class FloatAdapter implements MessagePackAdapter<Float>
        {
            @Override
            public Class<Float> type() { return Float.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serializeFunction )
            {
                return ValueFactory.newFloat( (Float) object );
            }

            @Override
            public Float deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return value.asFloatValue().toFloat();
            }
        }

        private static class DoubleAdapter implements MessagePackAdapter<Double>
        {
            @Override
            public Class<Double> type() { return Double.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serializeFunction )
            {
                return ValueFactory.newFloat( (Double) object );
            }

            @Override
            public Double deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return value.asFloatValue().toDouble();
            }
        }

        private static class BigDecimalAdapter extends ToStringAdapter<BigDecimal>
        {
            @Override
            public Class<BigDecimal> type() { return BigDecimal.class; }

            @Override
            public BigDecimal deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return new BigDecimal( value.asStringValue().asString() );
            }
        }

        private static class BigIntegerAdapter extends ToStringAdapter<BigInteger>
        {
            @Override
            public Class<BigInteger> type() { return BigInteger.class; }

            @Override
            public BigInteger deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return new BigInteger( value.asStringValue().asString() );
            }
        }

        private static class InstantAdapter extends ToStringAdapter<Instant>
        {
            @Override
            public Class<Instant> type() { return Instant.class; }

            @Override
            public Instant deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return Instant.parse( value.asStringValue().asString() );
            }
        }

        private static class ZonedDateTimeAdapter extends ToStringAdapter<ZonedDateTime>
        {
            @Override
            public Class<ZonedDateTime> type() { return ZonedDateTime.class; }

            @Override
            public ZonedDateTime deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return ZonedDateTime.parse( value.asStringValue().asString() );
            }
        }

        private static class OffsetDateTimeAdapter extends ToStringAdapter<OffsetDateTime>
        {
            @Override
            public Class<OffsetDateTime> type() { return OffsetDateTime.class; }

            @Override
            public OffsetDateTime deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return OffsetDateTime.parse( value.asStringValue().asString() );
            }
        }

        private static class LocalDateTimeAdapter extends ToStringAdapter<LocalDateTime>
        {
            @Override
            public Class<LocalDateTime> type() { return LocalDateTime.class; }

            @Override
            public LocalDateTime deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return LocalDateTime.parse( value.asStringValue().asString() );
            }
        }

        private static class LocalDateAdapter extends ToStringAdapter<LocalDate>
        {
            @Override
            public Class<LocalDate> type() { return LocalDate.class; }

            @Override
            public LocalDate deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return LocalDate.parse( value.asStringValue().asString() );
            }
        }

        private static class LocalTimeAdapter extends ToStringAdapter<LocalTime>
        {
            @Override
            public Class<LocalTime> type() { return LocalTime.class; }

            @Override
            public LocalTime deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return LocalTime.parse( value.asStringValue().asString() );
            }
        }

        private static class DurationAdapter extends ToStringAdapter<Duration>
        {
            @Override
            public Class<Duration> type() { return Duration.class; }

            @Override
            public Duration deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return Duration.parse( value.asStringValue().asString() );
            }
        }

        private static class PeriodAdapter extends ToStringAdapter<Period>
        {
            @Override
            public Class<Period> type() { return Period.class; }

            @Override
            public Period deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return Period.parse( value.asStringValue().asString() );
            }
        }

        private static class IdentityAdapter extends ToStringAdapter<Identity>
        {
            @Override
            public Class<Identity> type() { return Identity.class; }

            @Override
            public Identity deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return StringIdentity.fromString( value.asStringValue().asString() );
            }
        }

        private static class EntityReferenceAdapter extends ToStringAdapter<EntityReference>
        {
            @Override
            public Class<EntityReference> type() { return EntityReference.class; }

            @Override
            public EntityReference deserialize( Value value, BiFunction<Value, ValueType, Object> deserializeFunction )
            {
                return EntityReference.parseEntityReference( value.asStringValue().asString() );
            }
        }
    }
}
