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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.query.FromClause;
import org.apache.polygene.library.sql.generator.grammar.query.TableReference;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class FromClauseImpl extends SQLSyntaxElementBase<FromClause, FromClause>
    implements FromClause
{

    private List<TableReference> _tableReferences;

    public FromClauseImpl( SQLProcessorAggregator processor, TableReference... tableReferences )
    {
        this( processor, Arrays.asList( tableReferences ) );
    }

    public FromClauseImpl( SQLProcessorAggregator processor, List<TableReference> tableReferences )
    {
        this( processor, FromClause.class, tableReferences );
    }

    protected FromClauseImpl( SQLProcessorAggregator processor, Class<? extends FromClause> type,
                              List<TableReference> tableReferences )
    {
        super( processor, type );
        Objects.requireNonNull( tableReferences, "table references" );
        for( TableReference ref : tableReferences )
        {
            Objects.requireNonNull( ref, "table reference" );
        }
        this._tableReferences = Collections.unmodifiableList( tableReferences );
    }

    public List<TableReference> getTableReferences()
    {
        return this._tableReferences;
    }

    @Override
    protected boolean doesEqual( FromClause another )
    {
        return this._tableReferences.equals( another.getTableReferences() );
    }
}
