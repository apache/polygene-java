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
package org.apache.polygene.library.sql.generator.implementation.grammar.query.joins;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.query.TableReference;
import org.apache.polygene.library.sql.generator.grammar.query.joins.JoinSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.joins.JoinType;
import org.apache.polygene.library.sql.generator.grammar.query.joins.QualifiedJoinedTable;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class QualifiedJoinedTableImpl extends JoinedTableImpl<QualifiedJoinedTable>
    implements QualifiedJoinedTable
{

    private final JoinType _joinType;

    private final JoinSpecification _joinSpec;

    public QualifiedJoinedTableImpl( SQLProcessorAggregator processor, TableReference left, TableReference right,
                                     JoinType joinType, JoinSpecification joinSpec )
    {
        this( processor, QualifiedJoinedTable.class, left, right, joinType, joinSpec );
    }

    protected QualifiedJoinedTableImpl( SQLProcessorAggregator processor,
                                        Class<? extends QualifiedJoinedTable> implClass, TableReference left, TableReference right, JoinType joinType,
                                        JoinSpecification joinSpec )
    {
        super( processor, implClass, left, right );
        Objects.requireNonNull( joinType, "join type" );
        Objects.requireNonNull( joinSpec, "join specification" );
        this._joinType = joinType;
        this._joinSpec = joinSpec;
    }

    public JoinType getJoinType()
    {
        return this._joinType;
    }

    public JoinSpecification getJoinSpecification()
    {
        return this._joinSpec;
    }

    @Override
    protected boolean doesEqual( QualifiedJoinedTable another )
    {
        boolean result = this._joinType.equals( another.getJoinType() )
                         && this._joinSpec.equals( another.getJoinSpecification() ) && this.getRight().equals( another.getRight() );
        if( result )
        {
            result = super.doesEqual( another );
        }
        return result;
    }
}
