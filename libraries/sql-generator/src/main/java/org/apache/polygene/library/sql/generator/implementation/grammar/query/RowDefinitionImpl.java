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
package org.apache.polygene.library.sql.generator.implementation.grammar.query;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.common.ValueExpression;
import org.apache.polygene.library.sql.generator.grammar.query.RowDefinition;
import org.apache.polygene.library.sql.generator.grammar.query.RowValueConstructor;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class RowDefinitionImpl extends SQLSyntaxElementBase<RowValueConstructor, RowDefinition>
    implements RowDefinition
{

    private List<ValueExpression> _rowElements;

    public RowDefinitionImpl( SQLProcessorAggregator processor, List<ValueExpression> rowElements )
    {
        this( processor, RowDefinition.class, rowElements );
    }

    protected RowDefinitionImpl( SQLProcessorAggregator processor, Class<? extends RowDefinition> realImplementingType,
                                 List<ValueExpression> rowElements )
    {
        super( processor, realImplementingType );
        Objects.requireNonNull( rowElements, "row elements" );
        this._rowElements = Collections.unmodifiableList( rowElements );
    }

    public List<ValueExpression> getRowElements()
    {
        return this._rowElements;
    }

    @Override
    protected boolean doesEqual( RowDefinition another )
    {
        return this._rowElements.equals( another.getRowElements() );
    }
}
