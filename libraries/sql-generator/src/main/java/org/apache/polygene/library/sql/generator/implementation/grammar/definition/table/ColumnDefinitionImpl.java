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
package org.apache.polygene.library.sql.generator.implementation.grammar.definition.table;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLDataType;
import org.apache.polygene.library.sql.generator.grammar.definition.table.AutoGenerationPolicy;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableElement;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class ColumnDefinitionImpl extends SQLSyntaxElementBase<TableElement, ColumnDefinition>
    implements ColumnDefinition
{

    private final String _name;
    private final SQLDataType _dataType;
    private final String _default;
    private final Boolean _mayBeNull;
    private final AutoGenerationPolicy _autoGenerationPolicy;

    public ColumnDefinitionImpl( SQLProcessorAggregator processor, String name, SQLDataType dataType,
                                 String defaultStr, Boolean mayBeNull, AutoGenerationPolicy autoGenerationPolicy )
    {
        this( processor, ColumnDefinition.class, name, dataType, defaultStr, mayBeNull, autoGenerationPolicy );
    }

    protected ColumnDefinitionImpl( SQLProcessorAggregator processor,
                                    Class<? extends ColumnDefinition> realImplementingType, String name, SQLDataType dataType, String defaultStr,
                                    Boolean mayBeNull, AutoGenerationPolicy autoGenerationPolicy )
    {
        super( processor, realImplementingType );

        Objects.requireNonNull( name, "Column name" );
        Objects.requireNonNull( dataType, "Column data type" );
        Objects.requireNonNull( mayBeNull, "Null policy" );

        this._name = name;
        this._dataType = dataType;
        this._default = defaultStr;
        this._mayBeNull = mayBeNull;
        this._autoGenerationPolicy = autoGenerationPolicy;
    }

    @Override
    protected boolean doesEqual( ColumnDefinition another )
    {
        return this._name.equals( another.getColumnName() ) && this._dataType.equals( another.getDataType() )
               && TypeableImpl.bothNullOrEquals( this._default, another.getDefault() )
               && this._mayBeNull.equals( another.mayBeNull() );
    }

    public String getColumnName()
    {
        return this._name;
    }

    public SQLDataType getDataType()
    {
        return this._dataType;
    }

    public String getDefault()
    {
        return this._default;
    }

    public Boolean mayBeNull()
    {
        return this._mayBeNull;
    }

    public AutoGenerationPolicy getAutoGenerationPolicy()
    {
        return this._autoGenerationPolicy;
    }
}
