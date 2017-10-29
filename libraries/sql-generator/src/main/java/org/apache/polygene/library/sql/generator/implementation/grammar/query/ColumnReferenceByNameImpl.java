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

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReferenceByName;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class ColumnReferenceByNameImpl extends ColumnReferenceImpl<ColumnReferenceByName>
    implements ColumnReferenceByName
{

    private final String _tableName;

    private final String _columnName;

    public ColumnReferenceByNameImpl( SQLProcessorAggregator processor, String tableName, String columnName )
    {
        this( processor, ColumnReferenceByName.class, tableName, columnName );
    }

    protected ColumnReferenceByNameImpl( SQLProcessorAggregator processor,
                                         Class<? extends ColumnReferenceByName> implClass, String tableName, String columnName )
    {
        super( processor, implClass );
        Objects.requireNonNull( columnName, "column name" );
        this._tableName = tableName;
        this._columnName = columnName;
    }

    public String getColumnName()
    {
        return this._columnName;
    }

    public String getTableName()
    {
        return this._tableName;
    }

    @Override
    protected boolean doesEqual( ColumnReferenceByName another )
    {
        return this._columnName.equals( another.getColumnName() )
               && TypeableImpl.bothNullOrEquals( this._tableName, another.getTableName() );
    }
}
