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
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.serialization.Converter;
import org.apache.polygene.api.serialization.Converters;
import org.apache.polygene.api.type.ValueType;

/**
 * Built-in serialization converters.
 *
 * Mixin for {@link org.apache.polygene.api.serialization.Serialization} implementations that provides built-in
 * {@link Converter}s for the following types:
 *
 * <ul>
 *     <li>{@link Identity}</li>
 *     <li>{@link EntityReference}</li>
 *     <li>{@link BigDecimal}</li>
 *     <li>{@link BigInteger}</li>
 *     <li>{@link Instant}</li>
 *     <li>{@link ZonedDateTime}</li>
 *     <li>{@link OffsetDateTime}</li>
 *     <li>{@link LocalDateTime}</li>
 *     <li>{@link LocalDate}</li>
 *     <li>{@link LocalTime}</li>
 *     <li>{@link Duration}</li>
 *     <li>{@link Period}</li>
 * </ul>
 *
 * Note that this does not include {@link String} nor primitive values and their boxed counterparts.
 * {@literal Serialization} implementations must handle those.
 */
@Mixins( BuiltInConverters.Mixin.class )
public interface BuiltInConverters
{
    void registerBuiltInConverters( Converters converters );

    class Mixin implements BuiltInConverters
    {
        @Override
        public void registerBuiltInConverters( Converters converters )
        {
            // Polygene types
            converters.registerConverter( ValueType.IDENTITY, new IdentityConverter() );
            converters.registerConverter( ValueType.ENTITY_REFERENCE, new EntityReferenceConverter() );

            // Big numbers types
            converters.registerConverter( ValueType.BIG_DECIMAL, new BigDecimalConverter() );
            converters.registerConverter( ValueType.BIG_INTEGER, new BigIntegerConverter() );

            // Date types
            converters.registerConverter( ValueType.INSTANT, new InstantConverter() );
            converters.registerConverter( ValueType.ZONED_DATE_TIME, new ZonedDateTimeConverter() );
            converters.registerConverter( ValueType.OFFSET_DATE_TIME, new OffsetDateTimeConverter() );
            converters.registerConverter( ValueType.LOCAL_DATE_TIME, new LocalDateTimeConverter() );
            converters.registerConverter( ValueType.LOCAL_DATE, new LocalDateConverter() );
            converters.registerConverter( ValueType.LOCAL_TIME, new LocalTimeConverter() );
            converters.registerConverter( ValueType.DURATION, new DurationConverter() );
            converters.registerConverter( ValueType.PERIOD, new PeriodConverter() );
        }

        private static abstract class ToStringConverter<T> implements Converter<T>
        {
            @Override
            public String toString( T object )
            {
                return object.toString();
            }
        }

        private static class IdentityConverter extends ToStringConverter<Identity>
        {
            @Override
            public Class<Identity> type()
            {
                return Identity.class;
            }

            @Override
            public Identity fromString( String string )
            {
                return StringIdentity.identityOf( string );
            }
        }

        private static class EntityReferenceConverter extends ToStringConverter<EntityReference>
        {
            @Override
            public Class<EntityReference> type()
            {
                return EntityReference.class;
            }

            @Override
            public EntityReference fromString( String string )
            {
                return EntityReference.parseEntityReference( string );
            }
        }

        private static class BigDecimalConverter extends ToStringConverter<BigDecimal>
        {
            @Override
            public Class<BigDecimal> type()
            {
                return BigDecimal.class;
            }

            @Override
            public BigDecimal fromString( String string )
            {
                return new BigDecimal( string );
            }
        }

        private static class BigIntegerConverter extends ToStringConverter<BigInteger>
        {
            @Override
            public Class<BigInteger> type()
            {
                return BigInteger.class;
            }

            @Override
            public BigInteger fromString( String string )
            {
                return new BigInteger( string );
            }
        }

        private static class PeriodConverter extends ToStringConverter<Period>
        {
            @Override
            public Class<Period> type() { return Period.class; }

            @Override
            public Period fromString( String string )
            {
                return Period.parse( string );
            }
        }

        private static class DurationConverter extends ToStringConverter<Duration>
        {
            @Override
            public Class<Duration> type() { return Duration.class; }

            @Override
            public Duration fromString( String string )
            {
                return Duration.parse( string );
            }
        }

        private static class LocalTimeConverter extends ToStringConverter<LocalTime>
        {
            @Override
            public Class<LocalTime> type() { return LocalTime.class; }

            @Override
            public LocalTime fromString( String string )
            {
                return LocalTime.parse( string );
            }
        }

        private static class LocalDateConverter extends ToStringConverter<LocalDate>
        {
            @Override
            public Class<LocalDate> type() { return LocalDate.class; }

            @Override
            public LocalDate fromString( String string )
            {
                return LocalDate.parse( string );
            }
        }

        private static class LocalDateTimeConverter extends ToStringConverter<LocalDateTime>
        {
            @Override
            public Class<LocalDateTime> type() { return LocalDateTime.class; }

            @Override
            public LocalDateTime fromString( String string )
            {
                return LocalDateTime.parse( string );
            }
        }

        private static class OffsetDateTimeConverter extends ToStringConverter<OffsetDateTime>
        {
            @Override
            public Class<OffsetDateTime> type() { return OffsetDateTime.class; }

            @Override
            public OffsetDateTime fromString( String string )
            {
                return OffsetDateTime.parse( string );
            }
        }

        private static class ZonedDateTimeConverter extends ToStringConverter<ZonedDateTime>
        {
            @Override
            public Class<ZonedDateTime> type() { return ZonedDateTime.class; }

            @Override
            public ZonedDateTime fromString( String string )
            {
                return ZonedDateTime.parse( string );
            }
        }

        private static class InstantConverter extends ToStringConverter<Instant>
        {
            @Override
            public Class<Instant> type() { return Instant.class; }

            @Override
            public Instant fromString( String string )
            {
                return Instant.parse( string );
            }
        }
    }
}
