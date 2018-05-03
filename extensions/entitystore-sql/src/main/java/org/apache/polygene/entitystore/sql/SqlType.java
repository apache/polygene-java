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
package org.apache.polygene.entitystore.sql;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.jooq.types.Interval;

/**
 * Translation of Java types to SQLDataType in JOOQ.
 */
class SqlType
{
    static <T> Field<T> makeField( String columnName, Class<T> type, SQLDialect dialect, boolean reference )
    {
        return DSL.field( DSL.name( columnName ), getSqlDataTypeFor( dialect, type, reference ) );
    }

    public static <T> Field<T> makeField( String columnName, Class<T> type, SQLDialect dialect )
    {
        return makeField( columnName, type, dialect, true );
    }

    @SuppressWarnings( "unchecked" )
    static <T> DataType<T> getSqlDataTypeFor( SQLDialect dialect, Class<T> propertyType, boolean reference )
    {
        if( String.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) varCharType( dialect, reference );
        }
        if( Integer.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) SQLDataType.INTEGER;
        }
        if( Timestamp.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) SQLDataType.TIMESTAMP;
        }
        if( Long.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) SQLDataType.BIGINT;
        }
        if( Boolean.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) SQLDataType.BOOLEAN;
        }
        if( Float.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) SQLDataType.REAL;
        }
        if( Double.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) SQLDataType.DOUBLE;
        }
        if( Instant.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) varCharType( dialect, reference );
        }
        if( Interval.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) varCharType( dialect, reference );
        }
        if( Period.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) varCharType( dialect, reference );
        }
        if( LocalDate.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) varCharType( dialect, reference );
        }
        if( LocalTime.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) varCharType( dialect, reference );
        }
        if( LocalDateTime.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) varCharType( dialect, reference );
        }
        if( ZonedDateTime.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) varCharType( dialect, reference );
        }
        if( OffsetDateTime.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) varCharType( dialect, reference );
        }
        if( Character.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) SQLDataType.CHAR( 1 );
        }
        if( Short.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) SQLDataType.INTEGER;
        }
        if( Byte.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) SQLDataType.INTEGER;
        }
        if( Byte.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) SQLDataType.INTEGER;
        }
        if( BigDecimal.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) SQLDataType.DECIMAL;
        }
        if( BigInteger.class.isAssignableFrom( propertyType ) )
        {
            return (DataType<T>) SQLDataType.DECIMAL;
        }
        if( propertyType.isPrimitive() )
        {
            if( propertyType.equals( Integer.TYPE ) )
            {
                return (DataType<T>) SQLDataType.INTEGER;
            }
            if( propertyType.equals( Long.TYPE ) )
            {
                return (DataType<T>) SQLDataType.BIGINT;
            }
            if( propertyType.equals( Boolean.TYPE ) )
            {
                return (DataType<T>) SQLDataType.BOOLEAN;
            }
            if( propertyType.equals( Float.TYPE ) )
            {
                return (DataType<T>) SQLDataType.REAL;
            }
            if( propertyType.equals( Double.TYPE ) )
            {
                return (DataType<T>) SQLDataType.DOUBLE;
            }
            if( propertyType.equals( Character.TYPE ) )
            {
                return (DataType<T>) SQLDataType.CHAR( 1 );
            }
            if( propertyType.equals( Short.TYPE ) )
            {
                return (DataType<T>) SQLDataType.INTEGER;
            }
            if( propertyType.equals( Byte.TYPE ) )
            {
                return (DataType<T>) SQLDataType.INTEGER;
            }
        }
        return (DataType<T>) varCharType( dialect, reference );
    }

    private static DataType<String> varCharType( SQLDialect dialect, boolean reference )
    {
        if( dialect == SQLDialect.MYSQL || dialect == SQLDialect.MARIADB )
        {
            if( reference )
            {
                return SQLDataType.VARCHAR.length(200).nullable(false);
            }
            else
            {
                return new DefaultDataType<>( null, String.class, "MEDIUMTEXT" );
            }
        }
        return SQLDataType.VARCHAR;
    }
}
