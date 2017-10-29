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

import java.util.HashMap;
import java.util.Map;
import org.apache.polygene.library.sql.generator.Typeable;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameFunction;
import org.apache.polygene.library.sql.generator.grammar.definition.schema.SchemaDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropSchemaStatement;
import org.apache.polygene.library.sql.generator.grammar.query.LimitSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.OffsetSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.QuerySpecification;
import org.apache.polygene.library.sql.generator.implementation.transformation.DefaultSQLProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.NoOpProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.mysql.DefinitionProcessing.MySQLColumnDefinitionProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.mysql.DefinitionProcessing.MySQLSchemaDefinitionProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.mysql.QueryProcessing.MySQLLimitSpecificationProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.mysql.QueryProcessing.MySQLOffsetSpecificationProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.mysql.QueryProcessing.MySQLQuerySpecificationProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.mysql.TableProcessing.MySQLTableNameDirectProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.mysql.TableProcessing.MySQLTableNameFunctionProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessor;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 *
 */
public class MySQLProcessor extends DefaultSQLProcessor
{

    private static final Map<Class<? extends Typeable<?>>, SQLProcessor> _defaultProcessors;

    static
    {
        Map<Class<? extends Typeable<?>>, SQLProcessor> processors = new HashMap<Class<? extends Typeable<?>>, SQLProcessor>(
            DefaultSQLProcessor.getDefaultProcessors() );

        // MySQL does not understand schema-qualified table names (or anything related to schemas)
        processors.put( TableNameDirect.class, new MySQLTableNameDirectProcessor() );
        processors.put( TableNameFunction.class, new MySQLTableNameFunctionProcessor() );

        // Only process schema elements from schema definition, and ignore drop schema statements
        processors.put( SchemaDefinition.class, new MySQLSchemaDefinitionProcessor() );
        processors.put( DropSchemaStatement.class, new NoOpProcessor() );

        // Override default column definition support
        processors.put( ColumnDefinition.class, new MySQLColumnDefinitionProcessor() );

        // Different syntax for OFFSET/FETCH
        processors.put( QuerySpecification.class, new MySQLQuerySpecificationProcessor() );
        processors.put( OffsetSpecification.class, new MySQLOffsetSpecificationProcessor() );
        processors.put( LimitSpecification.class, new MySQLLimitSpecificationProcessor() );

        _defaultProcessors = processors;
    }

    public MySQLProcessor( SQLVendor vendor )
    {
        super( vendor, _defaultProcessors );
    }
}
