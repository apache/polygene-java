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
package org.apache.polygene.library.sql.generator.grammar.definition.view;

import org.apache.polygene.library.sql.generator.grammar.common.SchemaDefinitionStatement;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.definition.schema.SchemaElement;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;

/**
 * This syntax element represents the {@code CREATE VIEW} statement.
 *
 */
public interface ViewDefinition
    extends SchemaDefinitionStatement, SchemaElement
{

    /**
     * Returns {@code true} if the view is defined to be {@code RECURSIVE}, {@code false} otherwise.
     *
     * @return {@code true} if the view is defined to be {@code RECURSIVE}, {@code false} otherwise.
     */
    Boolean isRecursive();

    /**
     * Returns the name of this view.
     *
     * @return The name of this view.
     */
    TableNameDirect getViewName();

    /**
     * Returns the view specification.
     *
     * @return The view specification.
     * @see ViewSpecification
     */
    ViewSpecification getViewSpecification();

    /**
     * Returns the query defining the contents for this view.
     *
     * @return The query defining the contents for this view.
     */
    QueryExpression getViewQuery();

    /**
     * Returns the view check option. Will be {@code null} if none defined.
     *
     * @return The view check option. Will be {@code null} if none defined.
     * @see ViewCheckOption
     */
    ViewCheckOption getViewCheckOption();
}
