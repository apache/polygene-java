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

import java.util.List;
import org.apache.polygene.library.sql.generator.grammar.builders.AbstractBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.ValueExpression;
import org.apache.polygene.library.sql.generator.grammar.modification.ColumnSourceByValues;

/**
 * This builder builds the {@link ColumnSourceByValues} syntax element.
 *
 */
public interface ColumnSourceByValuesBuilder
    extends AbstractBuilder<ColumnSourceByValues>
{
    /**
     * Adds the expressions as values to columns.
     *
     * @param values The value expressions to add.
     * @return This builder.
     */
    ColumnSourceByValuesBuilder addValues( ValueExpression... values );

    /**
     * Returns the value expressions for the columns.
     *
     * @return The value expressions for the columns.
     */
    List<ValueExpression> getValues();

    /**
     * Adds the names for the columns.
     *
     * @param columnNames The names for columns.
     * @return This builder.
     */
    ColumnSourceByValuesBuilder addColumnNames( String... columnNames );

    /**
     * Returns the names for the columns.
     *
     * @return The names for the columns.
     */
    List<String> getColumnNames();
}
