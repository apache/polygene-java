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
package org.apache.polygene.library.sql.generator.vendor;

import org.apache.polygene.library.sql.generator.grammar.factories.pgsql.PgSQLDataTypeFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.pgsql.PgSQLManipulationFactory;

/**
 * This is vendor for PostgreSQL database. PostgreSQL provides some extra SQL syntax elements for queries (notably
 * {@code LIMIT} and {@code OFFSET} clauses), and this vendor gives access to factory, which enables the creation of
 * these elements.
 *
 * @author Stanislav Muhametsin
 */
public interface PostgreSQLVendor
    extends SQLVendor
{
    /**
     * Returns the data type factory, which knows to create PostgreSQL-specific data types as well as pre-defined
     * standard ones.
     */
    PgSQLDataTypeFactory getDataTypeFactory();

    /**
     * Returns the manipulation factory, which knows to create PostgreSQL-specific data manipulation statements.
     */
    PgSQLManipulationFactory getManipulationFactory();

    /**
     * Returns whether the legacy (pre-8.4) OFFSET/LIMIT expressions are used instead of the OFFSET/FETCH defined in the
     * SQL 2008 standard. For more information, see http://www.postgresql.org/docs/8.3/static/sql-select.html#SQL-LIMIT
     * . This method is not thread-safe.
     *
     * @return {@code true} if the legacy OFFSET/LIMIT expressions are used; {@code false} otherwise.
     */
    boolean legacyOffsetAndLimit();

    /**
     * Sets the switch to use legacy LIMIT/OFFSET expressions instead of the OFFSET/FETCH expressions of the SQL 2008
     * standard. This is necessary only for pre-8.4 PgSQL databases. For more information, see
     * http://www.postgresql.org/docs/8.3/static/sql-select.html#SQL-LIMIT . This method is not thread-safe.
     *
     * @param useLegacyOffsetAndLimit Whether to use legacy LIMIT/OFFSET expressions instead of the OFFSET/FETCH
     *                                expressions defined in the SQL 2008 standard.
     */
    void setLegacyOffsetAndLimit( boolean useLegacyOffsetAndLimit );
}
