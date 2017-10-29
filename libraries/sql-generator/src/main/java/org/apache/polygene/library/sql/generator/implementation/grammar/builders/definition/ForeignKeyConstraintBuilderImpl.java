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
package org.apache.polygene.library.sql.generator.implementation.grammar.builders.definition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.builders.definition.ForeignKeyConstraintBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ForeignKeyConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.MatchType;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ReferentialAction;
import org.apache.polygene.library.sql.generator.grammar.factories.ColumnsFactory;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLBuilderBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.definition.table.ForeignKeyConstraintImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class ForeignKeyConstraintBuilderImpl extends SQLBuilderBase
    implements ForeignKeyConstraintBuilder
{

    private final List<String> _sourceColumns;
    private final List<String> _targetColumns;
    private TableNameDirect _targetTable;
    private MatchType _matchType;
    private ReferentialAction _onUpdate;
    private ReferentialAction _onDelete;

    private final ColumnsFactory _c;

    public ForeignKeyConstraintBuilderImpl( SQLProcessorAggregator processor, ColumnsFactory c )
    {
        super( processor );
        Objects.requireNonNull( c, "Columns factory" );

        this._c = c;

        this._sourceColumns = new ArrayList<String>();
        this._targetColumns = new ArrayList<String>();
    }

    public ForeignKeyConstraint createExpression()
    {
        return new ForeignKeyConstraintImpl( this.getProcessor(), this._c.colNames( this._sourceColumns ),
                                             this._targetTable, this._targetColumns.size() == 0 ? null : this._c.colNames( this._targetColumns ),
                                             this._matchType, this._onDelete, this._onUpdate );
    }

    public ForeignKeyConstraintBuilder addSourceColumns( String... columnNames )
    {
        for( String name : columnNames )
        {
            this._sourceColumns.add( name );
        }
        return this;
    }

    public ForeignKeyConstraintBuilder addTargetColumns( String... columnNames )
    {
        for( String name : columnNames )
        {
            this._targetColumns.add( name );
        }
        return this;
    }

    public ForeignKeyConstraintBuilder setTargetTableName( TableNameDirect tableName )
    {
        this._targetTable = tableName;
        return this;
    }

    public ForeignKeyConstraintBuilder setMatchType( MatchType matchType )
    {
        this._matchType = matchType;
        return this;
    }

    public ForeignKeyConstraintBuilder setOnUpdate( ReferentialAction action )
    {
        this._onUpdate = action;
        return this;
    }

    public ForeignKeyConstraintBuilder setOnDelete( ReferentialAction action )
    {
        this._onDelete = action;
        return this;
    }

    public List<String> getSourceColumns()
    {
        return this._sourceColumns;
    }

    public List<String> getTargetColumns()
    {
        return this._targetColumns;
    }

    public TableNameDirect getTableName()
    {
        return this._targetTable;
    }

    public MatchType getMatchType()
    {
        return this._matchType;
    }

    public ReferentialAction getOnUpdate()
    {
        return this._onUpdate;
    }

    public ReferentialAction getOnDelete()
    {
        return this._onDelete;
    }
}
