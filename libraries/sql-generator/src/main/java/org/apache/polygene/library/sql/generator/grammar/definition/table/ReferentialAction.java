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
package org.apache.polygene.library.sql.generator.grammar.definition.table;

/**
 * This enum represents the five referential actions of SQL. These are {@link #CASCADE}, {@link #SET_NULL},
 * {@link #SET_DEFAULT}, {@link #RESTRICT}, and {@link #NO_ACTION}.
 *
 */
public final class ReferentialAction
{

    /**
     * Represents the cascading policy ({@code CASCADE}.
     */
    public static final ReferentialAction CASCADE = new ReferentialAction();

    /**
     * Represents the policy, which sets the value as {@code NULL} ({@code SET NULL}.
     */
    public static final ReferentialAction SET_NULL = new ReferentialAction();

    /**
     * Represents the policy, which sets the value as default ({@code SET DEFAULT}.
     */
    public static final ReferentialAction SET_DEFAULT = new ReferentialAction();

    /**
     * Represents the policy, which checks the integrity <b>before</b> {@code UPDATE} or {@code DELETE} statement, and
     * the statement will not be executed if it violates integrity.
     */
    public static final ReferentialAction RESTRICT = new ReferentialAction();

    /**
     * Represents the policy, which checks the integrity <b>after</b> {@code UPDATE} or {@code DELETE} statement, and
     * the statement will not be executed if it violates integrity.
     */
    public static final ReferentialAction NO_ACTION = new ReferentialAction();
}
