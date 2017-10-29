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
package org.apache.polygene.library.sql.generator.implementation.grammar.builders.modification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.builders.booleans.BooleanBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.modification.UpdateBySearchBuilder;
import org.apache.polygene.library.sql.generator.grammar.modification.SetClause;
import org.apache.polygene.library.sql.generator.grammar.modification.TargetTable;
import org.apache.polygene.library.sql.generator.grammar.modification.UpdateBySearch;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLBuilderBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.modification.UpdateBySearchImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class UpdateBySearchBuilderImpl extends SQLBuilderBase
    implements UpdateBySearchBuilder
{

    private TargetTable _targetTable;

    private final List<SetClause> _setClauses;

    private final BooleanBuilder _whereBuilder;

    public UpdateBySearchBuilderImpl( SQLProcessorAggregator processor, BooleanBuilder whereBuilder )
    {
        super( processor );
        Objects.requireNonNull( whereBuilder, "where builder" );

        this._setClauses = new ArrayList<SetClause>();
        this._whereBuilder = whereBuilder;
    }

    public UpdateBySearch createExpression()
    {
        return new UpdateBySearchImpl( this.getProcessor(), this._targetTable, this._setClauses,
                                       this._whereBuilder.createExpression() );
    }

    public UpdateBySearchBuilder setTargetTable( TargetTable table )
    {
        this._targetTable = table;
        return this;
    }

    public BooleanBuilder getWhereBuilder()
    {
        return this._whereBuilder;
    }

    public UpdateBySearchBuilder addSetClauses( SetClause... clauses )
    {
        for( SetClause clause : clauses )
        {
            this._setClauses.add( clause );
        }
        return this;
    }

    public List<SetClause> getSetClauses()
    {
        return Collections.unmodifiableList( this._setClauses );
    }
}
