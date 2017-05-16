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
package org.apache.polygene.library.sql.generator.grammar.query.joins;

/**
 * This enum represents the join type, used in {@link QualifiedJoinedTable} and {@link NaturalJoinedTable}. Is one of
 * {@link #INNER}, {@link #LEFT_OUTER}, {@link #RIGHT_OUTER}, or {@link #FULL_OUTER}.
 *
 * @author Stanislav Muhametsin
 */
public final class JoinType
{
    /**
     * The {@code INNER} join, typically default.
     */
    public static final JoinType INNER = new JoinType();

    /**
     * The {@code LEFT OUTER} join.
     */
    public static final JoinType LEFT_OUTER = new JoinType();

    /**
     * The {@code RIGHT OUTER} join.
     */
    public static final JoinType RIGHT_OUTER = new JoinType();

    /**
     * The {@code FULL OUTER} join.
     */
    public static final JoinType FULL_OUTER = new JoinType();
}