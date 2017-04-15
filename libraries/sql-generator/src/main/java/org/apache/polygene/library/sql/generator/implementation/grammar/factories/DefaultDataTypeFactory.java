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
package org.apache.polygene.library.sql.generator.implementation.grammar.factories;

import java.util.Objects;
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
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.BigIntImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.DecimalImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.DoublePrecisionImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.NumericImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.RealImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.SQLBooleanImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.SQLCharImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.SQLDateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.SQLFloatImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.SQLIntegerImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.SQLIntervalImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.SQLTimeImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.SQLTimeStampImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.SmallIntImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.datatypes.UserDefinedTypeImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 * @author Stanislav Muhametsin
 */
public class DefaultDataTypeFactory extends AbstractDataTypeFactory
{

    public DefaultDataTypeFactory( SQLVendor vendor, SQLProcessorAggregator processor )
    {
        super( vendor, processor );
    }

    public BigInt bigInt()
    {
        return BigIntImpl.INSTANCE;
    }

    public Decimal decimal( Integer precision, Integer scale )
    {
        return precision == null ? DecimalImpl.PLAIN_DECIMAL : new DecimalImpl( precision, scale );
    }

    public DoublePrecision doublePrecision()
    {
        return DoublePrecisionImpl.INSTANCE;
    }

    public Numeric numeric( Integer precision, Integer scale )
    {
        return precision == null ? NumericImpl.PLAIN_NUMERIC : new NumericImpl( precision, scale );
    }

    public Real real()
    {
        return RealImpl.INSTANCE;
    }

    public SmallInt smallInt()
    {
        return SmallIntImpl.INSTANCE;
    }

    public SQLBoolean sqlBoolean()
    {
        return SQLBooleanImpl.INSTANCE;
    }

    public SQLChar sqlChar( Integer length )
    {
        return length == null ? SQLCharImpl.PLAIN_FIXED : new SQLCharImpl( false, length );
    }

    public SQLChar sqlVarChar( Integer length )
    {
        return length == null ? SQLCharImpl.PLAIN_VARYING : new SQLCharImpl( true, length );
    }

    public SQLDate date()
    {
        return SQLDateImpl.INSTANCE;
    }

    public SQLFloat sqlFloat( Integer precision )
    {
        return precision == null ? SQLFloatImpl.PLAIN_FLOAT : new SQLFloatImpl( precision );
    }

    public SQLInteger integer()
    {
        return SQLIntegerImpl.INSTANCE;
    }

    public SQLInterval yearMonthInterval( IntervalDataType startField, Integer startFieldPrecision,
                                          IntervalDataType endField )
    {
        Objects.requireNonNull( startField, "Start field" );

        SQLInterval result = null;
        if( ( startField == IntervalDataType.YEAR || startField == IntervalDataType.MONTH )
            && ( endField == null || endField == IntervalDataType.YEAR || endField == IntervalDataType.MONTH ) )
        {
            result = new SQLIntervalImpl( startField, startFieldPrecision, endField, null );
        }
        else
        {
            throw new IllegalArgumentException( "The interval data types must be either YEAR or MONTH." );
        }

        return result;
    }

    public SQLInterval dayTimeInterval( IntervalDataType startField, Integer startFieldPrecision,
                                        IntervalDataType endField, Integer secondFracs )
    {
        Objects.requireNonNull( startField, "Start field" );
        SQLInterval result = null;
        if( startField != IntervalDataType.YEAR
            && startField != IntervalDataType.MONTH
            && ( endField == null || ( endField != IntervalDataType.YEAR && endField != IntervalDataType.MONTH && startField != IntervalDataType.SECOND ) ) )
        {
            if( secondFracs != null
                && ( startField != IntervalDataType.SECOND || ( endField != null && endField != IntervalDataType.SECOND ) ) )
            {
                // Trying to set second fractionals, even when not needed
                secondFracs = null;
            }

            if( endField == null && secondFracs != null && startFieldPrecision == null )
            {
                throw new IllegalArgumentException(
                    "When specifying second fracs for single day-time intervals, the start field precision must be specified also." );
            }

            result = new SQLIntervalImpl( startField, startFieldPrecision, endField, secondFracs );
        }
        else
        {
            throw new IllegalArgumentException(
                "The interval data types must be either DAY, HOUR, MINUTE, or SECOND. For single day-time intervals, the start field must not be SECOND if end field is non-null." );
        }
        return result;
    }

    public SQLTime time( Integer precision, Boolean withTimeZone )
    {
        SQLTime result = null;
        if( precision == null )
        {
            if( withTimeZone == null )
            {
                result = SQLTimeImpl.PLAIN_TIME;
            }
            else if( withTimeZone )
            {
                result = SQLTimeImpl.PLAIN_TIME_WITH_TZ;
            }
            else
            {
                result = SQLTimeImpl.PLAIN_TIME_WITHOUT_TZ;
            }
        }
        else
        {
            result = new SQLTimeImpl( precision, withTimeZone );
        }

        return result;
    }

    public SQLTimeStamp timeStamp( Integer precision, Boolean withTimeZone )
    {
        SQLTimeStamp result = null;
        if( precision == null )
        {
            if( withTimeZone == null )
            {
                result = SQLTimeStampImpl.PLAIN_TIMESTAMP;
            }
            else if( withTimeZone )
            {
                result = SQLTimeStampImpl.PLAIN_TIMESTAMP_WITH_TZ;
            }
            else
            {
                result = SQLTimeStampImpl.PLAIN_TIMESTAMP_WITHOUT_TZ;
            }
        }
        else
        {
            result = new SQLTimeStampImpl( precision, withTimeZone );
        }

        return result;
    }

    public UserDefinedType userDefined( String textualContent )
    {
        return new UserDefinedTypeImpl( textualContent );
    }
}
