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
package org.apache.polygene.library.sql.generator.grammar.modification.pgsql;

import org.apache.polygene.library.sql.generator.grammar.modification.InsertStatement;
import org.apache.polygene.library.sql.generator.grammar.query.SelectColumnClause;

/**
 * <p>
 * This interface represents {@code INSERT} statement with PostgreSQL-specific elements.
 * </p>
 * <p>
 * <p>
 * TODO: {@code WITH RECURSIVE} query
 * </p>
 *
 * @author Stanislav Muhametsin
 */
public interface PgSQLInsertStatement extends InsertStatement
{
    /**
     * Gets the columns for the {@code RETURNING} clause. Returns {@code null} if this
     * {@code INSERT} statement has no {@code RETURNING} clause.
     *
     * @return The {@code RETURNING} clause for this statement, or {@code null} if none.
     */
    SelectColumnClause getReturningClause();
}
