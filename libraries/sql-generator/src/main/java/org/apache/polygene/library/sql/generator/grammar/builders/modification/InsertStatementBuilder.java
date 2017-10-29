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
package org.apache.polygene.library.sql.generator.grammar.builders.modification;

import org.apache.polygene.library.sql.generator.grammar.builders.AbstractBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.TableName;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.modification.ColumnSource;
import org.apache.polygene.library.sql.generator.grammar.modification.InsertStatement;

/**
 * This builder builds statements inserting rows to tables ({@code INSERT INTO } tableName {@code [(} column names
 * {@code )]} expression).
 *
 * @see TableName
 * @see ColumnSource
 */
public interface InsertStatementBuilder
    extends AbstractBuilder<InsertStatement>
{

    /**
     * Sets the table name for this {@code INSERT} statement.
     *
     * @param tableName The table name for this {@code INSERT} statement.
     * @return This builder.
     */
    InsertStatementBuilder setTableName( TableNameDirect tableName );

    /**
     * Returns the table name for this {@code INSERT} statement.
     *
     * @return The table name for this {@code INSERT} statement.
     */
    TableNameDirect getTableName();

    /**
     * Sets the source for the columns for this {@code INSERT} statement.
     *
     * @param source The source for columns for this {@code INSERT} statement.
     * @return This builder.
     */
    InsertStatementBuilder setColumnSource( ColumnSource source );

    /**
     * Returns the source for the columns for this {@code INSERT} statement.
     *
     * @return The source for the columns for this {@code INSERT} statement.
     */
    ColumnSource getColumnSource();
}
