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
package org.apache.polygene.library.sql.generator.grammar.builders.query;

import java.util.List;
import org.apache.polygene.library.sql.generator.grammar.builders.AbstractBuilder;
import org.apache.polygene.library.sql.generator.grammar.query.FromClause;
import org.apache.polygene.library.sql.generator.grammar.query.TableReference;

/**
 * The builder that builds the {@code FROM} clause in SQL {@code SELECT} query. It treats {@code FROM} clause as a list
 * of {@link TableReference}s, and acts as aggregator for {@link TableReferenceBuilder}s.
 *
 * @see TableReference
 * @see TableReferenceBuilder
 */
public interface FromBuilder
    extends AbstractBuilder<FromClause>
{

    /**
     * Adds table reference builders to this {@code FROM} clause. When {@link #createExpression()} method will be called
     * on this builder, it will build all table references from builders that were added through this method.
     *
     * @param tableRefs Table reference builders to add to this {@code FROM} clause.
     * @return This builder.
     */
    FromBuilder addTableReferences( TableReferenceBuilder... tableRefs );

    /**
     * Returns a list of table reference builders in this builder.
     *
     * @return A list of table reference builders in this builder. Might be empty.
     */
    List<TableReferenceBuilder> getTableReferences();
}
