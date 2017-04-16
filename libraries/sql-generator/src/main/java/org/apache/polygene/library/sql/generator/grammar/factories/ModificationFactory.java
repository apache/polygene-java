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

import org.apache.polygene.library.sql.generator.grammar.builders.modification.ColumnSourceByValuesBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.modification.DeleteBySearchBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.modification.InsertStatementBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.modification.UpdateBySearchBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.ColumnNameList;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.common.ValueExpression;
import org.apache.polygene.library.sql.generator.grammar.modification.ColumnSourceByQuery;
import org.apache.polygene.library.sql.generator.grammar.modification.ColumnSourceByValues;
import org.apache.polygene.library.sql.generator.grammar.modification.DeleteBySearch;
import org.apache.polygene.library.sql.generator.grammar.modification.InsertStatement;
import org.apache.polygene.library.sql.generator.grammar.modification.SetClause;
import org.apache.polygene.library.sql.generator.grammar.modification.TargetTable;
import org.apache.polygene.library.sql.generator.grammar.modification.UpdateBySearch;
import org.apache.polygene.library.sql.generator.grammar.modification.UpdateSource;
import org.apache.polygene.library.sql.generator.grammar.modification.UpdateSourceByExpression;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 * A factory, which creates SQL syntax elements related to modification statements ({@code INSERT INTO},
 * {@code DELETE FROM}, and {@code UPDATE}). This factory is obtainable from {@link SQLVendor}.
 *
 * @author Stanislav Muhametsin
 * @see SQLVendor
 */
public interface ModificationFactory
{

    /**
     * Creates a builder to add values as column sources in {@code INSERT INTO} statement.
     *
     * @return The new {@link ColumnSourceByValuesBuilder} for {@link ColumnSourceByValues}.
     */
    ColumnSourceByValuesBuilder columnSourceByValues();

    /**
     * <p>
     * Creates a column source, which uses a query as a source for columns in {@code INSERT INTO} statement.
     * </p>
     * <p>
     * Calling this method is equivalent in calling {@link #columnSourceByQuery(ColumnNameList, QueryExpression)} and
     * passing {@code null} as first argument.
     * </p>
     *
     * @param query The query to use as source for columns in {@code INSERT INTO} statement.
     * @return The new {@link ColumnSourceByQuery}.
     */
    ColumnSourceByQuery columnSourceByQuery( QueryExpression query );

    /**
     * Creates a column source, which uses specified target table column names and query as source columns in
     * {@code INSERT INTO} statement.
     *
     * @param columnNames The column names to use in target table.
     * @param query       The query to use to populate target table.
     * @return The new {@link ColumnSourceByQuery}.
     */
    ColumnSourceByQuery columnSourceByQuery( ColumnNameList columnNames, QueryExpression query );

    /**
     * Creates builder to create {@link DeleteBySearch} statements.
     *
     * @return The new builder for {@link DeleteBySearch}.
     * @see DeleteBySearchBuilder
     */
    DeleteBySearchBuilder deleteBySearch();

    /**
     * Creates builder to create {@link InsertStatement}s.
     *
     * @return The new builder for {@link InsertStatement}.
     * @see InsertStatementBuilder
     */
    InsertStatementBuilder insert();

    /**
     * Creates builder to create {@link UpdateBySearch} statements.
     *
     * @return The new builder for {@link UpdateBySearch} statements.
     * @see UpdateBySearchBuilder
     */
    UpdateBySearchBuilder updateBySearch();

    /**
     * <p>
     * Creates new target table to use in modification statements.
     * </p>
     * <p>
     * Calling this method is equivalent for calling {@link #createTargetTable(TableNameDirect, Boolean)} and passing
     * {@code false} as second parameter.
     *
     * @param tableName The name of the table.
     * @return The new {@link TargetTable}.
     */
    TargetTable createTargetTable( TableNameDirect tableName );

    /**
     * Creates new target table to use in modification statements.
     *
     * @param tableName The name of the table.
     * @param isOnly    Whether modification should affect child-tables too.
     * @return The new {@link TargetTable}.
     */
    TargetTable createTargetTable( TableNameDirect tableName, Boolean isOnly );

    /**
     * Creates a new source for {@code UPDATE} statement. This source will use specified expression as a source for
     * values.
     *
     * @param expression The expression to use.
     * @return The new {@link UpdateSourceByExpression}.
     * @see UpdateBySearch
     */
    UpdateSourceByExpression updateSourceByExp( ValueExpression expression );

    /**
     * Creates a new set clause for {@code UPDATE} statement.
     *
     * @param updateTarget The target of the update, typically name of the column.
     * @param updateSource The source for data to be put into that column.
     * @return The new {@link SetClause}.
     * @see UpdateBySearch
     */
    SetClause setClause( String updateTarget, UpdateSource updateSource );
}
