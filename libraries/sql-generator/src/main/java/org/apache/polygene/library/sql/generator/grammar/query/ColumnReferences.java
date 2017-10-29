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
package org.apache.polygene.library.sql.generator.grammar.query;

import java.util.List;
import java.util.Objects;

/**
 * This syntax element represents the whole {@code SELECT <column1>, <column2>, ...} clause, up until {@code FROM}. Each
 * column might have an alias.
 *
 *
 */
public interface ColumnReferences
    extends SelectColumnClause
{
    /**
     * A helper class to encapsulate column reference along with its alias.
     *
     *
     */
    class ColumnReferenceInfo
    {
        private final String _alias;
        private final ColumnReference _reference;

        public ColumnReferenceInfo( String alias, ColumnReference reference )
        {
            Objects.requireNonNull( reference );

            this._alias = alias;
            this._reference = reference;
        }

        /**
         * Returns the alias of this column reference. May be {@code null.}
         *
         * @return The alias of this column reference. May be {@code null.}
         */
        public String getAlias()
        {
            return this._alias;
        }

        /**
         * Returns the column reference.
         *
         * @return The column reference.
         */
        public ColumnReference getReference()
        {
            return this._reference;
        }
    }

    /**
     * Returns the list of column references, along with their aliases.
     *
     * @return The list of column references, along with their aliases.
     */
    List<ColumnReferenceInfo> getColumns();
}
