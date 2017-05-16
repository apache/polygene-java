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
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterColumnAction;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterTableAction;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class AlterColumnDefinitionImpl extends SQLSyntaxElementBase<AlterTableAction, AlterColumnDefinition>
    implements AlterColumnDefinition
{

    private final String _columnName;
    private final AlterColumnAction _action;

    public AlterColumnDefinitionImpl( SQLProcessorAggregator processor, String columnName, AlterColumnAction action )
    {
        this( processor, AlterColumnDefinition.class, columnName, action );
    }

    protected AlterColumnDefinitionImpl( SQLProcessorAggregator processor,
                                         Class<? extends AlterColumnDefinition> realImplementingType, String columnName, AlterColumnAction action )
    {
        super( processor, realImplementingType );
        Objects.requireNonNull( columnName, "Column name" );
        Objects.requireNonNull( action, "Alter column action" );
        this._columnName = columnName;
        this._action = action;
    }

    @Override
    protected boolean doesEqual( AlterColumnDefinition another )
    {
        return this._columnName.equals( another.getColumnName() ) && this._action.equals( another.getAction() );
    }

    public AlterColumnAction getAction()
    {
        return this._action;
    }

    public String getColumnName()
    {
        return this._columnName;
    }
}
