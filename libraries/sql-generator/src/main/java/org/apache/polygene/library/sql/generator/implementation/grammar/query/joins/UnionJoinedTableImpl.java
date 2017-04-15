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

import org.apache.polygene.library.sql.generator.grammar.query.TableReference;
import org.apache.polygene.library.sql.generator.grammar.query.joins.UnionJoinedTable;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class UnionJoinedTableImpl extends JoinedTableImpl<UnionJoinedTable>
    implements UnionJoinedTable
{

    public UnionJoinedTableImpl( SQLProcessorAggregator processor, TableReference left, TableReference right )
    {
        this( processor, UnionJoinedTable.class, left, right );
    }

    protected UnionJoinedTableImpl( SQLProcessorAggregator processor, Class<? extends UnionJoinedTable> implClass,
                                    TableReference left, TableReference right )
    {
        super( processor, implClass, left, right );
    }

    @Override
    protected boolean doesEqual( UnionJoinedTable another )
    {
        boolean result = this.getRight().equals( another.getRight() );
        if( result )
        {
            result = super.doesEqual( another );
        }

        return result;
    }
}
