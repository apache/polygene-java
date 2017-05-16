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
package org.apache.polygene.library.sql.generator.implementation.grammar.modification;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.modification.DeleteBySearch;
import org.apache.polygene.library.sql.generator.grammar.modification.DeleteStatement;
import org.apache.polygene.library.sql.generator.grammar.modification.TargetTable;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class DeleteBySearchImpl extends SQLSyntaxElementBase<DeleteStatement, DeleteBySearch>
    implements DeleteBySearch
{

    private final TargetTable _targetTable;

    private final BooleanExpression _where;

    public DeleteBySearchImpl( SQLProcessorAggregator processor, TargetTable targetTable, BooleanExpression where )
    {
        this( processor, DeleteBySearch.class, targetTable, where );
    }

    protected DeleteBySearchImpl( SQLProcessorAggregator processor, Class<? extends DeleteBySearch> expressionClass,
                                  TargetTable targetTable, BooleanExpression where )
    {
        super( processor, expressionClass );
        Objects.requireNonNull( targetTable, "target table" );
        this._targetTable = targetTable;
        this._where = where;
    }

    public TargetTable getTargetTable()
    {
        return this._targetTable;
    }

    public BooleanExpression getWhere()
    {
        return this._where;
    }

    @Override
    protected boolean doesEqual( DeleteBySearch another )
    {
        return this._targetTable.equals( another.getTargetTable() )
               && TypeableImpl.bothNullOrEquals( this._where, another.getWhere() );
    }
}
