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

import org.apache.polygene.library.sql.generator.grammar.builders.definition.TableDefinitionBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableCommitAction;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableContentsSource;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableScope;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLBuilderBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.definition.table.TableDefinitionImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class TableDefinitionBuilderImpl extends SQLBuilderBase
    implements TableDefinitionBuilder
{

    private TableScope _scope;
    private TableNameDirect _name;
    private TableCommitAction _commitAction;
    private TableContentsSource _contents;

    public TableDefinitionBuilderImpl( SQLProcessorAggregator processor )
    {
        super( processor );
    }

    public TableDefinition createExpression()
    {
        return new TableDefinitionImpl( this.getProcessor(), this._commitAction, this._contents, this._name,
                                        this._scope );
    }

    public TableDefinitionBuilder setTableScope( TableScope scope )
    {
        this._scope = scope;
        return this;
    }

    public TableDefinitionBuilder setTableName( TableNameDirect tableName )
    {
        this._name = tableName;
        return this;
    }

    public TableDefinitionBuilder setCommitAction( TableCommitAction commitAction )
    {
        this._commitAction = commitAction;
        return this;
    }

    public TableDefinitionBuilder setTableContentsSource( TableContentsSource contents )
    {
        this._contents = contents;
        return this;
    }

    public TableScope getTableScope()
    {
        return this._scope;
    }

    public TableNameDirect getTableName()
    {
        return this._name;
    }

    public TableCommitAction getCommitAction()
    {
        return this._commitAction;
    }

    public TableContentsSource getTableContentsSource()
    {
        return this._contents;
    }
}
