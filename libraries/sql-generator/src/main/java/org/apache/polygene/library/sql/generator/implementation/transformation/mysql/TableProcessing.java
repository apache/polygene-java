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

import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameFunction;
import org.apache.polygene.library.sql.generator.implementation.transformation.TableReferenceProcessing.TableNameDirectProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.TableReferenceProcessing.TableNameFunctionProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class TableProcessing
{
    public static class MySQLTableNameDirectProcessor extends TableNameDirectProcessor
    {
        @Override
        protected void doProcess( SQLProcessorAggregator processor, TableNameDirect object, StringBuilder builder )
        {
            // MySQL does not understand schema-qualified table names
            builder.append( object.getTableName() );
        }
    }

    public static class MySQLTableNameFunctionProcessor extends TableNameFunctionProcessor
    {
        @Override
        protected void doProcess( SQLProcessorAggregator processor, TableNameFunction object, StringBuilder builder )
        {
            // MySQL does not understand schema-qualified table names
            processor.process( object.getFunction(), builder );
        }
    }
}
