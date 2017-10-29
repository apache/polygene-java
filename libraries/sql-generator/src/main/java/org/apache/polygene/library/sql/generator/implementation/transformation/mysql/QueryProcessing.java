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
package org.apache.polygene.library.sql.generator.implementation.transformation.mysql;

import org.apache.polygene.library.sql.generator.Typeable;
import org.apache.polygene.library.sql.generator.grammar.common.SQLConstants;
import org.apache.polygene.library.sql.generator.grammar.query.LimitSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.OffsetSpecification;
import org.apache.polygene.library.sql.generator.implementation.transformation.QueryProcessing.LimitSpecificationProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.QueryProcessing.OffsetSpecificationProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.QueryProcessing.QuerySpecificationProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.apache.polygene.library.sql.generator.vendor.MySQLVendor;

/**
 */
public class QueryProcessing
{
    private static final String MYSQL_LIMIT_PREFIX = "LIMIT";
    private static final String MYSQL_LIMIT_POSTFIX = null;
    private static final String MYSQL_OFFSET_PREFIX = "OFFSET";
    private static final String MYSQL_OFFSET_POSTFIX = null;

    public static class MySQLQuerySpecificationProcessor extends QuerySpecificationProcessor
    {
        @Override
        protected boolean isOffsetBeforeLimit( SQLProcessorAggregator processor )
        {
            return false;
        }

        @Override
        protected void processLimitAndOffset( SQLProcessorAggregator processor, StringBuilder builder,
                                              Typeable<?> first, Typeable<?> second )
        {
            MySQLVendor vendor = (MySQLVendor) processor.getVendor();
            if( vendor.legacyLimit() )
            {
                // Just do the processing right away, because of the difference of syntax
                builder.append( SQLConstants.NEWLINE ).append( MYSQL_LIMIT_PREFIX )
                       .append( SQLConstants.TOKEN_SEPARATOR );
                if( second != null )
                {
                    processor.process( ( (OffsetSpecification) second ).getSkip(), builder );
                    builder.append( SQLConstants.COMMA );
                }
                if( first != null && ( (LimitSpecification) first ).getCount() != null )
                {
                    processor.process( ( (LimitSpecification) first ).getCount(), builder );
                }
                else if( second != null )
                {
                    builder.append( Long.MAX_VALUE );
                }
            }
            else
            {
                if( first == null && second != null )
                {
                    first = vendor.getQueryFactory().limit( vendor.getLiteralFactory().n( Long.MAX_VALUE ) );
                }
                super.processLimitAndOffset( processor, builder, first, second );
            }
        }
    }

    public static class MySQLOffsetSpecificationProcessor extends OffsetSpecificationProcessor
    {
        @Override
        protected String getPrefix( SQLProcessorAggregator processor )
        {
            return MYSQL_OFFSET_PREFIX;
        }

        @Override
        protected String getPostfix( SQLProcessorAggregator processor )
        {
            return MYSQL_OFFSET_POSTFIX;
        }
    }

    public static class MySQLLimitSpecificationProcessor extends LimitSpecificationProcessor
    {
        @Override
        protected String getPrefix( SQLProcessorAggregator processor )
        {
            return MYSQL_LIMIT_PREFIX;
        }

        @Override
        protected String getPostfix( SQLProcessorAggregator processor )
        {
            return MYSQL_LIMIT_POSTFIX;
        }
    }
}
