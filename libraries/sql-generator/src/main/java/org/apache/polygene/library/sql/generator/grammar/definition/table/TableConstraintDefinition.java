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
 * This syntax element represents the constraint definition for a table.
 *
 * @author Stanislav Muhametsin
 * @see TableElement
 * @see TableDefinition
 * @see TableConstraint
 */
public interface TableConstraintDefinition
    extends TableElement
{

    /**
     * Returns the optional name for this constraint. Will be {@code null} if no name defined for this constraint.
     *
     * @return The optional name for this constraint. Will be {@code null} if no name defined for this constraint.
     */
    String getConstraintName();

    /**
     * Returns the constraint characteristics for this constraint. Will be {@code null} if no characteristics defined.
     *
     * @return The constraint characteristics for this constraint. Will be {@code null} if no characteristics defined.
     * @see ConstraintCharacteristics
     */
    ConstraintCharacteristics getCharacteristics();

    /**
     * Returns the actual table constraint.
     *
     * @return The actual table constraint.
     */
    TableConstraint getConstraint();
}
