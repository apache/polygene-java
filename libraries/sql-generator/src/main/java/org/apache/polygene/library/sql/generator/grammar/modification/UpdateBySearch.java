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
package org.apache.polygene.library.sql.generator.grammar.modification;

import java.util.List;
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;

/**
 * This syntax element represents {@code UPDATE} statement.
 *
 *
 */
public interface UpdateBySearch
    extends UpdateStatement
{

    /**
     * Returns the target table of this {@code UPDATE} statement.
     *
     * @return The target table of this {@code UPDATE} statement.
     * @see TargetTable
     */
    TargetTable getTargetTable();

    /**
     * Returns the search condition for the rows to be updated.
     *
     * @return The search condition for the rows to be updated.
     */
    BooleanExpression getWhere();

    /**
     * Returns the {@code SET} clauses for columns of the target table.
     *
     * @return The {@code SET} clauses for columns of the target table.
     * @see SetClause
     */
    List<SetClause> getSetClauses();
}
