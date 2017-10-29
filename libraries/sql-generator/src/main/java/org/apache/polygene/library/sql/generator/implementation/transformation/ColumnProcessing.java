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

import java.util.Iterator;
import org.apache.polygene.library.sql.generator.grammar.common.ColumnNameList;
import org.apache.polygene.library.sql.generator.grammar.common.SQLConstants;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReferenceByExpression;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReferenceByName;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class ColumnProcessing
{

    public static class ColumnReferenceByNameProcessor extends AbstractProcessor<ColumnReferenceByName>
    {
        public ColumnReferenceByNameProcessor()
        {
            super( ColumnReferenceByName.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator processor, ColumnReferenceByName columnRef,
                                  StringBuilder builder )
        {
            String tableName = columnRef.getTableName();
            if( ProcessorUtils.notNullAndNotEmpty( tableName ) )
            {
                builder.append( tableName ).append( SQLConstants.TABLE_COLUMN_SEPARATOR );
            }

            builder.append( columnRef.getColumnName() );
        }
    }

    public static class ColumnReferenceByExpressionProcessor extends AbstractProcessor<ColumnReferenceByExpression>
    {

        public ColumnReferenceByExpressionProcessor()
        {
            super( ColumnReferenceByExpression.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator processor, ColumnReferenceByExpression columnRef,
                                  StringBuilder builder )
        {
            processor.process( columnRef.getExpression(), builder );
        }
    }

    public static class ColumnNamesProcessor extends AbstractProcessor<ColumnNameList>
    {
        public ColumnNamesProcessor()
        {
            super( ColumnNameList.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator processor, ColumnNameList object, StringBuilder builder )
        {
            builder.append( SQLConstants.OPEN_PARENTHESIS );
            Iterator<String> iter = object.getColumnNames().iterator();
            while( iter.hasNext() )
            {
                builder.append( iter.next() );
                if( iter.hasNext() )
                {
                    builder.append( SQLConstants.COMMA ).append( SQLConstants.TOKEN_SEPARATOR );
                }
            }
            builder.append( SQLConstants.CLOSE_PARENTHESIS );
        }
    }
}
