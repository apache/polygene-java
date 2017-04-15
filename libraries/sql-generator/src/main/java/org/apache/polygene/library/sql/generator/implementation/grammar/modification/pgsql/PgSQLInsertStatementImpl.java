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
package org.apache.polygene.library.sql.generator.implementation.grammar.modification.pgsql;

import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.modification.ColumnSource;
import org.apache.polygene.library.sql.generator.grammar.modification.pgsql.PgSQLInsertStatement;
import org.apache.polygene.library.sql.generator.grammar.query.SelectColumnClause;
import org.apache.polygene.library.sql.generator.implementation.grammar.modification.InsertStatementImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

public class PgSQLInsertStatementImpl extends InsertStatementImpl implements PgSQLInsertStatement
{

    private final SelectColumnClause _returning;

    public PgSQLInsertStatementImpl( SQLProcessorAggregator processor,
                                     TableNameDirect tableName, ColumnSource columnSource, SelectColumnClause returning )
    {
        super( processor, PgSQLInsertStatement.class, tableName, columnSource );

        this._returning = returning;
    }

    public SelectColumnClause getReturningClause()
    {
        return this._returning;
    }
}
