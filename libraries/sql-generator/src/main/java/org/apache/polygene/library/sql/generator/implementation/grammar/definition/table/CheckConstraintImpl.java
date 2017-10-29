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
package org.apache.polygene.library.sql.generator.implementation.grammar.definition.table;

import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.definition.table.CheckConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableConstraint;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class CheckConstraintImpl extends SQLSyntaxElementBase<TableConstraint, CheckConstraint>
    implements CheckConstraint
{

    private final BooleanExpression _searchCondition;

    public CheckConstraintImpl( SQLProcessorAggregator processor, BooleanExpression searchCondition )
    {
        this( processor, CheckConstraint.class, searchCondition );
    }

    protected CheckConstraintImpl( SQLProcessorAggregator processor,
                                   Class<? extends CheckConstraint> realImplementingType, BooleanExpression searchCondition )
    {
        super( processor, realImplementingType );

        this._searchCondition = searchCondition;
    }

    @Override
    protected boolean doesEqual( CheckConstraint another )
    {
        return TypeableImpl.bothNullOrEquals( this._searchCondition, another.getCheckCondition() );
    }

    public BooleanExpression getCheckCondition()
    {
        return this._searchCondition;
    }
}
