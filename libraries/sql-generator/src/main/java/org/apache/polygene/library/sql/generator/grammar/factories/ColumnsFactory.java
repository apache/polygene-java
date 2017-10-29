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

import java.util.Collection;
import org.apache.polygene.library.sql.generator.grammar.common.ColumnNameList;
import org.apache.polygene.library.sql.generator.grammar.common.ValueExpression;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReferenceByExpression;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReferenceByName;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 * A factory to create various expressions related to columns. This factory is obtainable from {@link SQLVendor}.
 *
 * @see SQLVendor
 */
public interface ColumnsFactory
{

    /**
     * Creates column reference, which has value of some expression.
     *
     * @param expression The expression for the column.
     * @return The new {@link ColumnReferenceByExpression}.
     */
    ColumnReferenceByExpression colExp( ValueExpression expression );

    /**
     * <p>
     * Creates column reference, which references column by name, without table name.
     * </p>
     * <p>
     * Calling this method is equivalent in calling {@link #colName(String, String)} and passing {@code null} as first
     * argument.
     * </p>
     *
     * @param colName The name of the column.
     * @return The new {@link ColumnReferenceByName}.
     */
    ColumnReferenceByName colName( String colName );

    /**
     * Creates column reference, which reference column by its name and by name of table, to which it belongs.
     *
     * @param tableName The name of the table. May be {@code null}.
     * @param colName   The name of the column.
     * @return The new {@link ColumnReferenceByName}.
     */
    ColumnReferenceByName colName( String tableName, String colName );

    /**
     * Constructs new {@link ColumnNameList}.
     *
     * @param names The column names. At least one element must be present.
     * @return The new {@link ColumnNameList}.
     */
    ColumnNameList colNames( String... names );

    /**
     * Constructs new {@link ColumnNameList} using specified column names. A new copy of List will be allocated for the
     * {@link ColumnNameList}.
     *
     * @param names The column names. Must contain at least one name.
     * @return The new {@link ColumnNameList}.
     */
    ColumnNameList colNames( Collection<String> names );
}
