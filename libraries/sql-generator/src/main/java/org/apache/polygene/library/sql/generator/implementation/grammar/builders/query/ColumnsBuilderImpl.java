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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.builders.query.ColumnsBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.SetQuantifier;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReference;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReferences.ColumnReferenceInfo;
import org.apache.polygene.library.sql.generator.grammar.query.SelectColumnClause;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLBuilderBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.AsteriskSelectImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.ColumnReferencesImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class ColumnsBuilderImpl extends SQLBuilderBase
    implements ColumnsBuilder
{
    private final List<ColumnReferenceInfo> _columns;
    private SetQuantifier _quantifier;

    public ColumnsBuilderImpl( SQLProcessorAggregator processor, SetQuantifier setQuantifier )
    {
        super( processor );
        Objects.requireNonNull( setQuantifier, "set quantifier" );

        this._quantifier = setQuantifier;
        this._columns = new ArrayList<ColumnReferenceInfo>();
    }

    public ColumnsBuilder addUnnamedColumns( ColumnReference... columns )
    {
        for( ColumnReference col : columns )
        {
            this.addNamedColumns( new ColumnReferenceInfo( null, col ) );
        }

        return this;
    }

    public ColumnsBuilder addNamedColumns( ColumnReferenceInfo... namedColumns )
    {
        for( ColumnReferenceInfo info : namedColumns )
        {
            Objects.requireNonNull( info, "named column" );
            this._columns.add( info );
        }

        return this;
    }

    public ColumnsBuilder setSetQuantifier( SetQuantifier newSetQuantifier )
    {
        Objects.requireNonNull( newSetQuantifier, "new set quantifier" );
        this._quantifier = newSetQuantifier;

        return this;
    }

    public ColumnsBuilder selectAll()
    {
        this._columns.clear();
        return this;
    }

    public List<ColumnReferenceInfo> getColumns()
    {
        return Collections.unmodifiableList( this._columns );
    }

    public SetQuantifier getSetQuantifier()
    {
        return this._quantifier;
    }

    public SelectColumnClause createExpression()
    {
        SelectColumnClause result = null;
        if( this._columns.isEmpty() )
        {
            result = new AsteriskSelectImpl( this.getProcessor(), this._quantifier );
        }
        else
        {
            result = new ColumnReferencesImpl( this.getProcessor(), this._quantifier, this._columns );
        }

        return result;
    }
}
