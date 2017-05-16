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
import org.apache.polygene.library.sql.generator.grammar.booleans.Negation;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class NegationImpl extends ComposedBooleanExpressionImpl<Negation>
    implements Negation
{

    private final BooleanExpression _negated;

    public NegationImpl( SQLProcessorAggregator processor, BooleanExpression negated )
    {
        this( processor, Negation.class, negated );
    }

    protected NegationImpl( SQLProcessorAggregator processor, Class<? extends Negation> negationClass,
                            BooleanExpression negated )
    {
        super( processor, negationClass );
        Objects.requireNonNull( negated, "negated" );

        this._negated = negated;
    }

    public BooleanExpression getNegated()
    {
        return this._negated;
    }

    public Iterator<BooleanExpression> iterator()
    {
        return Arrays.asList( this._negated ).iterator();
    }
}
