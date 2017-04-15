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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.query.OrderByClause;
import org.apache.polygene.library.sql.generator.grammar.query.SortSpecification;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class OrderByClauseImpl extends SQLSyntaxElementBase<OrderByClause, OrderByClause>
    implements OrderByClause
{

    private List<SortSpecification> _sortSpecs;

    public OrderByClauseImpl( SQLProcessorAggregator processor, List<SortSpecification> sortSpecs )
    {
        super( processor, OrderByClause.class );
        Objects.requireNonNull( sortSpecs, "sort specifications" );
        for( SortSpecification sortSpec : sortSpecs )
        {
            Objects.requireNonNull( sortSpec, "sort specification" );
        }

        this._sortSpecs = Collections.unmodifiableList( new ArrayList<SortSpecification>( sortSpecs ) );
    }

    public List<SortSpecification> getOrderingColumns()
    {
        return this._sortSpecs;
    }

    @Override
    protected boolean doesEqual( OrderByClause another )
    {
        return this._sortSpecs.equals( another.getOrderingColumns() );
    }
}
