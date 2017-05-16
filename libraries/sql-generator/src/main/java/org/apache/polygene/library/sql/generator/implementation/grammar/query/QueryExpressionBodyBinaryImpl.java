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
package org.apache.polygene.library.sql.generator.implementation.grammar.query;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.common.SetQuantifier;
import org.apache.polygene.library.sql.generator.grammar.query.CorrespondingSpec;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpressionBody;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpressionBodyBinary;
import org.apache.polygene.library.sql.generator.grammar.query.SetOperation;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class QueryExpressionBodyBinaryImpl extends QueryExpressionBodyImpl<QueryExpressionBodyBinary>
    implements QueryExpressionBodyBinary
{

    private final SetOperation _setOperation;

    private final QueryExpressionBody _left;

    private final QueryExpressionBody _right;

    private final SetQuantifier _setQuantifier;

    private final CorrespondingSpec _correspondingColumns;

    public QueryExpressionBodyBinaryImpl( SQLProcessorAggregator processor, SetOperation setOperation,
                                          QueryExpressionBody left, QueryExpressionBody right, SetQuantifier setQuantifier,
                                          CorrespondingSpec correspondingColumns )
    {
        this( processor, QueryExpressionBodyBinary.class, setOperation, left, right, setQuantifier,
              correspondingColumns );
    }

    protected QueryExpressionBodyBinaryImpl( SQLProcessorAggregator processor,
                                             Class<? extends QueryExpressionBodyBinary> implClass, SetOperation setOperation, QueryExpressionBody left,
                                             QueryExpressionBody right, SetQuantifier setQuantifier, CorrespondingSpec correspondingColumns )
    {
        super( processor, implClass );
        Objects.requireNonNull( setOperation, "set operation" );
        Objects.requireNonNull( left, "left" );
        Objects.requireNonNull( right, "right" );
        Objects.requireNonNull( setQuantifier, "set quantifier" );
        this._setOperation = setOperation;
        this._left = left;
        this._right = right;
        this._correspondingColumns = correspondingColumns;
        this._setQuantifier = setQuantifier;
    }

    public QueryExpressionBody getLeft()
    {
        return this._left;
    }

    public QueryExpressionBody getRight()
    {
        return this._right;
    }

    public SetOperation getSetOperation()
    {
        return this._setOperation;
    }

    public CorrespondingSpec getCorrespondingColumns()
    {
        return this._correspondingColumns;
    }

    public SetQuantifier getSetQuantifier()
    {
        return this._setQuantifier;
    }

    @Override
    protected boolean doesEqual( QueryExpressionBodyBinary another )
    {
        return this._setOperation.equals( another.getSetOperation() )
               && this._setQuantifier.equals( another.getSetQuantifier() )
               && TypeableImpl.bothNullOrEquals( this._correspondingColumns, another.getCorrespondingColumns() )
               && this._left.equals( another.getLeft() ) && this._right.equals( another.getRight() );
    }
}
