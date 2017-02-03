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
        private JavaxXmlAdapters adapters;

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
            JavaxXmlSettings.orDefault( descriptor.metaInfo( JavaxXmlSettings.class ) )
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

        private static abstract class ToStringTextNodeAdapter<T> implements JavaxXmlAdapter<T>
        {
            @Override
            public Node serialize( Document document, Object object, Function<Object, Node> serializationFunction )
            {
                return document.createTextNode( object.toString() );
            }
        }

        private static class StringAdapter extends ToStringTextNodeAdapter<String>
        {
            @Override
            public Class<String> type() { return String.class; }

            @Override
            public String deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return node.getNodeValue();
            }
        }

        private static class CharacterAdapter extends ToStringTextNodeAdapter<Character>
        {
            @Override
            public Class<Character> type() { return Character.class; }

            @Override
            public Character deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
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
            public Boolean deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return Boolean.valueOf( node.getNodeValue() );
            }
        }

        private static class IntegerAdapter extends ToStringTextNodeAdapter<Integer>
        {
            @Override
            public Class<Integer> type() { return Integer.class; }

            @Override
            public Integer deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return Integer.valueOf( node.getNodeValue() );
            }
        }

        private static class LongAdapter extends ToStringTextNodeAdapter<Long>
        {
            @Override
            public Class<Long> type() { return Long.class; }

            @Override
            public Long deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return Long.valueOf( node.getNodeValue() );
            }
        }

        private static class ShortAdapter extends ToStringTextNodeAdapter<Short>
        {
            @Override
            public Class<Short> type() { return Short.class; }

            @Override
            public Short deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return Short.valueOf( node.getNodeValue() );
            }
        }

        private static class ByteAdapter extends ToStringTextNodeAdapter<Byte>
        {
            @Override
            public Class<Byte> type() { return Byte.class; }

            @Override
            public Byte deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return Byte.valueOf( node.getNodeValue() );
            }
        }

        private static class FloatAdapter extends ToStringTextNodeAdapter<Float>
        {
            @Override
            public Class<Float> type() { return Float.class; }

            @Override
            public Float deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return Float.valueOf( node.getNodeValue() );
            }
        }

        private static class DoubleAdapter extends ToStringTextNodeAdapter<Double>
        {
            @Override
            public Class<Double> type() { return Double.class; }

            @Override
            public Double deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return Double.valueOf( node.getNodeValue() );
            }
        }

        private static class BigDecimalAdapter extends ToStringTextNodeAdapter<BigDecimal>
        {
            @Override
            public Class<BigDecimal> type() { return BigDecimal.class; }

            @Override
            public BigDecimal deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return new BigDecimal( node.getNodeValue() );
            }
        }

        private static class BigIntegerAdapter extends ToStringTextNodeAdapter<BigInteger>
        {
            @Override
            public Class<BigInteger> type() { return BigInteger.class; }

            @Override
            public BigInteger deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return new BigInteger( node.getNodeValue() );
            }
        }

        private static class InstantAdapter extends ToStringTextNodeAdapter<Instant>
        {
            @Override
            public Class<Instant> type() { return Instant.class; }

            @Override
            public Instant deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return Instant.parse( node.getNodeValue() );
            }
        }

        private static class ZonedDateTimeAdapter extends ToStringTextNodeAdapter<ZonedDateTime>
        {
            @Override
            public Class<ZonedDateTime> type() { return ZonedDateTime.class; }

            @Override
            public ZonedDateTime deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return ZonedDateTime.parse( node.getNodeValue() );
            }
        }

        private static class OffsetDateTimeAdapter extends ToStringTextNodeAdapter<OffsetDateTime>
        {
            @Override
            public Class<OffsetDateTime> type() { return OffsetDateTime.class; }

            @Override
            public OffsetDateTime deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return OffsetDateTime.parse( node.getNodeValue() );
            }
        }

        private static class LocalDateTimeAdapter extends ToStringTextNodeAdapter<LocalDateTime>
        {
            @Override
            public Class<LocalDateTime> type() { return LocalDateTime.class; }

            @Override
            public LocalDateTime deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return LocalDateTime.parse( node.getNodeValue() );
            }
        }

        private static class LocalDateAdapter extends ToStringTextNodeAdapter<LocalDate>
        {
            @Override
            public Class<LocalDate> type() { return LocalDate.class; }

            @Override
            public LocalDate deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return LocalDate.parse( node.getNodeValue() );
            }
        }

        private static class LocalTimeAdapter extends ToStringTextNodeAdapter<LocalTime>
        {
            @Override
            public Class<LocalTime> type() { return LocalTime.class; }

            @Override
            public LocalTime deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return LocalTime.parse( node.getNodeValue() );
            }
        }

        private static class DurationAdapter extends ToStringTextNodeAdapter<Duration>
        {
            @Override
            public Class<Duration> type() { return Duration.class; }

            @Override
            public Duration deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return Duration.parse( node.getNodeValue() );
            }
        }

        private static class PeriodAdapter extends ToStringTextNodeAdapter<Period>
        {
            @Override
            public Class<Period> type() { return Period.class; }

            @Override
            public Period deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return Period.parse( node.getNodeValue() );
            }
        }

        private static class IdentityAdapter extends ToStringTextNodeAdapter<Identity>
        {
            @Override
            public Class<Identity> type() { return Identity.class; }

            @Override
            public Identity deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return StringIdentity.fromString( node.getNodeValue() );
            }
        }

        private static class EntityReferenceAdapter extends ToStringTextNodeAdapter<EntityReference>
        {
            @Override
            public Class<EntityReference> type() { return EntityReference.class; }

            @Override
            public EntityReference deserialize( Node node, BiFunction<Node, ValueType, Object> deserializationFunction )
            {
                return EntityReference.parseEntityReference( node.getNodeValue() );
            }
        }
    }
}
