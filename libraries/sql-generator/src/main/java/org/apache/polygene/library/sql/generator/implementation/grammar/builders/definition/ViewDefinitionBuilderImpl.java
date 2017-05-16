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
package org.apache.polygene.library.sql.generator.implementation.grammar.builders.definition;

import org.apache.polygene.library.sql.generator.grammar.builders.definition.ViewDefinitionBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.definition.view.ViewCheckOption;
import org.apache.polygene.library.sql.generator.grammar.definition.view.ViewDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.view.ViewSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLBuilderBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.definition.view.ViewDefinitionImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class ViewDefinitionBuilderImpl extends SQLBuilderBase
    implements ViewDefinitionBuilder
{

    private Boolean _isRecursive;
    private TableNameDirect _name;
    private QueryExpression _query;
    private ViewCheckOption _viewCheck;
    private ViewSpecification _viewSpec;

    public ViewDefinitionBuilderImpl( SQLProcessorAggregator processor )
    {
        super( processor );
    }

    public ViewDefinition createExpression()
    {
        return new ViewDefinitionImpl( this.getProcessor(), this._name, this._query, this._viewSpec, this._viewCheck,
                                       this._isRecursive );
    }

    public ViewDefinitionBuilder setRecursive( Boolean isRecursive )
    {
        this._isRecursive = isRecursive;
        return this;
    }

    public ViewDefinitionBuilder setViewName( TableNameDirect viewName )
    {
        this._name = viewName;
        return this;
    }

    public ViewDefinitionBuilder setQuery( QueryExpression query )
    {
        this._query = query;
        return this;
    }

    public ViewDefinitionBuilder setViewCheckOption( ViewCheckOption viewCheck )
    {
        this._viewCheck = viewCheck;
        return this;
    }

    public ViewDefinitionBuilder setViewSpecification( ViewSpecification spec )
    {
        this._viewSpec = spec;
        return this;
    }

    public Boolean isRecursive()
    {
        return this._isRecursive;
    }

    public TableNameDirect getViewName()
    {
        return this._name;
    }

    public QueryExpression getQueryExpression()
    {
        return this._query;
    }

    public ViewCheckOption getViewCheckOption()
    {
        return this._viewCheck;
    }

    public ViewSpecification getViewSpecification()
    {
        return this._viewSpec;
    }
}
