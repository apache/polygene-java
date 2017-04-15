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

import org.apache.polygene.library.sql.generator.grammar.builders.AbstractBuilder;
import org.apache.polygene.library.sql.generator.grammar.query.TableReference;
import org.apache.polygene.library.sql.generator.grammar.query.joins.JoinSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.joins.JoinType;

/**
 * The builder to build joined tables. A joined table contains 0 or more joins.
 *
 * @author Stanislav Muhametsin
 * @see TableReference
 */
public interface TableReferenceBuilder
    extends AbstractBuilder<TableReference>
{
    /**
     * Adds a qualified join ({@code JOIN}) to whatever current table of builder, and overwrites the current table with
     * the result.
     *
     * @param joinType The join type.
     * @param right    The table on the right side of the join.
     * @param joinSpec The join specification.
     * @return This builder
     * @see JoinType
     * @see JoinSpecification
     */
    TableReferenceBuilder addQualifiedJoin( JoinType joinType, TableReference right, JoinSpecification joinSpec );

    /**
     * Adds a cross join ({@code CROSS JOIN}) to whatever current table of builder, and overwrites the current table
     * with the result.
     *
     * @param right The table on the right side of the join.
     * @return This builder.
     */
    TableReferenceBuilder addCrossJoin( TableReference right );

    /**
     * Adds a natural join ({@code NATURAL JOIN}) to whatever current table of builder, and overwrites the current table
     * with the result.
     *
     * @param joinType The join type.
     * @param right    The table on the right side of the join.
     * @return This builder.
     */
    TableReferenceBuilder addNaturalJoin( JoinType joinType, TableReference right );

    /**
     * Adds an union join ({@code UNION JOIN}) to whatever current table of builder, and overwrites the current table
     * with the result.
     *
     * @param right The table on the right side of the join.
     * @return This builder
     */
    TableReferenceBuilder addUnionJoin( TableReference right );
}