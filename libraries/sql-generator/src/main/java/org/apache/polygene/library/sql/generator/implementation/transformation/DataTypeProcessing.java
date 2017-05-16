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
package org.apache.polygene.library.sql.generator.implementation.transformation;

import java.util.HashMap;
import java.util.Map;
import org.apache.polygene.library.sql.generator.grammar.common.SQLConstants;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.Decimal;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.IntervalDataType;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.Numeric;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLChar;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLFloat;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLInterval;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLTime;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLTimeStamp;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.UserDefinedType;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class DataTypeProcessing
{

    public static class UserDefinedDataTypeProcessor extends AbstractProcessor<UserDefinedType>
    {

        public UserDefinedDataTypeProcessor()
        {
            super( UserDefinedType.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, UserDefinedType object, StringBuilder builder )
        {
            builder.append( object.getTextualRepresentation() );
        }
    }

    public static class DecimalProcessor extends AbstractProcessor<Decimal>
    {

        public DecimalProcessor()
        {
            super( Decimal.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, Decimal object, StringBuilder builder )
        {
            builder.append( "DECIMAL" );
            if( object.getPrecision() != null )
            {
                builder.append( SQLConstants.OPEN_PARENTHESIS ).append( object.getPrecision() );
                if( object.getScale() != null )
                {
                    builder.append( object.getScale() );
                }
                builder.append( SQLConstants.CLOSE_PARENTHESIS );
            }
        }
    }

    public static class NumericProcessor extends AbstractProcessor<Numeric>
    {

        public NumericProcessor()
        {
            super( Numeric.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, Numeric object, StringBuilder builder )
        {
            builder.append( "NUMERIC" );
            if( object.getPrecision() != null )
            {
                builder.append( SQLConstants.OPEN_PARENTHESIS ).append( object.getPrecision() );
                if( object.getScale() != null )
                {
                    builder.append( object.getScale() );
                }
                builder.append( SQLConstants.CLOSE_PARENTHESIS );
            }
        }
    }

    public static class SQLCharProcessor extends AbstractProcessor<SQLChar>
    {
        public SQLCharProcessor()
        {
            super( SQLChar.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, SQLChar object, StringBuilder builder )
        {
            builder.append( "CHARACTER" );
            if( object.isVarying() )
            {
                builder.append( SQLConstants.TOKEN_SEPARATOR ).append( "VARYING" );
            }

            if( object.getLength() != null )
            {
                builder.append( SQLConstants.OPEN_PARENTHESIS ).append( object.getLength() )
                       .append( SQLConstants.CLOSE_PARENTHESIS );
            }
        }
    }

    public static class SQLFloatProcessor extends AbstractProcessor<SQLFloat>
    {

        public SQLFloatProcessor()
        {
            super( SQLFloat.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, SQLFloat object, StringBuilder builder )
        {
            builder.append( "FLOAT" );
            if( object.getPrecision() != null )
            {
                builder.append( SQLConstants.OPEN_PARENTHESIS ).append( object.getPrecision() )
                       .append( SQLConstants.CLOSE_PARENTHESIS );
            }
        }
    }

    public static class SQLIntervalProcessor extends AbstractProcessor<SQLInterval>
    {

        private final static Map<IntervalDataType, String> _defaultIntervalDataTypes;

        static
        {
            Map<IntervalDataType, String> map = new HashMap<IntervalDataType, String>();
            map.put( IntervalDataType.YEAR, "YEAR" );
            map.put( IntervalDataType.MONTH, "MONTH" );
            map.put( IntervalDataType.DAY, "DAY" );
            map.put( IntervalDataType.HOUR, "HOUR" );
            map.put( IntervalDataType.MINUTE, "MINUTE" );
            map.put( IntervalDataType.SECOND, "SECOND" );

            _defaultIntervalDataTypes = map;
        }

        private final Map<IntervalDataType, String> _intervalDataTypes;

        public SQLIntervalProcessor()
        {
            this( _defaultIntervalDataTypes );
        }

        public SQLIntervalProcessor( Map<IntervalDataType, String> intervalDataTypes )
        {
            super( SQLInterval.class );

            this._intervalDataTypes = intervalDataTypes;
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, SQLInterval object, StringBuilder builder )
        {
            builder.append( "INTERVAL" ).append( SQLConstants.TOKEN_SEPARATOR )
                   .append( this._intervalDataTypes.get( object.getStartField() ) );

            if( object.getStartFieldPrecision() != null )
            {
                builder.append( SQLConstants.OPEN_PARENTHESIS ).append( object.getStartFieldPrecision() );

                if( object.getEndField() == null && object.getSecondFracs() != null )
                {
                    builder.append( SQLConstants.COMMA ).append( SQLConstants.TOKEN_SEPARATOR )
                           .append( object.getSecondFracs() );
                }

                builder.append( SQLConstants.CLOSE_PARENTHESIS );
            }

            if( object.getEndField() != null )
            {
                builder.append( SQLConstants.TOKEN_SEPARATOR ).append( "TO" ).append( SQLConstants.TOKEN_SEPARATOR )
                       .append( this._intervalDataTypes.get( object.getEndField() ) );

                if( object.getSecondFracs() != null )
                {
                    builder.append( SQLConstants.OPEN_PARENTHESIS ).append( object.getSecondFracs() )
                           .append( SQLConstants.CLOSE_PARENTHESIS );
                }
            }
        }
    }

    public static class SQLTimeProcessor extends AbstractProcessor<SQLTime>
    {

        public SQLTimeProcessor()
        {
            super( SQLTime.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, SQLTime object, StringBuilder builder )
        {
            builder.append( "TIME" );
            if( object.getPrecision() != null )
            {
                builder.append( SQLConstants.OPEN_PARENTHESIS ).append( object.getPrecision() )
                       .append( SQLConstants.CLOSE_PARENTHESIS );
            }

            if( object.isWithTimeZone() != null )
            {
                builder.append( SQLConstants.TOKEN_SEPARATOR ).append( "WITH" );
                if( !object.isWithTimeZone() )
                {
                    builder.append( "OUT" );
                }

                builder.append( SQLConstants.TOKEN_SEPARATOR ).append( "TIME ZONE" );
            }
        }
    }

    public static class SQLTimeStampProcessor extends AbstractProcessor<SQLTimeStamp>
    {

        public SQLTimeStampProcessor()
        {
            super( SQLTimeStamp.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, SQLTimeStamp object, StringBuilder builder )
        {
            builder.append( "TIMESTAMP" );
            if( object.getPrecision() != null )
            {
                builder.append( SQLConstants.OPEN_PARENTHESIS ).append( object.getPrecision() )
                       .append( SQLConstants.CLOSE_PARENTHESIS );
            }

            if( object.isWithTimeZone() != null )
            {
                builder.append( SQLConstants.TOKEN_SEPARATOR ).append( "WITH" );
                if( !object.isWithTimeZone() )
                {
                    builder.append( "OUT" );
                }

                builder.append( SQLConstants.TOKEN_SEPARATOR ).append( "TIME ZONE" );
            }
        }
    }
}
