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
package org.apache.polygene.library.sql.generator.grammar.definition.schema;

import java.util.List;
import org.apache.polygene.library.sql.generator.grammar.common.SchemaDefinitionStatement;

/**
 * This syntax element represents the {@code CREATE SCHEMA} statement.
 *
 * @author Stanislav Muhametsin
 */
public interface SchemaDefinition
    extends SchemaDefinitionStatement
{

    /**
     * Returns the name of the schema to be created.
     *
     * @return The name of the schema to be created.
     */
    String getSchemaName();

    /**
     * Returns the character set for this schema. May be {@code null} if none specified.
     *
     * @return The character set for this schema. May be {@code null} if none specified.
     */
    String getSchemaCharset();

    /**
     * Returns any additional elements for this schema. May be empty if none are present.
     *
     * @return Any additional elements for this schema. May be empty if none are present.
     */
    List<SchemaElement> getSchemaElements();
}
