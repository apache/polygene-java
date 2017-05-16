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
package org.apache.polygene.library.sql.generator.grammar.builders.modification.pgsql;

import org.apache.polygene.library.sql.generator.grammar.builders.modification.InsertStatementBuilder;
import org.apache.polygene.library.sql.generator.grammar.query.SelectColumnClause;

/**
 * This build is used for creating {@code INSERT} statements specific for PostgreSQL databases.
 *
 * @author Stanislav Muhametsin
 */
public interface PgSQLInsertStatementBuilder extends InsertStatementBuilder
{

    /**
     * Sets the {@code RETURNING} clause for this {@code INSERT} statement.
     *
     * @param clause The {@code RETURNING} clause. May be {@code null} if no {@code RETURNING}
     *               clause is desired.
     * @return This builder.
     */
    PgSQLInsertStatementBuilder setReturningClause( SelectColumnClause clause );
}
