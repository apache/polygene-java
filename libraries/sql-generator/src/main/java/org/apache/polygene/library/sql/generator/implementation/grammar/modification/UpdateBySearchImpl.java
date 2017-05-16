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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.modification.SetClause;
import org.apache.polygene.library.sql.generator.grammar.modification.TargetTable;
import org.apache.polygene.library.sql.generator.grammar.modification.UpdateBySearch;
import org.apache.polygene.library.sql.generator.grammar.modification.UpdateStatement;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class UpdateBySearchImpl extends SQLSyntaxElementBase<UpdateStatement, UpdateBySearch>
    implements UpdateBySearch
{

    private final TargetTable _targetTable;

    private final List<SetClause> _setClauses;

    private final BooleanExpression _where;

    public UpdateBySearchImpl( SQLProcessorAggregator processor, TargetTable targetTable,
                               List<SetClause> setClauses,
                               BooleanExpression where )
    {
        this( processor, UpdateBySearch.class, targetTable, setClauses, where );
    }

    protected UpdateBySearchImpl( SQLProcessorAggregator processor,
                                  Class<? extends UpdateBySearch> expressionClass,
                                  TargetTable targetTable, List<SetClause> setClauses, BooleanExpression where )
    {
        super( processor, expressionClass );
        Objects.requireNonNull( targetTable, "target table" );
        Objects.requireNonNull( setClauses, "set clauses" );
        if( setClauses.isEmpty() )
        {
            throw new IllegalArgumentException( "At least one set clause must be present." );
        }
        for( SetClause clause : setClauses )
        {
            Objects.requireNonNull( clause, "set clause" );
        }

        this._targetTable = targetTable;
        this._setClauses = Collections.unmodifiableList( new ArrayList<SetClause>( setClauses ) );
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

    public List<SetClause> getSetClauses()
    {
        return this._setClauses;
    }

    @Override
    protected boolean doesEqual( UpdateBySearch another )
    {
        return this._targetTable.equals( another.getTargetTable() )
               && this._setClauses.equals( another.getSetClauses() )
               && TypeableImpl.bothNullOrEquals( this._where, another.getWhere() );
    }
}
