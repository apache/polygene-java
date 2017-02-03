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
 *
 *
 */
package org.apache.polygene.api.type;

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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.Identity;

import static java.util.stream.Collectors.joining;

/**
 * Base class for types of values in ValueComposites and Properties.
 */
public class ValueType implements HasTypes
{
    public static final ValueType OBJECT = ValueType.of( Object.class );
    public static final ValueType STRING = ValueType.of( String.class );
    public static final ValueType CHARACTER = ValueType.of( Character.class, char.class );
    public static final ValueType BOOLEAN = ValueType.of( Boolean.class, boolean.class );
    public static final ValueType INTEGER = ValueType.of( Integer.class, int.class );
    public static final ValueType LONG = ValueType.of( Long.class, long.class );
    public static final ValueType SHORT = ValueType.of( Short.class, short.class );
    public static final ValueType BYTE = ValueType.of( Byte.class, byte.class );
    public static final ValueType FLOAT = ValueType.of( Float.class, float.class );
    public static final ValueType DOUBLE = ValueType.of( Double.class, double.class );
    public static final ValueType BIG_DECIMAL = ValueType.of( BigDecimal.class );
    public static final ValueType BIG_INTEGER = ValueType.of( BigInteger.class );
    public static final ValueType INSTANT = ValueType.of( Instant.class );
    public static final ValueType ZONED_DATE_TIME = ValueType.of( ZonedDateTime.class );
    public static final ValueType OFFSET_DATE_TIME = ValueType.of( OffsetDateTime.class );
    public static final ValueType LOCAL_DATE_TIME = ValueType.of( LocalDateTime.class );
    public static final ValueType LOCAL_DATE = ValueType.of( LocalDate.class );
    public static final ValueType LOCAL_TIME = ValueType.of( LocalTime.class );
    public static final ValueType DURATION = ValueType.of( Duration.class );
    public static final ValueType PERIOD = ValueType.of( Period.class );
    public static final ValueType IDENTITY = ValueType.of( Identity.class );
    public static final ValueType ENTITY_REFERENCE = ValueType.of( EntityReference.class );

    public static ValueType of( Class<?>... types )
    {
        return new ValueType( types );
    }

    protected final List<Class<?>> types;

    protected ValueType( Class<?>... types )
    {
        this( Arrays.asList( types ) );
    }

    protected ValueType( List<Class<?>> types )
    {
        this.types = types;
    }

    public Class<?> primaryType()
    {
        return types.stream().findFirst().orElse( null );
    }

    @Override
    public Stream<Class<?>> types()
    {
        return types.stream();
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o ) { return true; }
        if( o == null || getClass() != o.getClass() ) { return false; }
        ValueType valueType = (ValueType) o;
        return Objects.equals( types, valueType.types );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( types );
    }

    @Override
    public String toString()
    {
        String name = types.stream().map( Class::getName ).collect( joining( "," ) );
        if( name.contains( "," ) )
        {
            name = "{" + name + "}";
        }
        return name;
    }
}