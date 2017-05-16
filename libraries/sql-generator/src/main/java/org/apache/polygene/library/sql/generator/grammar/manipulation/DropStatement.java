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

import org.apache.polygene.library.sql.generator.grammar.common.SchemaManipulationStatement;

/**
 * This is a common interface for {@code DROP} statements.
 *
 * @author Stanislav Muhametsin
 */
public interface DropStatement
    extends SchemaManipulationStatement, DropBehaviourContainer
{

    /**
     * Returns the object type to drop. Typically either {@link ObjectType#SCHEMA}, {@link ObjectType#TABLE}, or
     * {@link ObjectType#VIEW}.
     *
     * @return The object type to drop.
     * @see ObjectType
     */
    ObjectType whatToDrop();
}
