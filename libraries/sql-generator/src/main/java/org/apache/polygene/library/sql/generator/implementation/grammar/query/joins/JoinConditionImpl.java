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
package org.apache.polygene.library.sql.generator.implementation.grammar.query.joins;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.query.joins.JoinCondition;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class JoinConditionImpl extends JoinSpecificationImpl<JoinCondition>
    implements JoinCondition
{

    private final BooleanExpression _searchCondition;

    public JoinConditionImpl( SQLProcessorAggregator processor, BooleanExpression searchCondition )
    {
        this( processor, JoinCondition.class, searchCondition );
    }

    protected JoinConditionImpl( SQLProcessorAggregator processor, Class<? extends JoinCondition> implClass,
                                 BooleanExpression searchCondition )
    {
        super( processor, implClass );
        Objects.requireNonNull( searchCondition, "search condition" );
        this._searchCondition = searchCondition;
    }

    public BooleanExpression getSearchConidition()
    {
        return this._searchCondition;
    }

    @Override
    protected boolean doesEqual( JoinCondition another )
    {
        return this._searchCondition.equals( another.getSearchConidition() );
    }
}
