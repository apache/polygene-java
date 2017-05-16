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
package org.apache.polygene.library.sql.generator.grammar.builders.definition;

import java.util.List;
import org.apache.polygene.library.sql.generator.grammar.builders.AbstractBuilder;
import org.apache.polygene.library.sql.generator.grammar.definition.table.UniqueConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.UniqueSpecification;

/**
 * This is builder for {@code UNIQUE(...)} and {@code PRIMARY KEY(...)} table constraints in table definition.
 *
 * @author Stanislav Muhametsin
 */
public interface UniqueConstraintBuilder
    extends AbstractBuilder<UniqueConstraint>
{

    /**
     * Sets the uniqueness kind for this uniqueness constraint.
     *
     * @param uniqueness The uniqueness kind for this uniqueness constraint.
     * @return This builder.
     * @see UniqueSpecification
     */
    UniqueConstraintBuilder setUniqueness( UniqueSpecification uniqueness );

    /**
     * Adds the columns that have to be unique.
     *
     * @param columnNames The column names that have to be unique.
     * @return This builder.
     */
    UniqueConstraintBuilder addColumns( String... columnNames );

    /**
     * Returns the uniqueness type for this uniqueness constraint.
     *
     * @return The uniqueness type for this uniqueness constraint.
     */
    UniqueSpecification getUniqueness();

    /**
     * Returns the column names that have to be unique.
     *
     * @return The column names that have to be unique.
     */
    List<String> getColumns();
}
