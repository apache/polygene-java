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
package org.apache.polygene.library.sql.generator.implementation.transformation.pgsql;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.polygene.library.sql.generator.Typeable;
import org.apache.polygene.library.sql.generator.grammar.booleans.BinaryPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.NotRegexpPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.RegexpPredicate;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.BigInt;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLInteger;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SmallInt;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.pgsql.Text;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableCommitAction;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.pgsql.PgSQLTableCommitAction;
import org.apache.polygene.library.sql.generator.grammar.literals.TimestampTimeLiteral;
import org.apache.polygene.library.sql.generator.grammar.manipulation.pgsql.PgSQLDropTableOrViewStatement;
import org.apache.polygene.library.sql.generator.grammar.modification.pgsql.PgSQLInsertStatement;
import org.apache.polygene.library.sql.generator.grammar.query.LimitSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.OffsetSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.QuerySpecification;
import org.apache.polygene.library.sql.generator.implementation.transformation.BooleanExpressionProcessing.BinaryPredicateProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.ConstantProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.DefaultSQLProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.DefinitionProcessing.TableDefinitionProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.pgsql.DefinitionProcessing.PGColumnDefinitionProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.pgsql.LiteralExpressionProcessing.PGDateTimeLiteralProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.pgsql.ManipulationProcessing.PgSQLDropTableOrViewStatementProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.pgsql.ModificationProcessing.PgSQLInsertStatementProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.pgsql.QueryProcessing.PgSQLLimitSpecificationProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.pgsql.QueryProcessing.PgSQLOffsetSpecificationProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.pgsql.QueryProcessing.PgSQLQuerySpecificationProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessor;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 *
 */
public class PostgreSQLProcessor extends DefaultSQLProcessor
{

    private static final Map<Class<? extends Typeable<?>>, SQLProcessor> _defaultProcessors;

    private static final Map<Class<? extends BinaryPredicate>, String> _defaultPgSQLBinaryOperators;

    static
    {
        Map<Class<? extends BinaryPredicate>, String> binaryOperators =
            new HashMap<Class<? extends BinaryPredicate>, String>(
                DefaultSQLProcessor.getDefaultBinaryOperators() );
        binaryOperators.put( RegexpPredicate.class, "~" );
        binaryOperators.put( NotRegexpPredicate.class, "!~" );
        _defaultPgSQLBinaryOperators = binaryOperators;

        Map<Class<? extends Typeable<?>>, SQLProcessor> processors =
            new HashMap<Class<? extends Typeable<?>>, SQLProcessor>(
                DefaultSQLProcessor.getDefaultProcessors() );

        // Override default processor for date-time
        processors.put( TimestampTimeLiteral.class, new PGDateTimeLiteralProcessor() );

        // Override default processor for column definition
        Map<Class<?>, String> dataTypeSerials = new HashMap<Class<?>, String>();
        dataTypeSerials.put( BigInt.class, "BIGSERIAL" );
        dataTypeSerials.put( SQLInteger.class, "SERIAL" );
        dataTypeSerials.put( SmallInt.class, "SMALLSERIAL" );
        processors.put( ColumnDefinition.class,
                        new PGColumnDefinitionProcessor( Collections.unmodifiableMap( dataTypeSerials ) ) );

        // Add support for regexp comparing
        processors
            .put(
                RegexpPredicate.class,
                new BinaryPredicateProcessor( _defaultPgSQLBinaryOperators
                                                  .get( RegexpPredicate.class ) ) );
        processors.put(
            NotRegexpPredicate.class,
            new BinaryPredicateProcessor( _defaultPgSQLBinaryOperators
                                              .get( NotRegexpPredicate.class ) ) );

        // Add support for PostgreSQL legacy LIMIT/OFFSET
        processors.put( QuerySpecification.class, new PgSQLQuerySpecificationProcessor() );
        processors.put( OffsetSpecification.class, new PgSQLOffsetSpecificationProcessor() );
        processors.put( LimitSpecification.class, new PgSQLLimitSpecificationProcessor() );

        // Add support for "TEXT" data type
        processors.put( Text.class, new ConstantProcessor( "TEXT" ) );

        // Add "DROP" table commit action
        Map<TableCommitAction, String> commitActions = new HashMap<TableCommitAction, String>(
            TableDefinitionProcessor.getDefaultCommitActions() );
        commitActions.put( PgSQLTableCommitAction.DROP, "DROP" );
        processors.put( TableDefinition.class,
                        new TableDefinitionProcessor( TableDefinitionProcessor.getDefaultTableScopes(),
                                                      commitActions ) );

        // Add "IF EXISTS" functionality to DROP TABLE/VIEW statements
        processors.put( PgSQLDropTableOrViewStatement.class,
                        new PgSQLDropTableOrViewStatementProcessor() );

        // Add support for PostgreSQL-specific INSTERT statement RETURNING clause
        processors.put( PgSQLInsertStatement.class, new PgSQLInsertStatementProcessor() );

        _defaultProcessors = processors;
    }

    public PostgreSQLProcessor( SQLVendor vendor )
    {
        super( vendor, _defaultProcessors );
    }
}
