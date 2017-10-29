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
import org.apache.polygene.library.sql.generator.grammar.query.GroupByClause;
import org.apache.polygene.library.sql.generator.grammar.query.GroupingElement;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class GroupByClauseImpl extends SQLSyntaxElementBase<GroupByClause, GroupByClause>
    implements GroupByClause
{

    private final List<GroupingElement> _groupingElements;

    public GroupByClauseImpl( SQLProcessorAggregator processor, List<GroupingElement> groupingElements )
    {
        super( processor, GroupByClause.class );
        Objects.requireNonNull( groupingElements, "grouping elements" );
        for( GroupingElement element : groupingElements )
        {
            Objects.requireNonNull( element, "grouping element" );
        }
        this._groupingElements = Collections.unmodifiableList( new ArrayList<GroupingElement>( groupingElements ) );
    }

    public List<GroupingElement> getGroupingElements()
    {
        return this._groupingElements;
    }

    @Override
    protected boolean doesEqual( GroupByClause another )
    {
        return this._groupingElements.equals( another.getGroupingElements() );
    }
}
