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
import org.apache.polygene.library.sql.generator.grammar.common.NonBooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.query.OffsetSpecification;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author 2011 Stanislav Muhametsin
 */
public class OffsetSpecificationImpl extends SQLSyntaxElementBase<OffsetSpecification, OffsetSpecification>
    implements OffsetSpecification
{

    private final NonBooleanExpression _skip;

    public OffsetSpecificationImpl( SQLProcessorAggregator processor, NonBooleanExpression skip )
    {
        this( processor, OffsetSpecification.class, skip );
    }

    protected OffsetSpecificationImpl( SQLProcessorAggregator processor,
                                       Class<? extends OffsetSpecification> realImplementingType, NonBooleanExpression skip )
    {
        super( processor, realImplementingType );
        Objects.requireNonNull( skip, "Skip" );
        this._skip = skip;
    }

    public NonBooleanExpression getSkip()
    {
        return this._skip;
    }

    @Override
    protected boolean doesEqual( OffsetSpecification another )
    {
        return this._skip.equals( another.getSkip() );
    }
}
