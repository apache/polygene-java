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

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.builders.booleans.BooleanBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.modification.DeleteBySearchBuilder;
import org.apache.polygene.library.sql.generator.grammar.modification.DeleteBySearch;
import org.apache.polygene.library.sql.generator.grammar.modification.TargetTable;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLBuilderBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.modification.DeleteBySearchImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class DeleteBySearchBuilderImpl extends SQLBuilderBase
    implements DeleteBySearchBuilder
{

    private final BooleanBuilder _whereBuilder;

    private TargetTable _targetTable;

    public DeleteBySearchBuilderImpl( SQLProcessorAggregator processor, BooleanBuilder whereBuilder )
    {
        super( processor );
        Objects.requireNonNull( whereBuilder, "where builder" );
        this._whereBuilder = whereBuilder;
    }

    public DeleteBySearch createExpression()
    {

        return new DeleteBySearchImpl( this.getProcessor(), this._targetTable, this._whereBuilder.createExpression() );
    }

    public DeleteBySearchBuilder setTargetTable( TargetTable table )
    {
        Objects.requireNonNull( table, "table" );
        this._targetTable = table;
        return this;
    }

    public TargetTable getTargetTable()
    {
        return this._targetTable;
    }

    public BooleanBuilder getWhere()
    {
        return this._whereBuilder;
    }
}
