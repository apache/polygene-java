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
import org.apache.polygene.library.sql.generator.grammar.query.OrderByClause;
import org.apache.polygene.library.sql.generator.grammar.query.SortSpecification;

/**
 * The builder that builds the {@code ORDER BY} clause in SQL {@code SELECT} query.
 *
 * @author Stanislav Muhametsin
 * @see OrderByClause
 * @see SortSpecification
 */
public interface OrderByBuilder
    extends AbstractBuilder<OrderByClause>
{

    /**
     * Adds sort specifications to this {@code ORDER BY} clause.
     *
     * @param specs The sort specifications for this {@code ORDER BY} clause.
     * @return This builder.
     */
    OrderByBuilder addSortSpecs( SortSpecification... specs );

    /**
     * Returns the list of added sort specifications.
     *
     * @return The added sort specifications.
     */
    List<SortSpecification> getSortSpecs();
}
