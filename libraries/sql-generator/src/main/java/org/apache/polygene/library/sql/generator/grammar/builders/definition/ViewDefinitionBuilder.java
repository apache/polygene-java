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

import org.apache.polygene.library.sql.generator.grammar.builders.AbstractBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.definition.view.RegularViewSpecification;
import org.apache.polygene.library.sql.generator.grammar.definition.view.ViewCheckOption;
import org.apache.polygene.library.sql.generator.grammar.definition.view.ViewDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.view.ViewSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;

/**
 * This is a builder for {@code CREATE VIEW} statements.
 *
 * @author Stanislav Muhametsin
 * @see ViewDefinition
 */
public interface ViewDefinitionBuilder
    extends AbstractBuilder<ViewDefinition>
{

    /**
     * Sets whether this view is {@code RECURSIVE}.
     *
     * @param isRecursive True if view is to be {@code RECURSIVE}; false otherwise.
     * @return This builder.
     */
    ViewDefinitionBuilder setRecursive( Boolean isRecursive );

    /**
     * Sets the name for this view.
     *
     * @param viewName The name for this view.
     * @return This builder.
     */
    ViewDefinitionBuilder setViewName( TableNameDirect viewName );

    /**
     * Sets the query for this view.
     *
     * @param query The query for this view.
     * @return This builder.
     */
    ViewDefinitionBuilder setQuery( QueryExpression query );

    /**
     * Sets the view check option for this view.
     *
     * @param viewCheck The view check option for this view.
     * @return This builder.
     * @see ViewCheckOption
     */
    ViewDefinitionBuilder setViewCheckOption( ViewCheckOption viewCheck );

    /**
     * Sets the view specification for this view. Typically is a list of columns (via {@link RegularViewSpecification}).
     *
     * @param spec The view specification.
     * @return This builder.
     * @see ViewSpecification
     */
    ViewDefinitionBuilder setViewSpecification( ViewSpecification spec );

    /**
     * Returns whether this view is to be {@code RECURSIVE}.
     *
     * @return True if this view is to be {@code RECURSIVE}; false otherwise.
     */
    Boolean isRecursive();

    /**
     * Returns the name of the view.
     *
     * @return The name of the view.
     */
    TableNameDirect getViewName();

    /**
     * Returns the query for the view.
     *
     * @return The query for the view.
     */
    QueryExpression getQueryExpression();

    /**
     * Returns the view check option.
     *
     * @return The view check option.
     * @see ViewCheckOption
     */
    ViewCheckOption getViewCheckOption();

    /**
     * Returns the view specification. Typically is a list of columns (via {@link RegularViewSpecification}).
     *
     * @return The view specification.
     */
    ViewSpecification getViewSpecification();
}
