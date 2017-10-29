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
package org.apache.polygene.library.sql.generator.implementation.grammar.modification;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.modification.TargetTable;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class TargetTableImpl extends SQLSyntaxElementBase<TargetTable, TargetTable>
    implements TargetTable
{

    private Boolean _isOnly;

    private TableNameDirect _tableName;

    public TargetTableImpl( SQLProcessorAggregator processor, Boolean isOnly, TableNameDirect tableName )
    {
        this( processor, TargetTable.class, isOnly, tableName );
    }

    protected TargetTableImpl( SQLProcessorAggregator processor, Class<? extends TargetTable> expressionClass,
                               Boolean isOnly, TableNameDirect tableName )
    {
        super( processor, expressionClass );
        Objects.requireNonNull( tableName, "table name" );
        if( isOnly == null )
        {
            isOnly = false;
        }
        this._tableName = tableName;
        this._isOnly = isOnly;
    }

    public Boolean isOnly()
    {
        return this._isOnly;
    }

    public TableNameDirect getTableName()
    {
        return this._tableName;
    }

    @Override
    protected boolean doesEqual( TargetTable another )
    {
        return this._tableName.equals( another.getTableName() ) && this._isOnly.equals( another.isOnly() );
    }
}
