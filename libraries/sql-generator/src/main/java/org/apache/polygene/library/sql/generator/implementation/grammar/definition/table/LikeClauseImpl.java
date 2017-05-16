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
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.definition.table.LikeClause;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableElement;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class LikeClauseImpl extends SQLSyntaxElementBase<TableElement, LikeClause>
    implements LikeClause
{

    private final TableNameDirect _tableName;

    public LikeClauseImpl( SQLProcessorAggregator processor, TableNameDirect tableName )
    {
        this( processor, LikeClause.class, tableName );
    }

    protected LikeClauseImpl( SQLProcessorAggregator processor, Class<? extends LikeClause> realImplementingType,
                              TableNameDirect tableName )
    {
        super( processor, realImplementingType );

        Objects.requireNonNull( tableName, "Table name" );

        this._tableName = tableName;
    }

    @Override
    protected boolean doesEqual( LikeClause another )
    {
        return this._tableName.equals( another.getTableName() );
    }

    public TableNameDirect getTableName()
    {
        return this._tableName;
    }
}
