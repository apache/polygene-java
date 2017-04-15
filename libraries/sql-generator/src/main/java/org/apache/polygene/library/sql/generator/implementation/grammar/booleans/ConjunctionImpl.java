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
package org.apache.polygene.library.sql.generator.implementation.grammar.booleans;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.booleans.Conjunction;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class ConjunctionImpl extends ComposedBooleanExpressionImpl<Conjunction>
    implements Conjunction
{

    private final BooleanExpression _left;

    private final BooleanExpression _right;

    public ConjunctionImpl( SQLProcessorAggregator processor, BooleanExpression left, BooleanExpression right )
    {
        this( processor, Conjunction.class, left, right );
    }

    protected ConjunctionImpl( SQLProcessorAggregator processor, Class<? extends Conjunction> expressionClass,
                               BooleanExpression left, BooleanExpression right )
    {
        super( processor, expressionClass );
        Objects.requireNonNull( left, "left" );
        Objects.requireNonNull( right, "right" );

        this._left = left;
        this._right = right;
    }

    public BooleanExpression getLeft()
    {
        return this._left;
    }

    public BooleanExpression getRight()
    {
        return this._right;
    }

    public Iterator<BooleanExpression> iterator()
    {
        return Arrays.asList( this._left, this._right ).iterator();
    }
}
