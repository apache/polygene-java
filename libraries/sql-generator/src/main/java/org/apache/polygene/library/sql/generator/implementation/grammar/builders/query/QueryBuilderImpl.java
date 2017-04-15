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
package org.apache.polygene.library.sql.generator.implementation.grammar.builders.query;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.builders.query.QueryBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.SetQuantifier;
import org.apache.polygene.library.sql.generator.grammar.query.CorrespondingSpec;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpressionBody;
import org.apache.polygene.library.sql.generator.grammar.query.SetOperation;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLBuilderBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.QueryExpressionBodyBinaryImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class QueryBuilderImpl extends SQLBuilderBase
    implements QueryBuilder
{

    public static final SetQuantifier DEFAULT_SET_QUANTIFIER_FOR_UNIONS = SetQuantifier.DISTINCT;

    public static final SetQuantifier DEFAULT_SET_QUANTIFIER_FOR_INTERSECTIONS = SetQuantifier.DISTINCT;

    public static final SetQuantifier DEFAULT_SET_QUANTIFIER_FOR_EXCEPTS = SetQuantifier.DISTINCT;

    private final SetQuantifier _defaultSetQuantifierForUnions;

    private final SetQuantifier _defaultSetQuantifierForIntersections;

    private final SetQuantifier _defaultSetQuantifierForExcepts;

    private QueryExpressionBody _topLevelExpression;

    public QueryBuilderImpl( SQLProcessorAggregator processor, QueryExpressionBody topLevelExpression )
    {
        this( processor, topLevelExpression, DEFAULT_SET_QUANTIFIER_FOR_UNIONS,
              DEFAULT_SET_QUANTIFIER_FOR_INTERSECTIONS, DEFAULT_SET_QUANTIFIER_FOR_EXCEPTS );
    }

    protected QueryBuilderImpl( SQLProcessorAggregator processor, QueryExpressionBody topLevelExpression,
                                SetQuantifier defaultSetQuantifierForUnions, SetQuantifier defaultSetQuantifierForIntersections,
                                SetQuantifier defaultSetQuantifierForExcepts )
    {
        super( processor );
        Objects.requireNonNull( defaultSetQuantifierForUnions, "default quantifier for unions" );
        Objects.requireNonNull( defaultSetQuantifierForIntersections, "default quantifier for intersections" );
        Objects.requireNonNull( defaultSetQuantifierForExcepts, "default quantifier for excepts" );
        Objects.requireNonNull( topLevelExpression, "top level expression" );

        this._defaultSetQuantifierForExcepts = defaultSetQuantifierForExcepts;
        this._defaultSetQuantifierForIntersections = defaultSetQuantifierForIntersections;
        this._defaultSetQuantifierForUnions = defaultSetQuantifierForUnions;
        this._topLevelExpression = topLevelExpression;
    }

    public QueryBuilder union( QueryExpressionBody another )
    {
        return this.union( this._defaultSetQuantifierForUnions, another );
    }

    public QueryBuilder union( CorrespondingSpec correspondingSpec, QueryExpressionBody another )
    {
        return this.union( this._defaultSetQuantifierForUnions, correspondingSpec, another );
    }

    public QueryBuilder union( SetQuantifier setQuantifier, QueryExpressionBody another )
    {
        return this.union( setQuantifier, null, another );
    }

    public QueryBuilder union( SetQuantifier setQuantifier, CorrespondingSpec correspondingSpec,
                               QueryExpressionBody another )
    {
        this._topLevelExpression = new QueryExpressionBodyBinaryImpl( this.getProcessor(), SetOperation.UNION,
                                                                      this._topLevelExpression, another, setQuantifier, correspondingSpec );
        return this;
    }

    public QueryBuilder intersect( QueryExpressionBody another )
    {
        return this.intersect( this._defaultSetQuantifierForIntersections, another );
    }

    public QueryBuilder intersect( CorrespondingSpec correspondingSpec, QueryExpressionBody another )
    {
        return this.intersect( this._defaultSetQuantifierForIntersections, correspondingSpec, another );
    }

    public QueryBuilder intersect( SetQuantifier setQuantifier, QueryExpressionBody another )
    {
        return this.intersect( setQuantifier, null, another );
    }

    public QueryBuilder intersect( SetQuantifier setQuantifier, CorrespondingSpec correspondingSpec,
                                   QueryExpressionBody another )
    {
        this._topLevelExpression = new QueryExpressionBodyBinaryImpl( this.getProcessor(), SetOperation.INTERSECT,
                                                                      this._topLevelExpression, another, setQuantifier, correspondingSpec );
        return this;
    }

    public QueryBuilder except( QueryExpressionBody another )
    {
        return this.except( this._defaultSetQuantifierForExcepts, another );
    }

    public QueryBuilder except( CorrespondingSpec correspondingSpec, QueryExpressionBody another )
    {
        return this.except( this._defaultSetQuantifierForExcepts, correspondingSpec, another );
    }

    public QueryBuilder except( SetQuantifier setQuantifier, QueryExpressionBody another )
    {
        return this.except( setQuantifier, null, another );
    }

    public QueryBuilder except( SetQuantifier setQuantifier, CorrespondingSpec correspondingSpec,
                                QueryExpressionBody another )
    {
        this._topLevelExpression = new QueryExpressionBodyBinaryImpl( this.getProcessor(), SetOperation.EXCEPT,
                                                                      this._topLevelExpression, another, setQuantifier, correspondingSpec );
        return this;
    }

    public QueryExpressionBody createExpression()
    {
        return this._topLevelExpression;
    }
}
