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
import org.apache.polygene.library.sql.generator.Typeable;
import org.apache.polygene.library.sql.generator.grammar.common.SchemaStatement;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableCommitAction;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableContentsSource;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableScope;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class TableDefinitionImpl extends SQLSyntaxElementBase<SchemaStatement, TableDefinition>
    implements TableDefinition
{

    private final TableCommitAction _commitAction;
    private final TableContentsSource _contents;
    private final TableNameDirect _name;
    private final TableScope _scope;

    public TableDefinitionImpl( SQLProcessorAggregator processor, TableCommitAction commitAction,
                                TableContentsSource contents, TableNameDirect name, TableScope scope )
    {
        this( processor, TableDefinition.class, commitAction, contents, name, scope );
    }

    protected TableDefinitionImpl( SQLProcessorAggregator processor,
                                   Class<? extends TableDefinition> realImplementingType, TableCommitAction commitAction,
                                   TableContentsSource contents, TableNameDirect name, TableScope scope )
    {
        super( processor, realImplementingType );

        Objects.requireNonNull( name, "Table name" );
        Objects.requireNonNull( contents, "Table contents" );

        this._name = name;
        this._contents = contents;
        this._scope = scope;
        this._commitAction = commitAction;
    }

    @Override
    protected boolean doesEqual( TableDefinition another )
    {
        return this._name.equals( another.getTableName() ) && this._contents.equals( another.getContents() )
               && TypeableImpl.bothNullOrEquals( this._scope, another.getTableScope() )
               && TypeableImpl.bothNullOrEquals( this._commitAction, another.getCommitAction() );
    }

    public Typeable<?> asTypeable()
    {
        return this;
    }

    public TableCommitAction getCommitAction()
    {
        return this._commitAction;
    }

    public TableContentsSource getContents()
    {
        return this._contents;
    }

    public TableNameDirect getTableName()
    {
        return this._name;
    }

    public TableScope getTableScope()
    {
        return this._scope;
    }
}
