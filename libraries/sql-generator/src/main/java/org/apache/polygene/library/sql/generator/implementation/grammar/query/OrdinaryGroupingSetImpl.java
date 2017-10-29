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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.common.NonBooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.query.GroupingElement;
import org.apache.polygene.library.sql.generator.grammar.query.OrdinaryGroupingSet;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class OrdinaryGroupingSetImpl extends SQLSyntaxElementBase<GroupingElement, OrdinaryGroupingSet>
    implements OrdinaryGroupingSet
{
    private final List<NonBooleanExpression> _columns;

    public OrdinaryGroupingSetImpl( SQLProcessorAggregator processor, NonBooleanExpression... columns )
    {
        this( processor, Arrays.asList( columns ) );
    }

    public OrdinaryGroupingSetImpl( SQLProcessorAggregator processor, List<NonBooleanExpression> columns )
    {
        this( processor, OrdinaryGroupingSet.class, columns );
    }

    protected OrdinaryGroupingSetImpl( SQLProcessorAggregator processor,
                                       Class<? extends OrdinaryGroupingSet> implClass, List<NonBooleanExpression> columns )
    {
        super( processor, implClass );
        Objects.requireNonNull( columns, "columns" );
        for( NonBooleanExpression expr : columns )
        {
            Objects.requireNonNull( expr, "column" );
        }
        this._columns = Collections.unmodifiableList( new ArrayList<NonBooleanExpression>( columns ) );
    }

    public List<NonBooleanExpression> getColumns()
    {
        return this._columns;
    }

    @Override
    protected boolean doesEqual( OrdinaryGroupingSet another )
    {
        return this._columns.equals( another.getColumns() );
    }
}
