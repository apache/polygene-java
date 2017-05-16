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
import org.apache.polygene.library.sql.generator.Typeable;
import org.apache.polygene.library.sql.generator.grammar.query.TableReference;
import org.apache.polygene.library.sql.generator.grammar.query.joins.JoinedTable;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.QueryExpressionBodyImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public abstract class JoinedTableImpl<TableReferenceType extends JoinedTable> extends
                                                                              QueryExpressionBodyImpl<TableReferenceType>
    implements JoinedTable
{

    private final TableReference _left;
    private final TableReference _right;

    protected JoinedTableImpl( SQLProcessorAggregator processor,
                               Class<? extends TableReferenceType> tableReferenceClass, TableReference left, TableReference right )
    {
        super( processor, tableReferenceClass );
        Objects.requireNonNull( left, "left" );
        Objects.requireNonNull( right, "right" );
        this._left = left;
        this._right = right;
    }

    public TableReference getLeft()
    {
        return this._left;
    }

    public TableReference getRight()
    {
        return this._right;
    }

    public Typeable<?> asTypeable()
    {
        return this;
    }

    @Override
    protected boolean doesEqual( TableReferenceType another )
    {
        return this._left.equals( another.getLeft() );
    }
}
