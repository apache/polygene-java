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

import org.apache.polygene.library.sql.generator.grammar.builders.booleans.BooleanBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.NonBooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.query.QuerySpecification;

/**
 * This builder builds a single {@code SELECT} query. It acts as an aggregator for {@link ColumnsBuilder} for columns in
 * {@code SELECT}, {@link FromBuilder} for tables in {@code FROM} clause, {@link BooleanBuilder} for search condition in
 * {@code WHERE} clause, {@link GroupByBuilder} for {@code GROUP BY} clause, another {@link BooleanBuilder} for grouping
 * conditions in {@code HAVING} clause, and finally {@link OrderByBuilder} for {@code ORDER BY} clause.
 *
 * @see QuerySpecification
 */
public interface QuerySpecificationBuilder
    extends AbstractQueryBuilder<QuerySpecification>
{

    /**
     * Gets the builder for columns in this {@code SELECT} statement.
     *
     * @return The builder for columns in this {@code SELECT} statement.
     */
    ColumnsBuilder getSelect();

    /**
     * Gets the builder for {@code FROM} clause of this {@code SELECT} statement.
     *
     * @return The builder for {@code FROM} clause of this {@code SELECT} statement.
     */
    FromBuilder getFrom();

    /**
     * Gets the builder for search condition in {@code WHERE} clause of this {@code SELECT} statement.
     *
     * @return The builder for search condition in {@code WHERE} clause of this {@code SELECT} statement.
     */
    BooleanBuilder getWhere();

    /**
     * Gets the builder for {@code GROUP BY} clause of this {@code SELECT} statement.
     *
     * @return The builder for {@code GROUP BY} clause of this {@code SELECT} statement.
     */
    GroupByBuilder getGroupBy();

    /**
     * Gets the builder for grouping condition in {@code HAVING} clause of this {@code SELECT} statement.
     *
     * @return The builder for grouping condition in {@code HAVING} clause of this {@code SELECT} statement.
     */
    BooleanBuilder getHaving();

    /**
     * Gets the builder for {@code ORDER BY} clause of this {@code SELECT} statement.
     *
     * @return The builder for {@code ORDER BY} clause of this {@code SELECT} statement.
     */
    OrderByBuilder getOrderBy();

    /**
     * Checks that all selected columns are in {@code GROUP BY} clause. If they are not, it adds them as last columns of
     * {@code GROUP BY} clause.
     *
     * @return This builder.
     */
    QuerySpecificationBuilder trimGroupBy();

    /**
     * Sets the builder for columns in {@code SELECT} statement.
     *
     * @return This builder.
     */
    QuerySpecificationBuilder setSelect( ColumnsBuilder builder );

    /**
     * Sets the builder for {@code FROM} clause of this {@code SELECT} statement.
     *
     * @return This builder.
     */
    QuerySpecificationBuilder setFrom( FromBuilder builder );

    /**
     * Sets the builder for search condition in {@code WHERE} clause of this {@code SELECT} statement.
     *
     * @return This builder.
     */
    QuerySpecificationBuilder setWhere( BooleanBuilder builder );

    /**
     * Sets the builder for {@code GROUP BY} clause of this {@code SELECT} statement.
     *
     * @return This builder.
     */
    QuerySpecificationBuilder setGroupBy( GroupByBuilder builder );

    /**
     * Sets the builder for grouping condition in {@code HAVING} clause of this {@code SELECT} statement.
     *
     * @return This builder.
     */
    QuerySpecificationBuilder setHaving( BooleanBuilder builder );

    /**
     * Sets the builder for {@code ORDER BY} clause of this {@code SELECT} statement.
     *
     * @return This builder.
     */
    QuerySpecificationBuilder setOrderBy( OrderByBuilder builder );

    QuerySpecificationBuilder limit();

    QuerySpecificationBuilder limit( Integer max );

    QuerySpecificationBuilder limit( NonBooleanExpression max );

    QuerySpecificationBuilder offset( Integer skip );

    QuerySpecificationBuilder offset( NonBooleanExpression skip );
}
