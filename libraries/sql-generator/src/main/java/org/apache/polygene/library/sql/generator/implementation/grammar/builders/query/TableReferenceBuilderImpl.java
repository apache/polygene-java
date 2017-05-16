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
package org.apache.polygene.library.sql.generator.implementation.grammar.builders.query;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.builders.query.TableReferenceBuilder;
import org.apache.polygene.library.sql.generator.grammar.query.TableReference;
import org.apache.polygene.library.sql.generator.grammar.query.TableReferencePrimary;
import org.apache.polygene.library.sql.generator.grammar.query.joins.JoinSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.joins.JoinType;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLBuilderBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.joins.CrossJoinedTableImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.joins.NaturalJoinedTableImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.joins.QualifiedJoinedTableImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.joins.UnionJoinedTableImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

public class TableReferenceBuilderImpl extends SQLBuilderBase
    implements TableReferenceBuilder
{

    private TableReference _currentTable;

    public TableReferenceBuilderImpl( SQLProcessorAggregator processor, TableReferencePrimary startingTable )
    {
        super( processor );
        Objects.requireNonNull( startingTable, "starting table" );

        this._currentTable = startingTable;
    }

    public TableReferenceBuilder addQualifiedJoin( JoinType joinType, TableReference right, JoinSpecification joinSpec )
    {
        this._currentTable = new QualifiedJoinedTableImpl( this.getProcessor(), this._currentTable, right, joinType,
                                                           joinSpec );
        return this;
    }

    public TableReferenceBuilder addCrossJoin( TableReference right )
    {
        this._currentTable = new CrossJoinedTableImpl( this.getProcessor(), this._currentTable, right );
        return this;
    }

    public TableReferenceBuilder addNaturalJoin( JoinType joinType, TableReference right )
    {
        this._currentTable = new NaturalJoinedTableImpl( this.getProcessor(), this._currentTable, right, joinType );
        return this;
    }

    public TableReferenceBuilder addUnionJoin( TableReference right )
    {
        this._currentTable = new UnionJoinedTableImpl( this.getProcessor(), this._currentTable, right );
        return this;
    }

    public TableReference createExpression()
    {
        return this._currentTable;
    }
}