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
package org.apache.polygene.library.sql.generator.implementation.grammar.builders.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.builders.query.OrderByBuilder;
import org.apache.polygene.library.sql.generator.grammar.query.OrderByClause;
import org.apache.polygene.library.sql.generator.grammar.query.SortSpecification;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLBuilderBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.OrderByClauseImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class OrderByBuilderImpl extends SQLBuilderBase
    implements OrderByBuilder
{

    private final List<SortSpecification> _sortSpecs;

    public OrderByBuilderImpl( SQLProcessorAggregator processor )
    {
        super( processor );
        this._sortSpecs = new ArrayList<SortSpecification>();
    }

    public OrderByBuilder addSortSpecs( SortSpecification... specs )
    {
        for( SortSpecification spec : specs )
        {
            Objects.requireNonNull( spec, "specification" );
        }

        this._sortSpecs.addAll( Arrays.asList( specs ) );
        return this;
    }

    public List<SortSpecification> getSortSpecs()
    {
        return Collections.unmodifiableList( this._sortSpecs );
    }

    public OrderByClause createExpression()
    {
        return new OrderByClauseImpl( this.getProcessor(), this._sortSpecs );
    }
}
