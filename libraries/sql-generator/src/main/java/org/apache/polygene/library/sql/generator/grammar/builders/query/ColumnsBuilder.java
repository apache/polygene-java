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
package org.apache.polygene.library.sql.generator.grammar.builders.query;

import java.util.List;
import org.apache.polygene.library.sql.generator.grammar.builders.AbstractBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.SetQuantifier;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReference;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReferences.ColumnReferenceInfo;
import org.apache.polygene.library.sql.generator.grammar.query.QuerySpecification;
import org.apache.polygene.library.sql.generator.grammar.query.SelectColumnClause;

/**
 * This builder builds the columns for {@code SELECT} statement.
 *
 * @author Stanislav Muhametsin
 * @see SelectColumnClause
 * @see QuerySpecification
 * @see ColumnReference
 */
public interface ColumnsBuilder
    extends AbstractBuilder<SelectColumnClause>
{
    /**
     * Adds columns without aliases to this {@code SELECT} statement.
     *
     * @param columns Columns without aliases to add to this {@code SELECT} statement.
     * @return This builder.
     */
    ColumnsBuilder addUnnamedColumns( ColumnReference... columns );

    /**
     * Add columns with aliases to this {@code SELECT} statement.
     *
     * @param namedColumns Columns with aliases to add to this {@code SELECT} statement.
     * @return
     */
    ColumnsBuilder addNamedColumns( ColumnReferenceInfo... namedColumns );

    /**
     * Sets the set quantifier ({@code ALL} or {@code DISTINCT}) for this {@code SELECT} statement.
     *
     * @param newSetQuantifier The new set quantifier.
     * @return This builder.
     * @see SetQuantifier
     */
    ColumnsBuilder setSetQuantifier( SetQuantifier newSetQuantifier );

    /**
     * Marks that all columns should be selected ({@literal SELECT} *). This will empty a list of columns to select.
     *
     * @return This builder.
     */
    ColumnsBuilder selectAll();

    /**
     * Returns the columns of this {@code SELECT} statement. It returns empty by default, or after calling
     * {@link #selectAll()} method.
     *
     * @return A list of columns of this {@code SELECT} statement. Might be empty.
     */
    List<ColumnReferenceInfo> getColumns();

    /**
     * Returns the set quantifier ({@code ALL} or {@code DISTINCT}) of this {@code SELECT} statement.
     *
     * @return The set quantifier of this {@code SELECT} statement.
     */
    SetQuantifier getSetQuantifier();
}
