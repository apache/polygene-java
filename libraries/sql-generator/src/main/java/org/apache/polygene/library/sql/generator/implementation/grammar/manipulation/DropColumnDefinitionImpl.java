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
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterTableAction;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropBehaviour;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropColumnDefinition;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class DropColumnDefinitionImpl extends SQLSyntaxElementBase<AlterTableAction, DropColumnDefinition>
    implements DropColumnDefinition
{

    private final String _columnName;
    private final DropBehaviour _dropBehaviour;

    public DropColumnDefinitionImpl( SQLProcessorAggregator processor, String columnName, DropBehaviour dropBehaviour )
    {
        this( processor, DropColumnDefinition.class, columnName, dropBehaviour );
    }

    protected DropColumnDefinitionImpl( SQLProcessorAggregator processor,
                                        Class<? extends DropColumnDefinition> realImplementingType, String columnName, DropBehaviour dropBehaviour )
    {
        super( processor, realImplementingType );
        Objects.requireNonNull( columnName, "Column name" );
        Objects.requireNonNull( dropBehaviour, "Drop behaviour" );
        this._columnName = columnName;
        this._dropBehaviour = dropBehaviour;
    }

    @Override
    protected boolean doesEqual( DropColumnDefinition another )
    {
        return this._dropBehaviour.equals( another.getDropBehaviour() )
               && this._columnName.equals( another.getColumnName() );
    }

    public String getColumnName()
    {
        return this._columnName;
    }

    public DropBehaviour getDropBehaviour()
    {
        return this._dropBehaviour;
    }
}
