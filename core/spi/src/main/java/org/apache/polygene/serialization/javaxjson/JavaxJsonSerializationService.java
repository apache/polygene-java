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
import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.type.ValueType;

// TODO Move into JavaxJsonSerialization
// TODO Do the same on XML & MessagePack
@Mixins( JavaxJsonSerializationService.Activation.class )
public interface JavaxJsonSerializationService extends JavaxJsonSerialization, ServiceActivation
{
    class Activation implements ServiceActivation
    {
        @Uses
        private ServiceDescriptor descriptor;

        @This
        private JavaxJsonAdapters adapters;

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
            JavaxJsonSettings.orDefault( descriptor.metaInfo( JavaxJsonSettings.class ) )
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

        private static abstract class ToJsonStringAdapter<T> implements JavaxJsonAdapter<T>
        {
            @Override
            public JsonValue serialize( Object object, Function<Object, JsonValue> serializeFunction )
            {
                return JavaxJson.toJsonString( object );
            }
        }

        private static class StringAdapter extends ToJsonStringAdapter<String>
        {
            @Override
            public Class<String> type() { return String.class; }

            @Override
            public String deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                return JavaxJson.asString( json );
            }
        }

        private static class CharacterAdapter extends ToJsonStringAdapter<Character>
        {
            @Override
            public Class<Character> type() { return Character.class; }

            @Override
            public Character deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                String string = JavaxJson.asString( json );
                return string.isEmpty() ? null : string.charAt( 0 );
            }
        }

        private static class BooleanAdapter implements JavaxJsonAdapter<Boolean>
        {
            @Override
            public Class<Boolean> type() { return Boolean.class; }

            @Override
            public JsonValue serialize( Object object, Function<Object, JsonValue> serializeFunction )
            {
                return type().cast( object ) ? JsonValue.TRUE : JsonValue.FALSE;
            }

            @Override
            public Boolean deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                switch( json.getValueType() )
                {
                    case TRUE:
                        return true;
                    case FALSE:
                        return false;
                    case NULL:
                        return null;
                    case NUMBER:
                        return ( (JsonNumber) json ).doubleValue() > 0;
                    case STRING:
                        return Boolean.valueOf( ( (JsonString) json ).getString() );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Boolean from " + json );
                }
            }
        }

        private static class IntegerAdapter implements JavaxJsonAdapter<Integer>
        {
            @Override
            public Class<Integer> type() { return Integer.class; }

            @Override
            public JsonValue serialize( Object object, Function<Object, JsonValue> serializeFunction )
            {
                return Json.createObjectBuilder().add( "value", type().cast( object ) ).build()
                           .getJsonNumber( "value" );
            }

            @Override
            public Integer deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return ( (JsonNumber) json ).intValueExact();
                    case STRING:
                        String string = ( (JsonString) json ).getString();
                        return string.isEmpty() ? 0 : Integer.parseInt( string );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Integer from " + json );
                }
            }
        }

        private static class LongAdapter implements JavaxJsonAdapter<Long>
        {
            @Override
            public Class<Long> type() { return Long.class; }

            @Override
            public JsonValue serialize( Object object, Function<Object, JsonValue> serializeFunction )
            {
                return Json.createObjectBuilder().add( "value", type().cast( object ) ).build().getJsonNumber(
                    "value" );
            }

            @Override
            public Long deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return ( (JsonNumber) json ).longValueExact();
                    case STRING:
                        String string = ( (JsonString) json ).getString();
                        return string.isEmpty() ? 0L : Long.parseLong( string );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Long from " + json );
                }
            }
        }

        private static class ShortAdapter implements JavaxJsonAdapter<Short>
        {
            @Override
            public Class<Short> type() { return Short.class; }

            @Override
            public JsonValue serialize( Object object, Function<Object, JsonValue> serializeFunction )
            {
                return Json.createObjectBuilder().add( "value", type().cast( object ) ).build()
                           .getJsonNumber( "value" );
            }

            @Override
            public Short deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return (short) ( (JsonNumber) json ).intValueExact();
                    case STRING:
                        String string = ( (JsonString) json ).getString();
                        return string.isEmpty() ? 0 : Short.parseShort( string );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Short from " + json );
                }
            }
        }

        private static class ByteAdapter implements JavaxJsonAdapter<Byte>
        {
            @Override
            public Class<Byte> type() { return Byte.class; }

            @Override
            public JsonValue serialize( Object object, Function<Object, JsonValue> serializeFunction )
            {
                return Json.createObjectBuilder().add( "value", type().cast( object ) ).build()
                           .getJsonNumber( "value" );
            }

            @Override
            public Byte deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return (byte) ( (JsonNumber) json ).intValueExact();
                    case STRING:
                        String string = ( (JsonString) json ).getString();
                        return string.isEmpty() ? 0 : Byte.parseByte( string );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Byte from " + json );
                }
            }
        }

        private static class FloatAdapter implements JavaxJsonAdapter<Float>
        {
            @Override
            public Class<Float> type() { return Float.class; }

            @Override
            public JsonValue serialize( Object object, Function<Object, JsonValue> serializeFunction )
            {
                return Json.createObjectBuilder().add( "value", type().cast( object ) ).build()
                           .getJsonNumber( "value" );
            }

            @Override
            public Float deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return (float) ( (JsonNumber) json ).doubleValue();
                    case STRING:
                        String string = ( (JsonString) json ).getString();
                        return string.isEmpty() ? 0F : Float.parseFloat( string );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Float from " + json );
                }
            }
        }

        private static class DoubleAdapter implements JavaxJsonAdapter<Double>
        {
            @Override
            public Class<Double> type() { return Double.class; }

            @Override
            public JsonValue serialize( Object object, Function<Object, JsonValue> serializeFunction )
            {
                return Json.createObjectBuilder().add( "value", type().cast( object ) ).build()
                           .getJsonNumber( "value" );
            }

            @Override
            public Double deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return ( (JsonNumber) json ).doubleValue();
                    case STRING:
                        String string = ( (JsonString) json ).getString();
                        return string.isEmpty() ? 0D : Double.parseDouble( string );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Double from " + json );
                }
            }
        }

        private static class BigDecimalAdapter extends ToJsonStringAdapter<BigDecimal>
        {
            @Override
            public Class<BigDecimal> type() { return BigDecimal.class; }

            @Override
            public BigDecimal deserialize( JsonValue json,
                                           BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return new BigDecimal( json.toString() );
                    case STRING:
                        return new BigDecimal( ( (JsonString) json ).getString() );
                    default:
                        throw new SerializationException(
                            "Don't know how to deserialize BigDecimal from " + json );
                }
            }
        }

        private static class BigIntegerAdapter extends ToJsonStringAdapter<BigInteger>
        {
            @Override
            public Class<BigInteger> type() { return BigInteger.class; }

            @Override
            public BigInteger deserialize( JsonValue json,
                                           BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return new BigInteger( json.toString() );
                    case STRING:
                        return new BigInteger( ( (JsonString) json ).getString() );
                    default:
                        throw new SerializationException(
                            "Don't know how to deserialize BigInteger from " + json );
                }
            }
        }

        private static class PeriodAdapter extends ToJsonStringAdapter<Period>
        {
            @Override
            public Class<Period> type() { return Period.class; }

            @Override
            public Period deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                return Period.parse( JavaxJson.asString( json ) );
            }
        }

        private static class DurationAdapter extends ToJsonStringAdapter<Duration>
        {
            @Override
            public Class<Duration> type() { return Duration.class; }

            @Override
            public Duration deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                return Duration.parse( JavaxJson.asString( json ) );
            }
        }

        private static class LocalTimeAdapter extends ToJsonStringAdapter<LocalTime>
        {
            @Override
            public Class<LocalTime> type() { return LocalTime.class; }

            @Override
            public LocalTime deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                return LocalTime.parse( JavaxJson.asString( json ) );
            }
        }

        private static class LocalDateAdapter extends ToJsonStringAdapter<LocalDate>
        {
            @Override
            public Class<LocalDate> type() { return LocalDate.class; }

            @Override
            public LocalDate deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                return LocalDate.parse( JavaxJson.asString( json ) );
            }
        }

        private static class LocalDateTimeAdapter extends ToJsonStringAdapter<LocalDateTime>
        {
            @Override
            public Class<LocalDateTime> type() { return LocalDateTime.class; }

            @Override
            public LocalDateTime deserialize( JsonValue json,
                                              BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                return LocalDateTime.parse( JavaxJson.asString( json ) );
            }
        }

        private static class OffsetDateTimeAdapter extends ToJsonStringAdapter<OffsetDateTime>
        {
            @Override
            public Class<OffsetDateTime> type() { return OffsetDateTime.class; }

            @Override
            public OffsetDateTime deserialize( JsonValue json,
                                               BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                return OffsetDateTime.parse( JavaxJson.asString( json ) );
            }
        }

        private static class ZonedDateTimeAdapter extends ToJsonStringAdapter<ZonedDateTime>
        {
            @Override
            public Class<ZonedDateTime> type() { return ZonedDateTime.class; }

            @Override
            public ZonedDateTime deserialize( JsonValue json,
                                              BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                return ZonedDateTime.parse( JavaxJson.asString( json ) );
            }
        }

        private static class InstantAdapter extends ToJsonStringAdapter<Instant>
        {
            @Override
            public Class<Instant> type() { return Instant.class; }

            @Override
            public Instant deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                return Instant.parse( JavaxJson.asString( json ) );
            }
        }

        private static class IdentityAdapter extends ToJsonStringAdapter<Identity>
        {
            @Override
            public Class<Identity> type() { return Identity.class; }

            @Override
            public Identity deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                return StringIdentity.fromString( JavaxJson.asString( json ) );
            }
        }

        private static class EntityReferenceAdapter extends ToJsonStringAdapter<EntityReference>
        {
            @Override
            public Class<EntityReference> type() { return EntityReference.class; }

            @Override
            public EntityReference deserialize( JsonValue json,
                                                BiFunction<JsonValue, ValueType, Object> deserializeFunction )
            {
                return EntityReference.parseEntityReference( JavaxJson.asString( json ) );
            }
        }
    }
}
