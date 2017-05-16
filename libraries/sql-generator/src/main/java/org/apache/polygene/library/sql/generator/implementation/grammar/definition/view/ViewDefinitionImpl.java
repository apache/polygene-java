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
package org.apache.polygene.library.sql.generator.implementation.grammar.definition.view;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.Typeable;
import org.apache.polygene.library.sql.generator.grammar.common.SchemaStatement;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.definition.view.ViewCheckOption;
import org.apache.polygene.library.sql.generator.grammar.definition.view.ViewDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.view.ViewSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class ViewDefinitionImpl extends SQLSyntaxElementBase<SchemaStatement, ViewDefinition>
    implements ViewDefinition
{

    private final TableNameDirect _name;
    private final QueryExpression _query;
    private final ViewSpecification _spec;
    private final ViewCheckOption _viewCheck;
    private final Boolean _isRecursive;

    public ViewDefinitionImpl( SQLProcessorAggregator processor, TableNameDirect name, QueryExpression query,
                               ViewSpecification spec, ViewCheckOption viewCheck, Boolean isRecursive )
    {
        this( processor, ViewDefinition.class, name, query, spec, viewCheck, isRecursive );
    }

    protected ViewDefinitionImpl( SQLProcessorAggregator processor,
                                  Class<? extends ViewDefinition> realImplementingType, TableNameDirect name, QueryExpression query,
                                  ViewSpecification spec, ViewCheckOption viewCheck, Boolean isRecursive )
    {
        super( processor, realImplementingType );

        Objects.requireNonNull( name, "View name" );
        Objects.requireNonNull( query, "View query" );
        Objects.requireNonNull( isRecursive, "Is recursive" );
        Objects.requireNonNull( spec, "View specification" );

        this._name = name;
        this._query = query;
        this._spec = spec;
        this._isRecursive = isRecursive;
        this._viewCheck = viewCheck;
    }

    @Override
    protected boolean doesEqual( ViewDefinition another )
    {
        return this._name.equals( another.getViewName() ) && this._isRecursive.equals( another.isRecursive() )
               && this._spec.equals( another.getViewSpecification() ) && this._query.equals( another.getViewQuery() )
               && TypeableImpl.bothNullOrEquals( this._viewCheck, another.getViewCheckOption() );
    }

    public Typeable<?> asTypeable()
    {
        return this;
    }

    public ViewCheckOption getViewCheckOption()
    {
        return this._viewCheck;
    }

    public TableNameDirect getViewName()
    {
        return this._name;
    }

    public QueryExpression getViewQuery()
    {
        return this._query;
    }

    public ViewSpecification getViewSpecification()
    {
        return this._spec;
    }

    public Boolean isRecursive()
    {
        return this._isRecursive;
    }
}
