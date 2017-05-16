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
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropBehaviour;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropTableOrViewStatement;
import org.apache.polygene.library.sql.generator.grammar.manipulation.ObjectType;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class DropTableOrViewStatementImpl extends DropStatementImpl<DropTableOrViewStatement>
    implements DropTableOrViewStatement
{

    private final TableNameDirect _name;

    public DropTableOrViewStatementImpl( SQLProcessorAggregator processor, ObjectType whatToDrop,
                                         DropBehaviour dropBehaviour, TableNameDirect name )
    {
        this( processor, DropTableOrViewStatement.class, whatToDrop, dropBehaviour, name );
    }

    protected DropTableOrViewStatementImpl( SQLProcessorAggregator processor,
                                            Class<? extends DropTableOrViewStatement> realImplementingType, ObjectType whatToDrop,
                                            DropBehaviour dropBehaviour, TableNameDirect name )
    {
        super( processor, realImplementingType, whatToDrop, dropBehaviour );
        Objects.requireNonNull( name, "Table name" );
        this._name = name;
    }

    @Override
    protected boolean doesEqual( DropTableOrViewStatement another )
    {
        return this._name.equals( another.getTableName() ) && super.doesEqual( another );
    }

    public TableNameDirect getTableName()
    {
        return this._name;
    }
}
