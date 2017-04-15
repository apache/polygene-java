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
package org.apache.polygene.library.sql.generator.grammar.manipulation;

/**
 * Object type is used in generalized {@code DROP} statement to define what to drop. It is usually one of
 * {@link #SCHEMA}, {@link #TABLE}, or {@link #VIEW}.
 *
 * @author Stanislav Muhametsin
 * @see DropStatement
 */
public final class ObjectType
{

    /**
     * The object type which means to {@code DROP SCHEMA}.
     */
    public static final ObjectType SCHEMA = new ObjectType();

    /**
     * The object type which means to {@code DROP TABLE}.
     */
    public static final ObjectType TABLE = new ObjectType();

    /**
     * The object type which means to {@code DROP VIEW}.
     */
    public static final ObjectType VIEW = new ObjectType();
}
