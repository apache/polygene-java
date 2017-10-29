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
package org.apache.polygene.library.sql.generator.grammar.factories;

import org.apache.polygene.library.sql.generator.grammar.common.datatypes.BigInt;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.Decimal;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.DoublePrecision;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.IntervalDataType;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.Numeric;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.Real;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLBoolean;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLChar;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLDate;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLFloat;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLInteger;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLInterval;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLTime;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLTimeStamp;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SmallInt;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.UserDefinedType;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableDefinition;

/**
 * This is factory for creating SQL data types. Typically required when defining tables.
 *
 * @see TableDefinition
 */
public interface DataTypeFactory
{

    /**
     * Creates data type representing {@code BIGINT}.
     *
     * @return Data type representing {@code BIGINT}.
     */
    BigInt bigInt();

    /**
     * <p>
     * Creates plain {@code DECIMAL} type. Calling this method is equivalent to calling
     * {@link #decimal(Integer, Integer)} and passing {@code null} as both parameters.
     * </p>
     * <p>
     * The site http://intelligent-enterprise.informationweek.com/000626/celko.jhtml explains difference between
     * {@code NUMERIC} and {@code DECIMAL}:
     * </p>
     * <p>
     * The difference between DECIMAL(s,p) and NUMERIC(s,p) is subtle in the SQL-92 Standard -- DECIMAL(s,p) must be
     * exactly as precise as declared, while NUMERIC(s,p) must be at least as precise as declared.
     * </p>
     *
     * @return The new {@code DECIMAL} type.
     */
    Decimal decimal();

    /**
     * <p>
     * Creates {@code DECIMAL(p)} type, where {@code p} is given precision. Calling this method is equivalent to calling
     * {@link #decimal(Integer, Integer)} and passing {@code null} as second parameter.
     * </p>
     * <p>
     * The site http://intelligent-enterprise.informationweek.com/000626/celko.jhtml explains difference between
     * {@code NUMERIC} and {@code DECIMAL}:
     * </p>
     * <p>
     * The difference between DECIMAL(s,p) and NUMERIC(s,p) is subtle in the SQL-92 Standard -- DECIMAL(s,p) must be
     * exactly as precise as declared, while NUMERIC(s,p) must be at least as precise as declared.
     * </p>
     *
     * @param precision The precision for this {@code DECIMAL}. May be {@code null} to create plain {@code DECIMAL}
     *                  type.
     * @return The new {@code DECIMAL} type.
     */
    Decimal decimal( Integer precision );

    /**
     * <p>
     * Creates {@code DECIMAL(p,s)} type, where {@code p} is given precision, and {@code s} is given scale.
     * </p>
     * <p>
     * The site http://intelligent-enterprise.informationweek.com/000626/celko.jhtml explains difference between
     * {@code NUMERIC} and {@code DECIMAL}:
     * </p>
     * <p>
     * The difference between DECIMAL(s,p) and NUMERIC(s,p) is subtle in the SQL-92 Standard -- DECIMAL(s,p) must be
     * exactly as precise as declared, while NUMERIC(s,p) must be at least as precise as declared.
     * </p>
     *
     * @param precision The precision for this {@code DECIMAL}. May be {@code null} to create plain {@code DECIMAL}.
     * @param scale     The scale for this {@code DECIMAL}. Is ignored if {@code precision} is {@code null}.
     * @return The new {@code DECIMAL} type.
     */
    Decimal decimal( Integer precision, Integer scale );

    /**
     * Creates data type representing {@code DOUBLE PRECISION}.
     *
     * @return Data type representing {@code DOUBLE PRECISION}.
     */
    DoublePrecision doublePrecision();

    /**
     * <p>
     * Creates plain {@code NUMERIC} type. Calling this method is equivalent to calling
     * {@link #numeric(Integer, Integer)} and passing {@code null} as both parameters.
     * </p>
     * <p>
     * The site http://intelligent-enterprise.informationweek.com/000626/celko.jhtml explains difference between
     * {@code NUMERIC} and {@code DECIMAL}:
     * </p>
     * <p>
     * The difference between DECIMAL(s,p) and NUMERIC(s,p) is subtle in the SQL-92 Standard -- DECIMAL(s,p) must be
     * exactly as precise as declared, while NUMERIC(s,p) must be at least as precise as declared.
     * </p>
     *
     * @return The new {@code NUMERIC} type.
     */
    Numeric numeric();

    /**
     * <p>
     * Creates {@code NUMERIC(p)} type, where {@code p} is given precision. Calling this method is equivalent to calling
     * {@link #numeric(Integer, Integer)} and passing {@code null} as second parameter.
     * </p>
     * <p>
     * The site http://intelligent-enterprise.informationweek.com/000626/celko.jhtml explains difference between
     * {@code NUMERIC} and {@code DECIMAL}:
     * </p>
     * <p>
     * The difference between DECIMAL(s,p) and NUMERIC(s,p) is subtle in the SQL-92 Standard -- DECIMAL(s,p) must be
     * exactly as precise as declared, while NUMERIC(s,p) must be at least as precise as declared.
     * </p>
     *
     * @param precision The precision for this {@code NUMERIC}. May be {@code null} to create plain {@code NUMERIC}
     *                  type.
     * @return The new {@code NUMERIC} type.
     */
    Numeric numeric( Integer precision );

    /**
     * <p>
     * Creates {@code NUMERIC(p,s)} type, where {@code p} is given precision, and {@code s} is given scale.
     * </p>
     * <p>
     * The site http://intelligent-enterprise.informationweek.com/000626/celko.jhtml explains difference between
     * {@code NUMERIC} and {@code DECIMAL}:
     * </p>
     * <p>
     * The difference between DECIMAL(s,p) and NUMERIC(s,p) is subtle in the SQL-92 Standard -- DECIMAL(s,p) must be
     * exactly as precise as declared, while NUMERIC(s,p) must be at least as precise as declared.
     * </p>
     *
     * @param precision The precision for this {@code NUMERIC}. May be {@code null} to create plain {@code NUMERIC}.
     * @param scale     The scale for this {@code NUMERIC}. Is ignored if {@code precision} is {@code null}.
     * @return The new {@code NUMERIC} type.
     */
    Numeric numeric( Integer precision, Integer scale );

    /**
     * Creates data type representing {@code REAL}.
     *
     * @return Data type representing {@code REAL}.
     */
    Real real();

    /**
     * Creates data type representing {@code SMALLINT}.
     *
     * @return Data type representing {@code SMALLINT}.
     */
    SmallInt smallInt();

    /**
     * Creates data type representing {@code BOOLEAN}.
     *
     * @return Data type representing {@code BOOLEAN}.
     */
    SQLBoolean sqlBoolean();

    /**
     * Creates a new {@code CHARACTER} type. Calling this method is equivalent to calling {@link #sqlChar(Integer)} and
     * passing {@code null} as parameter.
     *
     * @return New {@code CHARACTER} type.
     */
    SQLChar sqlChar();

    /**
     * Creates a new {@code CHARACTER} type.
     *
     * @param length The length for this {@code CHARACTER}. May be {@code null}.
     * @return New {@code CHARACTER} type.
     */
    SQLChar sqlChar( Integer length );

    /**
     * Creates a new {@code CHARACTER VARYING} type. Calling this method is equivalent to calling
     * {@link #sqlVarChar(Integer)} and passing {@code null} as parameter.
     *
     * @return New {@code CHARACTER VARYING} type.
     */
    SQLChar sqlVarChar();

    /**
     * Creates a new {@code CHARACTER VARYING} type.
     *
     * @param length The length for this {@code CHARACTER VARYING}. May be {@code null}.
     * @return New {@code CHARACTER VARYING} type.
     */
    SQLChar sqlVarChar( Integer length );

    /**
     * Creates a {@code DATE} type.
     *
     * @return New {@code DATE} type.
     */
    SQLDate date();

    /**
     * Creates new instance of {@code FLOAT} type. Calling this method is equivalent to calling
     * {@link #sqlFloat(Integer)} and passing {@code null} as argument.
     *
     * @return New {@code FLOAt} type.
     */
    SQLFloat sqlFloat();

    /**
     * Creates new instance of {@code FLOAT} type.
     *
     * @param precision The precision for this {@code FLOAT} type. May be {@code null}.
     * @return New {@code FLOAT} type.
     */
    SQLFloat sqlFloat( Integer precision );

    /**
     * Creates new instance of {@code INTEGER} type.
     *
     * @return New {@code INTEGER} type.
     */
    SQLInteger integer();

    /**
     * Creates a new <b>year-month</b> {@code INTERVAL}.
     *
     * @param startField          The type of start field for this {@code INTERVAL}. Must be either {@link IntervalDataType#YEAR}
     *                            or {@link IntervalDataType#MONTH}.
     * @param startFieldPrecision The precision for the start field. May be {@code null} if none specified.
     * @param endField            The type of end field for this {@code INTERVAL}. May be {@code null} for single date interval. If
     *                            it is not {@code null}, must be either {@link IntervalDataType#YEAR} or {@link IntervalDataType#MONTH}
     *                            .
     * @return The new {@code INTERVAL} with specified values.
     * @throws NullPointerException     If {@code startField} is {@code null}.
     * @throws IllegalArgumentException If requirements for {@code startField} or {@code endField} fail.
     */
    SQLInterval yearMonthInterval( IntervalDataType startField, Integer startFieldPrecision,
                                   IntervalDataType endField );

    /**
     * Creates a new <b>day-time</b> {@code INTERVAL}.
     *
     * @param startField          The type of start field for this {@code INTERVAL}. Must not be {@link IntervalDataType#YEAR}
     *                            nor {@link IntervalDataType#MONTH}.
     * @param startFieldPrecision The precision for the start field. May {@code null} if none specified.
     * @param endField            The type of end field for this {@code INTERVAL}. May be {@code null} for single day-time
     *                            interval. If it is not {@code null}, it must not be {@link IntervalDataType#YEAR} nor
     *                            {@link IntervalDataType#MONTH}.
     * @param secondFracs         The fractional precision for when end field is {@link IntervalDataType#SECOND} or when this is
     *                            single day-time interval and start field is {@link IntervalDataType#SECOND}. Otherwise this value is
     *                            ignored.
     * @return The new {@code INTERVAL}.
     * @throws NullPointerException     If {@code startField} is {@code null}.
     * @throws IllegalArgumentException If requirements for {@code startField} or {@code endField} fail, or if when
     *                                  using single day-time interval with type {@link IntervalDataType#SECOND}, and {@code secondFracs}
     *                                  is specified, but {@code startFieldPrecision} is not specified.
     */
    SQLInterval dayTimeInterval( IntervalDataType startField, Integer startFieldPrecision,
                                 IntervalDataType endField, Integer secondFracs );

    /**
     * Creates {@code TIME} type with unspecified precision and unspecified value for including time zone. Calling this
     * method is equivalent to calling {@link #time(Integer, Boolean)} and pass {@code null} as both parameters.
     *
     * @return {@code TIME} type without precision and unspecified value for including time zone.
     */
    SQLTime time();

    /**
     * Creates {@code TIME} type with given precision and unspecified value for including time zone. Calling this method
     * is equivalent to calling {@link #time(Integer, Boolean)} and pass {@code null} as second parameter.
     *
     * @param precision The precision for {@code TIME}. May be {@code null}.
     * @return {@code TIME} type with given precision and unspecified value for including time zone.
     */
    SQLTime time( Integer precision );

    /**
     * Creates {@code TIME} type with unspecified precision and given value for including time zone. Calling this method
     * is equivalent to calling {@link #time(Integer, Boolean)} and pass {@code null} as first parameter.
     *
     * @param withTimeZone Whether to include time zone. May be {@code null} if no decision is wanted.
     * @return {@code TIME} type with unspecified precision and given value for including time zone.
     */
    SQLTime time( Boolean withTimeZone );

    /**
     * Creates {@code TIME} type with given precision and whether to include time zone.
     *
     * @param precision    The precision for {@code TIME}. May be {@code null}.
     * @param withTimeZone Whether to include time zone. May be {@code null} if no decision is wanted.
     * @return {@code TIME} type with given precision and whether to include time zone.
     */
    SQLTime time( Integer precision, Boolean withTimeZone );

    /**
     * Creates {@code TIMESTAMP} type with unspecified precision and unspecified value for including time zone. Calling
     * this method is equivalent to calling {@link #timeStamp(Integer, Boolean)} and pass {@code null} as both
     * parameters.
     *
     * @return {@code TIMESTAMP} type without precision and unspecified value for including time zone.
     */
    SQLTimeStamp timeStamp();

    /**
     * Creates {@code TIMESTAMP} type with given precision and unspecified value for including time zone. Calling this
     * method is equivalent to calling {@link #timeStamp(Integer, Boolean)} and pass {@code null} as second parameter.
     *
     * @param precision The precision for {@code TIME}. May be {@code null}.
     * @return {@code TIMESTAMP} type with given precision and unspecified value for including time zone.
     */
    SQLTimeStamp timeStamp( Integer precision );

    /**
     * Creates {@code TIMESTAMP} type with unspecified precision and given value for including time zone. Calling this
     * method is equivalent to calling {@link #timeStamp(Integer, Boolean)} and pass {@code null} as first parameter.
     *
     * @param withTimeZone Whether to include time zone. May be {@code null} if no decision is wanted.
     * @return {@code TIMESTAMP} type with unspecified precision and given value for including time zone.
     */
    SQLTimeStamp timeStamp( Boolean withTimeZone );

    /**
     * Creates {@code TIMESTAMP} type with given precision and whether to include time zone.
     *
     * @param precision    The precision for {@code TIMESTAMP}. May be {@code null}.
     * @param withTimeZone Whether to include time zone. May be {@code null} if no decision is wanted.
     * @return {@code TIMESTAMP} type with given precision and whether to include time zone.
     */
    SQLTimeStamp timeStamp( Integer precision, Boolean withTimeZone );

    /**
     * Creates a user-defined type which will be inserted into SQL statement as-such.
     *
     * @param textualContent The textual contents for user-defined type.
     * @return Syntax element for user defined type.
     */
    UserDefinedType userDefined( String textualContent );
}
