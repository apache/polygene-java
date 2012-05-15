/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.entitystore.sql.internal;

import org.qi4j.api.injection.scope.This;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql.generation.api.grammar.builders.modification.DeleteBySearchBuilder;
import org.sql.generation.api.grammar.builders.modification.UpdateBySearchBuilder;
import org.sql.generation.api.grammar.common.SQLStatement;
import org.sql.generation.api.grammar.common.datatypes.SQLDataType;
import org.sql.generation.api.grammar.definition.table.UniqueSpecification;
import org.sql.generation.api.grammar.factories.*;
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

    @SuppressWarnings("PublicInnerClass")
    abstract class CommonMixin
        implements DatabaseSQLStringsBuilder
    {

        private static final Logger LOGGER = LoggerFactory.getLogger( DatabaseSQLStringsBuilder.class );

        @This
        private DatabaseSQLServiceState _state;

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

        public void init()
        {
            this.vendor = this._state.vendor().get();

            this.schemaName = this._state.schemaName().get();

            this.schemaCreationSQLs = this.toString( this.createSchemaStatements( this.vendor ) );
            LOGGER.trace( "SQL for schema creation: {}", this.schemaCreationSQLs );

            this.indexCreationSQLs = this.toString( this.createIndicesStatements( this.vendor ) );
            LOGGER.trace( "SQL for index creation: {}", this.indexCreationSQLs );

            this.tableCreationSQLs = this.toString( this.createTableStatements( this.vendor ) );
            LOGGER.trace( "SQL for table creation: {}", this.tableCreationSQLs );

            this.selectAllEntitiesSQL = this.vendor.toString( this.createSelectAllEntitiesStatement( this.vendor ) );
            LOGGER.trace( "SQL for select all entities: {}", this.selectAllEntitiesSQL );

            this.selectEntitySQL = this.vendor.toString( this.createSelectEntityStatement( this.vendor ) );
            LOGGER.trace( "SQL for select entity: {}", this.selectEntitySQL );

            this.insertEntitySQL = this.vendor.toString( this.createInsertEntityStatement( this.vendor ) );
            LOGGER.trace( "SQL for insert entity: {}", this.insertEntitySQL );

            this.updateEntitySQL = this.vendor.toString( this.createUpdateEntityStatement( this.vendor ) );
            LOGGER.trace( "SQL for update entity: {}", this.updateEntitySQL );

            this.removeEntitySQL = this.vendor.toString( this.createRemoveEntityStatement( this.vendor ) );
            LOGGER.trace( "SQL for remove entity: {}", this.removeEntitySQL );
        }

        protected String[] toString( SQLStatement[] stmts )
        {
            String[] result = new String[stmts == null ? 0 : stmts.length];
            if( stmts != null )
            {
                for( Integer x = 0; x < stmts.length; ++x )
                {
                    result[x] = this.vendor.toString( stmts[x] );
                }
            }

            return result;
        }

        protected SQLVendor getVendor()
        {
            return this.vendor;
        }

        protected String getSchemaName()
        {
            return this.schemaName;
        }

        protected SQLStatement[] createSchemaStatements( SQLVendor vendor )
        {
            // @formatter:off
            return new SQLStatement[]
            {
                vendor.getDefinitionFactory().createSchemaDefinitionBuilder()
                .setSchemaName( this.schemaName )
                .createExpression()
            };
            // @formatter:on
        }

        protected SQLStatement[] createIndicesStatements( SQLVendor vendor )
        {
            // TODO
            return new SQLStatement[] {};
        }

        // TODO QueryEntityPK
        protected SQLStatement[] createTableStatements( SQLVendor vendor )
        {
            DefinitionFactory d = vendor.getDefinitionFactory();
            TableReferenceFactory t = vendor.getTableReferenceFactory();

            // When indexing and store are synchronized and we'll be able to let the database generate the PKs
            // something like this:
            // "my_pk_column BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "

            // @formatter:off
            
            return new SQLStatement[]
            {
                d.createTableDefinitionBuilder()
                    .setTableName( t.tableName( this.getSchemaName(), SQLs.TABLE_NAME ) )
                    .setTableContentsSource( d.createTableElementListBuilder()
                        .addTableElement( d.createColumnDefinition( SQLs.ENTITY_PK_COLUMN_NAME, this.getPKType(), false ) )
                        .addTableElement( d.createColumnDefinition( SQLs.ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME, this.getOptimisticLockType(), false ) )
                        .addTableElement( d.createColumnDefinition( SQLs.ENTITY_IDENTITY_COLUMN_NAME, this.getIDType(), false ) )
                        .addTableElement( d.createColumnDefinition( SQLs.ENTITY_STATE_COLUMN_NAME, this.getStateType(), false ) )
                        .addTableElement( d.createColumnDefinition( SQLs.ENTITY_LAST_MODIFIED_COLUMN_NAME, this.getLastModifiedType(), false ) )
                        .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                            .setUniqueness( UniqueSpecification.PRIMARY_KEY )
                            .addColumns( SQLs.ENTITY_PK_COLUMN_NAME )
                            .createExpression() ) )
                        .addTableElement( d.createTableConstraintDefinition( d.createUniqueConstraintBuilder()
                            .setUniqueness( UniqueSpecification.UNIQUE )
                            .addColumns( SQLs.ENTITY_IDENTITY_COLUMN_NAME )
                            .createExpression() ) )
                        .createExpression()
                        )
                   .createExpression()
            };
            
            // @formatter:on
        }

        protected SQLStatement createSelectAllEntitiesStatement( SQLVendor vendor )
        {
            QueryFactory q = vendor.getQueryFactory();
            TableReferenceFactory t = vendor.getTableReferenceFactory();

            // @formatter:off
            return q.simpleQueryBuilder()
                .select( SQLs.ENTITY_PK_COLUMN_NAME, SQLs.ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME, SQLs.ENTITY_STATE_COLUMN_NAME )
                .from( t.tableName( this.schemaName, SQLs.TABLE_NAME ) )
                .createExpression();
            // @formatter:on
        }

        protected SQLStatement createSelectEntityStatement( SQLVendor vendor )
        {
            QueryFactory q = vendor.getQueryFactory();
            TableReferenceFactory t = vendor.getTableReferenceFactory();
            BooleanFactory b = vendor.getBooleanFactory();
            ColumnsFactory c = vendor.getColumnsFactory();
            LiteralFactory l = vendor.getLiteralFactory();

            // @formatter:off
            return q.simpleQueryBuilder()
                .select( SQLs.ENTITY_PK_COLUMN_NAME, SQLs.ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME, SQLs.ENTITY_STATE_COLUMN_NAME )
                .from( t.tableName( this.schemaName, SQLs.TABLE_NAME ) )
                .where( b.eq( c.colName( SQLs.ENTITY_IDENTITY_COLUMN_NAME ), l.param() ) )
                .createExpression();
            // @formatter:on
        }

        protected SQLStatement createInsertEntityStatement( SQLVendor vendor )
        {
            ModificationFactory m = vendor.getModificationFactory();
            TableReferenceFactory t = vendor.getTableReferenceFactory();
            LiteralFactory l = vendor.getLiteralFactory();

            // @formatter:off
            return m.insert()
                .setTableName( t.tableName( this.schemaName, SQLs.TABLE_NAME ) )
                .setColumnSource( m.columnSourceByValues()
                    .addColumnNames(
                        SQLs.ENTITY_PK_COLUMN_NAME,
                        SQLs.ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME,
                        SQLs.ENTITY_IDENTITY_COLUMN_NAME,
                        SQLs.ENTITY_STATE_COLUMN_NAME,
                        SQLs.ENTITY_LAST_MODIFIED_COLUMN_NAME
                        )
                    .addValues(
                        l.param(),
                        l.n( 0 ),
                        l.param(),
                        l.param(),
                        l.param()
                        )
                    .createExpression()
                    )
                 .createExpression();
            // @formatter:on
        }

        protected SQLStatement createUpdateEntityStatement( SQLVendor vendor )
        {
            ModificationFactory m = vendor.getModificationFactory();
            TableReferenceFactory t = vendor.getTableReferenceFactory();
            LiteralFactory l = vendor.getLiteralFactory();
            BooleanFactory b = vendor.getBooleanFactory();
            ColumnsFactory c = vendor.getColumnsFactory();

            // @formatter:off
            UpdateBySearchBuilder builder = m.updateBySearch()
                .setTargetTable( m.createTargetTable( t.tableName( this.schemaName, SQLs.TABLE_NAME ) ) )
                .addSetClauses(
                    m.setClause( SQLs.ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME, m.updateSourceByExp( l.param() ) ),
                    m.setClause( SQLs.ENTITY_STATE_COLUMN_NAME, m.updateSourceByExp( l.param() ) ),
                    m.setClause( SQLs.ENTITY_LAST_MODIFIED_COLUMN_NAME, m.updateSourceByExp( l.param() ) )
                    );
            builder
                .getWhereBuilder()
                    .reset( b.eq( c.colName( SQLs.ENTITY_PK_COLUMN_NAME ), l.param() ) )
                    .and( b.eq( c.colName( SQLs.ENTITY_OPTIMISTIC_LOCK_COLUMN_NAME ), l.param() ) );
            return builder.createExpression();
            // @formatter:on
        }

        protected SQLStatement createRemoveEntityStatement( SQLVendor vendor )
        {
            ModificationFactory m = vendor.getModificationFactory();
            TableReferenceFactory t = vendor.getTableReferenceFactory();
            LiteralFactory l = vendor.getLiteralFactory();
            BooleanFactory b = vendor.getBooleanFactory();
            ColumnsFactory c = vendor.getColumnsFactory();

            // @formatter:off
            DeleteBySearchBuilder builder = m.deleteBySearch()
                .setTargetTable( m.createTargetTable( t.tableName( this.schemaName, SQLs.TABLE_NAME ) ) );
            builder.getWhere()
                .reset( b.eq( c.colName( SQLs.ENTITY_PK_COLUMN_NAME ), l.param() ) );
            return builder.createExpression();
            // @formatter:on
        }

        protected SQLDataType getPKType()
        {
            return this.vendor.getDataTypeFactory().bigInt();
        }

        protected SQLDataType getOptimisticLockType()
        {
            return this.vendor.getDataTypeFactory().bigInt();
        }

        protected SQLDataType getIDType()
        {
            return this.vendor.getDataTypeFactory().sqlVarChar( 256 );
        }

        protected SQLDataType getStateType()
        {
            return this.vendor.getDataTypeFactory().sqlVarChar( 10000 );
        }

        protected SQLDataType getLastModifiedType()
        {
            return this.vendor.getDataTypeFactory().bigInt();
        }

        public String[] buildSQLForSchemaCreation()
        {
            return this.schemaCreationSQLs;
        }

        public String[] buildSQLForIndexCreation()
        {
            return this.indexCreationSQLs;
        }

        public String buildSQLForSelectAllEntitiesStatement()
        {
            return this.selectAllEntitiesSQL;
        }

        public String buildSQLForSelectEntityStatement()
        {
            return this.selectEntitySQL;
        }

        public String buildSQLForInsertEntityStatement()
        {
            return this.insertEntitySQL;
        }

        public String buildSQLForUpdateEntityStatement()
        {
            return this.updateEntitySQL;
        }

        public String buildSQLForRemoveEntityStatement()
        {
            return this.removeEntitySQL;
        }

        public String[] buildSQLForTableCreation()
        {
            return this.tableCreationSQLs;
        }

    }

}
