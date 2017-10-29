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
package org.apache.polygene.library.sql.generator.grammar.query;

import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.builders.query.QuerySpecificationBuilder;

/**
 * This syntax element represents the single {@code SELECT} statement.
 *
 *
 * @see QuerySpecificationBuilder
 * @see SelectColumnClause
 * @see FromClause
 * @see BooleanExpression
 * @see GroupByClause
 * @see OrderByClause
 */
public interface QuerySpecification
    extends QueryExpressionBodyQuery
{

    /**
     * Returns the columns in this {@code SELECT} statement.
     *
     * @return The columns in this {@code SELECT} statement.
     */
    SelectColumnClause getColumns();

    /**
     * Returns the {@code FROM} clause of this {@code SELECT} statement.
     *
     * @return The {@code FROM} clause of this {@code SELECT} statement.
     */
    FromClause getFrom();

    /**
     * Returns the search condition for resulting rows of this {@code SELECT} statement.
     *
     * @return The search condition for resulting rows of this {@code SELECT} statement.
     */
    BooleanExpression getWhere();

    /**
     * Returns the {@code GROUP BY} clause of this {@code SELECT} statement.
     *
     * @return The {@code GROUP BY} clause of this {@code SELECT} statement.
     */
    GroupByClause getGroupBy();

    /**
     * Returns the grouping condition for {@code GROUP BY} clause of this {@code SELECT} statement.
     *
     * @return The grouping condition for {@code GROUP BY} clause of this {@code SELECT} statement.
     */
    BooleanExpression getHaving();

    /**
     * Returns the {@code ORDER BY} clause of this {@code SELECT} statement.
     *
     * @return The {@code ORDER BY} clause of this {@code SELECT} statement.
     */
    OrderByClause getOrderBy();

    /**
     * Returns the {@code FETCH FIRST <number> ROWS ONLY} expression for this {@code SELECT} statement.
     *
     * @return The {@code FETCH FIRST <number> ROWS ONLY} expression for this {@code SELECT} statement.
     */
    LimitSpecification getLimitSpecification();

    /**
     * Returns the {@code OFFSET <number> ROWS} expression for this {@code SELECT} statement.
     *
     * @return The {@code OFFSET <number> ROWS} expression for this {@code SELECT} statement.
     */
    OffsetSpecification getOffsetSpecification();
}
