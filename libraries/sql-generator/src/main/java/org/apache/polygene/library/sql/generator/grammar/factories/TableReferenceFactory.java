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

import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.builders.query.TableReferenceBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.ColumnNameList;
import org.apache.polygene.library.sql.generator.grammar.common.TableName;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameFunction;
import org.apache.polygene.library.sql.generator.grammar.literals.SQLFunctionLiteral;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;
import org.apache.polygene.library.sql.generator.grammar.query.TableAlias;
import org.apache.polygene.library.sql.generator.grammar.query.TableReferenceByExpression;
import org.apache.polygene.library.sql.generator.grammar.query.TableReferenceByName;
import org.apache.polygene.library.sql.generator.grammar.query.TableReferencePrimary;
import org.apache.polygene.library.sql.generator.grammar.query.joins.JoinCondition;
import org.apache.polygene.library.sql.generator.grammar.query.joins.NamedColumnsJoin;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 * A factory for creating builders and syntax elements related to tables. This factory is obtainable from
 * {@link SQLVendor}.
 *
 * @author Stanislav Muhametsin
 * @see SQLVendor
 */
public interface TableReferenceFactory
{
    /**
     * <p>
     * Creates a new table reference, which uses given table name, without table alias.
     * </p>
     * <p>
     * Calling this method is equivalent to calling {@link #table(TableName, TableAlias)} and passing {@code null} as
     * second parameter.
     * </p>
     *
     * @param tableName The table name to use.
     * @return The new {@link TableReferenceByName}.
     */
    TableReferenceByName table( TableName tableName );

    /**
     * Creates a new table references, which uses given table name along with given table alias.
     *
     * @param tableName The table name to use.
     * @param alias     The table alias to use. May be {@code null}.
     * @return The new {@link TableReferenceByName}.
     */
    TableReferenceByName table( TableName tableName, TableAlias alias );

    /**
     * <p>
     * Creates a new table name, which isn't schema-qualified.
     * </p>
     * <p>
     * Calling this method is equivalent to calling {@link #tableName(String, String)} and passing {@code null} as first
     * parameter.
     * </p>
     *
     * @param tableName The name of the table.
     * @return The new {@link TableName}.
     */
    TableNameDirect tableName( String tableName );

    /**
     * Creates a new table name. If the given schema-name is non-{@code null}, the table name is said to be
     * schema-qualified.
     *
     * @param schemaName The schema name to use. May be {@code null}.
     * @param tableName  The table name to use.
     * @return The new {@link TableName}.
     */
    TableNameDirect tableName( String schemaName, String tableName );

    /**
     * Creates a new table name representing a call to SQL function without a schema. This is equivalent to calling
     * {@link #tableName(String, SQLFunctionLiteral)} and passing {@code null} as first argument.
     *
     * @param function The function to call.
     * @return Table name representing a call to SQL function without a schema.
     */
    TableNameFunction tableName( SQLFunctionLiteral function );

    /**
     * Creates a new table name representing a call to SQL function with schema.
     *
     * @param schemaName The schema where function resides.
     * @param function   The function to call.
     * @return Table name representing a call to SQL function with schema.
     */
    TableNameFunction tableName( String schemaName, SQLFunctionLiteral function );

    /**
     * <p>
     * Creates a new alias for table.
     * </p>
     * <p>
     * Calling this method is equivalent to calling {@link #tableAliasWithCols(String, String...)} and not pass anything
     * to varargs parameter.
     * </p>
     *
     * @param tableNameAlias The alias for table name.
     * @return The new {@link TableAlias}.
     */
    TableAlias tableAlias( String tableNameAlias );

    /**
     * Creates a new table alias for table, with renamed columns.
     *
     * @param tableNameAlias The alias for table name.
     * @param colNames       The new column names for table.
     * @return The new {@link TableAlias}.
     */
    TableAlias tableAliasWithCols( String tableNameAlias, String... colNames );

    /**
     * <p>
     * Creates a new table reference, which will use the values returned by query as if they were values of the table.
     * </p>
     * <p>
     * Calling this method is equivalent to calling {@link #table(QueryExpression, TableAlias)} and passing {@code null}
     * as second parameter.
     * </p>
     *
     * @param query The query to use.
     * @return The new {@link TableReferenceByExpression}.
     */
    TableReferenceByExpression table( QueryExpression query );

    /**
     * Creates a new table reference, which will use the values returned by query as if they were values of the table.
     * Optionally, the table will has a given alias.
     *
     * @param query The query to use.
     * @param alias The table alias to use. May be {@code null} if no alias is needed.
     * @return The new {@link TableReferenceByExpression}.
     */
    TableReferenceByExpression table( QueryExpression query, TableAlias alias );

    /**
     * Creates a new {@link TableReferenceBuilder} typically used to build joined tables.
     *
     * @param firstTable The starting table.
     * @return The new {@link TableReferenceBuilder}.
     */
    TableReferenceBuilder tableBuilder( TableReferencePrimary firstTable );

    /**
     * Creates a join-condition using specified boolean expression to join tables.
     *
     * @param condition The condition to join tables.
     * @return The new {@link JoinCondition}.
     */
    JoinCondition jc( BooleanExpression condition );

    /**
     * Creates a new named columns join specification, which will use column names to join tables.
     *
     * @param columnNames The column names to use to join tables.
     * @return The new {@link NamedColumnsJoin}.
     */
    NamedColumnsJoin nc( ColumnNameList columnNames );
}
