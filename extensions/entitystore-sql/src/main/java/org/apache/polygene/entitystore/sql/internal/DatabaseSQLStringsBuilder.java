/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with work for additional information
 *  regarding copyright ownership.  The ASF licenses file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use file except in compliance
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
package org.apache.polygene.entitystore.sql.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.polygene.api.injection.scope.This;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql.generation.api.grammar.builders.modification.DeleteBySearchBuilder;
import org.sql.generation.api.grammar.builders.modification.UpdateBySearchBuilder;
import org.sql.generation.api.grammar.common.SQLStatement;
import org.sql.generation.api.grammar.common.datatypes.SQLDataType;
import org.sql.generation.api.grammar.definition.table.UniqueSpecification;
import org.sql.generation.api.grammar.factories.BooleanFactory;
import org.sql.generation.api.grammar.factories.ColumnsFactory;
import org.sql.generation.api.grammar.factories.DefinitionFactory;
import org.sql.generation.api.grammar.factories.LiteralFactory;
import org.sql.generation.api.grammar.factories.ModificationFactory;
import org.sql.generation.api.grammar.factories.QueryFactory;
import org.sql.generation.api.grammar.factories.TableReferenceFactory;
import org.sql.generation.api.vendor.SQLVendor;

public interface DatabaseSQLStringsBuilder
{
    void init();

    String[] buildSQLForSchemaCreation();

    String[] buildSQLForIndexCreation();

    String[] buildSQLForTableCreation();

    String buildSQLForSelectAllEntitiesStatement();

    String buildSQLForSelectEntityStatement();

    String buildSQLForInsertEntityStatement();

    String buildSQLForUpdateEntityStatement();

    String buildSQLForRemoveEntityStatement();

    abstract class CommonMixin
        implements DatabaseSQLStringsBuilder
    {
        private static final Logger LOGGER = LoggerFactory.getLogger( DatabaseSQLStringsBuilder.class );

        @This
        private DatabaseSQLServiceState dbState;

        private SQLVendor vendor;
        private String schemaName;

        private String[] schemaCreationSQLs;
        private String[] indexCreationSQLs;
        private String[] tableCreationSQLs;

        private String selectAllEntitiesSQL;
        private String selectEntitySQL;
        private String insertEntitySQL;
        private String updateEntitySQL;
        private String removeEntitySQL;

        @Override
        public void init()
        {
            vendor = dbState.vendor().get();
            schemaName = dbState.schemaName().get();
            schemaCreationSQLs = toString( createSchemaStatements( vendor ) );
            indexCreationSQLs = toString( createIndicesStatements( vendor ) );
            tableCreationSQLs = toString( createTableStatements( vendor ) );
            selectAllEntitiesSQL = vendor.toString( createSelectAllEntitiesStatement( vendor ) );
            selectEntitySQL = vendor.toString( createSelectEntityStatement( vendor ) );
            insertEntitySQL = vendor.toString( createInsertEntityStatement( vendor ) );
            updateEntitySQL = vendor.toString( createUpdateEntityStatement( vendor ) );
            removeEntitySQL = vendor.toString( createRemoveEntityStatement( vendor ) );

            if( LOGGER.isTraceEnabled() )
            {
                LOGGER.trace( "SQL for schema creation: {}", Arrays.asList( schemaCreationSQLs ) );
                LOGGER.trace( "SQL for index creation: {}", Arrays.asList( indexCreationSQLs ) );
                LOGGER.trace( "SQL for table creation: {}", Arrays.asList( tableCreationSQLs ) );
                LOGGER.trace( "SQL for select all entities: {}", selectAllEntitiesSQL );
                LOGGER.trace( "SQL for select entity: {}", selectEntitySQL );
                LOGGER.trace( "SQL for insert entity: {}", insertEntitySQL );
                LOGGER.trace( "SQL for update entity: {}", updateEntitySQL );
                LOGGER.trace( "SQL for remove entity: {}", removeEntitySQL );
            }
        }

        protected String[] toString( SQLStatement[] stmts )
        {
            List<String> result = new ArrayList<>();
            if( stmts != null )
            {
                for( Integer idx = 0; idx < stmts.length; ++idx )
                {
                    SQLStatement statement = stmts[ idx ];
                    if( statement != null )
                    {
                        String stringStatement = vendor.toString( statement );
                        if( stringStatement != null && stringStatement.length() > 0 )
                        {
                            result.add( vendor.toString( statement ) );
                        }
                    }
                }
            }
            return result.toArray( new String[ result.size() ] );
        }

        protected SQLVendor getVendor()
        {
            return vendor;
        }

        protected String getSchemaName()
        {
            return schemaName;
        }

        protected SQLStatement[] createSchemaStatements( SQLVendor vendor )
        {
            return new SQLStatement[] {
                vendor.getDefinitionFactory().createSchemaDefinitionBuilder()
                      .setSchemaName( schemaName ).createExpression()
            };
        }

        protected SQLStatement[] createIndicesStatements( SQLVendor vendor )
        {
            return new SQLStatement[] {};
        }

        protected SQLStatement[] createTableStatements( SQLVendor vendor )
        {
            DefinitionFactory d = vendor.getDefinitionFactory();
            TableReferenceFactory t = vendor.getTableReferenceFactory();

            return new SQLStatement[] {
                d.createTableDefinitionBuilder()
                 .setTableName( t.tableName( getSchemaName(), SQLs.TABLE_NAME ) )
                 .setTableContentsSource(
                     d.createTableElementListBuilder()
                      .addTableElement( d.createColumnDefinition( SQLs.ENTITY_IDENTITY_COLUMN_NAME,
                                                                  getIDType(), false ) )
                      .addTableElement( d.createColumnDefinition( SQLs.ENTITY_VERSION_COLUMN_NAME,
                                                                  getVersionType(), false ) )
                      .addTableElement( d.createColumnDefinition( SQLs.ENTITY_STATE_COLUMN_NAME,
                                                                  getStateType(), false ) )
                      .addTableElement( d.createTableConstraintDefinition(
                          d.createUniqueConstraintBuilder()
                           .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                           .addColumns( SQLs.ENTITY_IDENTITY_COLUMN_NAME )
                           .createExpression() )
                      ).createExpression()
                 ).createExpression()
            };
        }

        protected SQLStatement createSelectAllEntitiesStatement( SQLVendor vendor )
        {
            QueryFactory q = vendor.getQueryFactory();
            TableReferenceFactory t = vendor.getTableReferenceFactory();

            return q.simpleQueryBuilder()
                    .select( SQLs.ENTITY_STATE_COLUMN_NAME )
                    .from( t.tableName( schemaName, SQLs.TABLE_NAME ) )
                    .createExpression();
        }

        protected SQLStatement createSelectEntityStatement( SQLVendor vendor )
        {
            QueryFactory q = vendor.getQueryFactory();
            TableReferenceFactory t = vendor.getTableReferenceFactory();
            BooleanFactory b = vendor.getBooleanFactory();
            ColumnsFactory c = vendor.getColumnsFactory();
            LiteralFactory l = vendor.getLiteralFactory();

            return q.simpleQueryBuilder()
                    .select( SQLs.ENTITY_STATE_COLUMN_NAME )
                    .from( t.tableName( schemaName, SQLs.TABLE_NAME ) )
                    .where( b.eq( c.colName( SQLs.ENTITY_IDENTITY_COLUMN_NAME ), l.param() ) )
                    .createExpression();
        }

        protected SQLStatement createInsertEntityStatement( SQLVendor vendor )
        {
            ModificationFactory m = vendor.getModificationFactory();
            TableReferenceFactory t = vendor.getTableReferenceFactory();
            LiteralFactory l = vendor.getLiteralFactory();

            return m.insert()
                    .setTableName( t.tableName( schemaName, SQLs.TABLE_NAME ) )
                    .setColumnSource( m.columnSourceByValues()
                                       .addColumnNames( SQLs.ENTITY_IDENTITY_COLUMN_NAME,
                                                        SQLs.ENTITY_STATE_COLUMN_NAME )
                                       .addValues( l.param(),
                                                   l.param() )
                                       .createExpression()
                    ).createExpression();
        }

        protected SQLStatement createUpdateEntityStatement( SQLVendor vendor )
        {
            ModificationFactory m = vendor.getModificationFactory();
            TableReferenceFactory t = vendor.getTableReferenceFactory();
            LiteralFactory l = vendor.getLiteralFactory();
            BooleanFactory b = vendor.getBooleanFactory();
            ColumnsFactory c = vendor.getColumnsFactory();

            UpdateBySearchBuilder builder = m.updateBySearch().setTargetTable(
                m.createTargetTable( t.tableName( schemaName, SQLs.TABLE_NAME ) )
            ).addSetClauses(
                m.setClause( SQLs.ENTITY_VERSION_COLUMN_NAME, m.updateSourceByExp( l.param() ) ),
                m.setClause( SQLs.ENTITY_STATE_COLUMN_NAME, m.updateSourceByExp( l.param() ) )
            );
            builder.getWhereBuilder().reset(
                b.eq( c.colName( SQLs.ENTITY_IDENTITY_COLUMN_NAME ), l.param() )
            ).and(
                b.eq( c.colName( SQLs.ENTITY_VERSION_COLUMN_NAME ), l.param() )
            );
            return builder.createExpression();
        }

        protected SQLStatement createRemoveEntityStatement( SQLVendor vendor )
        {
            ModificationFactory m = vendor.getModificationFactory();
            TableReferenceFactory t = vendor.getTableReferenceFactory();
            LiteralFactory l = vendor.getLiteralFactory();
            BooleanFactory b = vendor.getBooleanFactory();
            ColumnsFactory c = vendor.getColumnsFactory();

            DeleteBySearchBuilder builder = m.deleteBySearch().setTargetTable(
                m.createTargetTable( t.tableName( schemaName, SQLs.TABLE_NAME ) )
            );
            builder.getWhere().reset(
                b.eq( c.colName( SQLs.ENTITY_IDENTITY_COLUMN_NAME ), l.param() )
            );
            return builder.createExpression();
        }

        protected SQLDataType getIDType()
        {
            return vendor.getDataTypeFactory().sqlVarChar( 64 );
        }

        protected SQLDataType getVersionType()
        {
            return vendor.getDataTypeFactory().sqlVarChar( 64 );
        }

        protected SQLDataType getStateType()
        {
            return vendor.getDataTypeFactory().sqlVarChar( 10000 );
        }

        @Override
        public String[] buildSQLForSchemaCreation()
        {
            return schemaCreationSQLs;
        }

        @Override
        public String[] buildSQLForIndexCreation()
        {
            return indexCreationSQLs;
        }

        @Override
        public String buildSQLForSelectAllEntitiesStatement()
        {
            return selectAllEntitiesSQL;
        }

        @Override
        public String buildSQLForSelectEntityStatement()
        {
            return selectEntitySQL;
        }

        @Override
        public String buildSQLForInsertEntityStatement()
        {
            return insertEntitySQL;
        }

        @Override
        public String buildSQLForUpdateEntityStatement()
        {
            return updateEntitySQL;
        }

        @Override
        public String buildSQLForRemoveEntityStatement()
        {
            return removeEntitySQL;
        }

        @Override
        public String[] buildSQLForTableCreation()
        {
            return tableCreationSQLs;
        }
    }
}
