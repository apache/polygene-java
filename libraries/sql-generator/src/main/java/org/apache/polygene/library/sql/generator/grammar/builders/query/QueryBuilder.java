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
import org.apache.polygene.library.sql.generator.grammar.common.SetQuantifier;
import org.apache.polygene.library.sql.generator.grammar.query.CorrespondingSpec;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpressionBody;
import org.apache.polygene.library.sql.generator.grammar.query.QuerySpecification;

/**
 * The builder that builds the SQL queries having {@code UNION}, {@code INTERSECT}, or {@code EXCEPT} between the
 * {@code SELECT} statements.
 *
 * @author Stanislav Muhametsin
 * @see QueryExpressionBody
 * @see QuerySpecification
 * @see QuerySpecificationBuilder
 * @see CorrespondingSpec
 */
public interface QueryBuilder
    extends AbstractBuilder<QueryExpressionBody>
{
    /**
     * <p>
     * Adds {@code UNION} between current query and the given query. Then makes resulting query the current query.
     * </p>
     * <p>
     * This is equivalent on calling {@link #union(SetQuantifier, QueryExpressionBody)} and giving
     * {@link SetQuantifier#DISTINCT} as first parameter.
     * </p>
     *
     * @param another The query to perform {@code UNION} with.
     * @return This builder.
     */
    QueryBuilder union( QueryExpressionBody another );

    /**
     * <p>
     * Adds {@code UNION <setQuantifier>} between current query and the given query. Then makes resulting query the
     * current query.
     * </p>
     * <p>
     * This is equivalent on calling {@link #union(SetQuantifier, CorrespondingSpec, QueryExpressionBody)} and giving
     * {@code null} as second parameter.
     * </p>
     *
     * @param setQuantifier The set quantifier for this union.
     * @param another       The query to perform {@code UNION} with.
     * @return This builder.
     */
    QueryBuilder union( SetQuantifier setQuantifier, QueryExpressionBody another );

    /**
     * <p>
     * Adds {@code UNION <correspondingSpec>} between current query and the given query. Then makes resulting query the
     * current query.
     * </p>
     * <p>
     * This is equivalent on calling {@link #union(SetQuantifier, CorrespondingSpec, QueryExpressionBody)} and giving
     * {@link SetQuantifier#DISTINCT} as first parameter.
     * </p>
     *
     * @param correspondingSpec The column correspondence specification.
     * @param another           The query to perform {@code UNION} with.
     * @return This builder.
     */
    QueryBuilder union( CorrespondingSpec correspondingSpec, QueryExpressionBody another );

    /**
     * Adds {@code UNION <setQuantifier> <correspondingSpec>} between current query and the given query. Then makes
     * resulting query the current query.
     *
     * @param setQuantifier     The set quantifier for this union.
     * @param correspondingSpec The column correspondence specification. May be {@code null}.
     * @param another           The query to perform {@code UNION} with.
     * @return This builder.
     */
    QueryBuilder union( SetQuantifier setQuantifier, CorrespondingSpec correspondingSpec,
                        QueryExpressionBody another );

    /**
     * <p>
     * Adds {@code INTERSECT} between current query and the given query. Then makes resulting query the current query.
     * </p>
     * <p>
     * This is equivalent on calling {@link #intersect(SetQuantifier, QueryExpressionBody)} and giving
     * {@link SetQuantifier#DISTINCT} as first parameter.
     * </p>
     *
     * @param another The query to perform {@code INTERSECT} with.
     * @return This builder.
     */
    QueryBuilder intersect( QueryExpressionBody another );

    /**
     * <p>
     * Adds {@code INTERSECT <setQuantifier>} between current query and the given query. Then makes resulting query the
     * current query.
     * </p>
     * <p>
     * This is equivalent on calling {@link #intersect(SetQuantifier, CorrespondingSpec, QueryExpressionBody)} and
     * giving {@code null} as second parameter.
     * </p>
     *
     * @param setQuantifier The set quantifier for this union.
     * @param another       The query to perform {@code INTERSECT} with.
     * @return This builder.
     */
    QueryBuilder intersect( SetQuantifier setQuantifier, QueryExpressionBody another );

    /**
     * <p>
     * Adds {@code INTERSECT <correspondingSpec>} between current query and the given query. Then makes resulting query
     * the current query.
     * </p>
     * <p>
     * This is equivalent on calling {@link #intersect(SetQuantifier, CorrespondingSpec, QueryExpressionBody)} and
     * giving {@link SetQuantifier#DISTINCT} as first parameter.
     * </p>
     *
     * @param correspondingSpec The column correspondence specification.
     * @param another           The query to perform {@code INTERSECT} with.
     * @return This builder.
     */
    QueryBuilder intersect( CorrespondingSpec correspondingSpec, QueryExpressionBody another );

    /**
     * Adds {@code INTERSECT <setQuantifier> <correspondingSpec>} between current query and the given query. Then makes
     * resulting query the current query.
     *
     * @param setQuantifier     The set quantifier for this union.
     * @param correspondingSpec The column correspondence specification. May be {@code null}.
     * @param another           The query to perform {@code INTERSECT} with.
     * @return This builder.
     */
    QueryBuilder intersect( SetQuantifier setQuantifier, CorrespondingSpec correspondingSpec,
                            QueryExpressionBody another );

    /**
     * <p>
     * Adds {@code EXCEPT} between current query and the given query. Then makes resulting query the current query.
     * </p>
     * <p>
     * This is equivalent on calling {@link #except(SetQuantifier, QueryExpressionBody)} and giving
     * {@link SetQuantifier#DISTINCT} as first parameter.
     * </p>
     *
     * @param another The query to perform {@code EXCEPT} with.
     * @return This builder.
     */
    QueryBuilder except( SetQuantifier setQuantifier, CorrespondingSpec correspondingSpec,
                         QueryExpressionBody another );

    /**
     * <p>
     * Adds {@code EXCEPT <setQuantifier>} between current query and the given query. Then makes resulting query the
     * current query.
     * </p>
     * <p>
     * This is equivalent on calling {@link #except(SetQuantifier, CorrespondingSpec, QueryExpressionBody)} and giving
     * {@code null} as second parameter.
     * </p>
     *
     * @param setQuantifier The set quantifier for this union.
     * @param another       The query to perform {@code EXCEPT} with.
     * @return This builder.
     */
    QueryBuilder except( QueryExpressionBody another );

    /**
     * <p>
     * Adds {@code EXCEPT <correspondingSpec>} between current query and the given query. Then makes resulting query the
     * current query.
     * </p>
     * <p>
     * This is equivalent on calling {@link #except(SetQuantifier, CorrespondingSpec, QueryExpressionBody)} and giving
     * {@link SetQuantifier#DISTINCT} as first parameter.
     * </p>
     *
     * @param correspondingSpec The column correspondence specification.
     * @param another           The query to perform {@code EXCEPT} with.
     * @return This builder.
     */
    QueryBuilder except( SetQuantifier setQuantifier, QueryExpressionBody another );

    /**
     * Adds {@code EXCEPT <setQuantifier> <correspondingSpec>} between current query and the given query. Then makes
     * resulting query the current query.
     *
     * @param setQuantifier     The set quantifier for this union.
     * @param correspondingSpec The column correspondence specification. May be {@code null}.
     * @param another           The query to perform {@code EXCEPT} with.
     * @return This builder.
     */
    QueryBuilder except( CorrespondingSpec correspondingSpec, QueryExpressionBody another );
}
