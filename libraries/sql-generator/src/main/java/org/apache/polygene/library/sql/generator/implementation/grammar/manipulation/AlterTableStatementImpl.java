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
package org.apache.polygene.library.sql.generator.implementation.grammar.manipulation;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.common.SchemaStatement;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterTableAction;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterTableStatement;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class AlterTableStatementImpl extends SQLSyntaxElementBase<SchemaStatement, AlterTableStatement>
    implements AlterTableStatement
{

    private final TableNameDirect _tableName;
    private final AlterTableAction _action;

    public AlterTableStatementImpl( SQLProcessorAggregator processor, TableNameDirect tableName, AlterTableAction action )
    {
        this( processor, AlterTableStatement.class, tableName, action );
    }

    protected AlterTableStatementImpl( SQLProcessorAggregator processor,
                                       Class<? extends AlterTableStatement> realImplementingType, TableNameDirect tableName, AlterTableAction action )
    {
        super( processor, realImplementingType );
        Objects.requireNonNull( tableName, "Table name" );
        Objects.requireNonNull( action, "Alter table taction" );
        this._tableName = tableName;
        this._action = action;
    }

    @Override
    protected boolean doesEqual( AlterTableStatement another )
    {
        return this._tableName.equals( another.getTableName() ) && this._action.equals( another.getAction() );
    }

    public AlterTableAction getAction()
    {
        return this._action;
    }

    public TableNameDirect getTableName()
    {
        return this._tableName;
    }
}
