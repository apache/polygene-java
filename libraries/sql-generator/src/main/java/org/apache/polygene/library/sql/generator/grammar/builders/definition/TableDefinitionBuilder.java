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
package org.apache.polygene.library.sql.generator.grammar.builders.definition;

import org.apache.polygene.library.sql.generator.grammar.builders.AbstractBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.TableName;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableCommitAction;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableContentsSource;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableElementList;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableScope;

/**
 * This is the builder for table definition statements ({@code CREATE TABLE} ...).
 *
 * @author Stanislav Muhametsin
 */
public interface TableDefinitionBuilder
    extends AbstractBuilder<TableDefinition>
{

    /**
     * Sets the scope for this table.
     *
     * @param scope The scope for this table.
     * @return This builder.
     * @see TableScope
     */
    TableDefinitionBuilder setTableScope( TableScope scope );

    /**
     * Sets the name for this table.
     *
     * @param tableName The name for this table.
     * @return This builder.
     * @see TableName
     */
    TableDefinitionBuilder setTableName( TableNameDirect tableName );

    /**
     * Sets the commit action for this table.
     *
     * @param commitAction The commit action for this table.
     * @return This builder.
     * @see TableCommitAction
     */
    TableDefinitionBuilder setCommitAction( TableCommitAction commitAction );

    /**
     * Sets the contents source for this table.
     *
     * @param contents The contents source for this table.
     * @return This builder.
     * @see TableContentsSource
     * @see TableElementList
     */
    TableDefinitionBuilder setTableContentsSource( TableContentsSource contents );

    /**
     * Returns the scope for this table.
     *
     * @return The scope for this table.
     */
    TableScope getTableScope();

    /**
     * Returns the name for this table.
     *
     * @return The name for this table.
     */
    TableNameDirect getTableName();

    /**
     * Returns the commit action for this table.
     *
     * @return The commit action for this table.
     */
    TableCommitAction getCommitAction();

    /**
     * Returns the table contents source for this table.
     *
     * @return The table contents source for this table.
     */
    TableContentsSource getTableContentsSource();
}
