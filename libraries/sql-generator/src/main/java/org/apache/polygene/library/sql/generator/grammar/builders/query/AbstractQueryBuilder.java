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

import org.apache.polygene.library.sql.generator.grammar.builders.AbstractBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.NonBooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.literals.NumericLiteral;
import org.apache.polygene.library.sql.generator.grammar.query.LimitSpecification;

/**
 */
public interface AbstractQueryBuilder<ExpressionType>
    extends AbstractBuilder<ExpressionType>
{
    /**
     * Adds the {@code FETCH FIRST ROW ONLY} expression for this query. The resulting {@link LimitSpecification} will
     * have its count as {@code null}.
     *
     * @return This builder.
     */
    AbstractQueryBuilder<ExpressionType> limit();

    /**
     * Adds the {@code FETCH FIRST <number> ROWS ONLY} expression for this query. Calling this method is equivalent of
     * calling {@link #limit(NonBooleanExpression)} and passing the {@link NumericLiteral} representing the given number
     * as the parameter.
     *
     * @param max The maximum amount of rows for this query to return. Use {@code null} to remove the
     *            {@code FETCH FIRST <number> ROWS ONLY} expression.
     * @return This builder.
     */
    AbstractQueryBuilder<ExpressionType> limit( Integer max );

    /**
     * Adds the {@code FETCH FIRST <number> ROWS ONLY} expression for this query.
     *
     * @param max The maximum amount of rows for this query to return. May be subquery or something else that evaluates
     *            to number or {@code NULL}. Use {@code null} to remove the {@code FETCH FIRST <number> ROWS ONLY}
     *            expression.
     * @return This builder.
     */
    AbstractQueryBuilder<ExpressionType> limit( NonBooleanExpression max );

    /**
     * Adds the {@code OFFSET <number> ROWS} expression for this query. Calling this method is equivalen of calling
     * {@link #offset(NonBooleanExpression)} and passing the {@link NumericLiteral} representing the given number as the
     * parameter.
     *
     * @param skip The amount of rows to skip before starting to include them into this query. Use {@code null} to
     *             remove the {@code OFFSET <number> ROWS} expression.
     * @return This builder.
     */
    AbstractQueryBuilder<ExpressionType> offset( Integer skip );

    /**
     * Adds the {@code OFFSET <number> ROWS} expression for this query.
     *
     * @param skip The amount of rows to skip before starting to include them into this query. May be subquery or
     *             something else that evaluates to number or {@code NULL}. Use {@code null} to remove the
     *             {@code OFFSET <number> ROWS} expression.
     * @return This builder.
     */
    AbstractQueryBuilder<ExpressionType> offset( NonBooleanExpression skip );
}
