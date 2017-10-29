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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.query.RowValueConstructor;
import org.apache.polygene.library.sql.generator.grammar.query.TableValueConstructor;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class TableValueConstructorImpl extends QueryExpressionBodyImpl<TableValueConstructor>
    implements TableValueConstructor
{

    private final List<RowValueConstructor> _rows;

    public TableValueConstructorImpl( SQLProcessorAggregator processor, List<RowValueConstructor> rows )
    {
        this( processor, TableValueConstructor.class, rows );
    }

    protected TableValueConstructorImpl( SQLProcessorAggregator processor,
                                         Class<? extends TableValueConstructor> expressionClass, List<RowValueConstructor> rows )
    {
        super( processor, expressionClass );
        Objects.requireNonNull( rows, "rows" );
        this._rows = Collections.unmodifiableList( rows );
    }

    public List<RowValueConstructor> getRows()
    {
        return this._rows;
    }

    @Override
    protected boolean doesEqual( TableValueConstructor other )
    {
        return this._rows.equals( other.getRows() );
    }
}
