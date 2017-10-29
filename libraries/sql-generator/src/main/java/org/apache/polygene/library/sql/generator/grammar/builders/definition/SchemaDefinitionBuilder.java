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
import org.apache.polygene.library.sql.generator.grammar.definition.schema.SchemaDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.schema.SchemaElement;

/**
 * This is builder interface for creating schema definition statements ({@code CREATE SCHEMA} ...).
 *
 */
public interface SchemaDefinitionBuilder
    extends AbstractBuilder<SchemaDefinition>
{

    /**
     * Sets the name for the schema to be created.
     *
     * @param schemaName The name for the schema to be created.
     * @return This builder.
     */
    SchemaDefinitionBuilder setSchemaName( String schemaName );

    /**
     * Sets the character set for the schema to be created.
     *
     * @param charset The charset for the schema to be created.
     * @return This builder.
     */
    SchemaDefinitionBuilder setSchemaCharset( String charset );

    /**
     * Adds schema elements for the schema to be created.
     *
     * @param elements The schema elements for the schema to be created.
     * @return This builder.
     * @see SchemaElement
     */
    SchemaDefinitionBuilder addSchemaElements( SchemaElement... elements );

    /**
     * Returns the name of the schema to be created.
     *
     * @return The name of the schema to be created.
     */
    String getSchemaName();

    /**
     * Returns the name of the character set for the schema to be created.
     *
     * @return The name of the character set for the schema to be created.
     */
    String getSchemaCharset();

    /**
     * Returns all the schema elements for the schema to be created.
     *
     * @return All the schema elements for the schema to be created.
     */
    List<SchemaElement> getSchemaElements();
}
