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
package org.apache.polygene.library.sql.generator.implementation.grammar.literals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.common.ValueExpression;
import org.apache.polygene.library.sql.generator.grammar.literals.SQLFunctionLiteral;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.NonBooleanExpressionImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class SQLFunctionLiteralImpl extends NonBooleanExpressionImpl<SQLFunctionLiteral>
    implements SQLFunctionLiteral
{

    private final String _name;

    private final List<ValueExpression> _parameters;

    public SQLFunctionLiteralImpl( SQLProcessorAggregator processor, String name, ValueExpression... parameters )
    {
        this( processor, name, Arrays.asList( parameters ) );
    }

    public SQLFunctionLiteralImpl( SQLProcessorAggregator processor, String name, List<ValueExpression> parameters )
    {
        this( processor, SQLFunctionLiteral.class, name, parameters );
    }

    protected SQLFunctionLiteralImpl( SQLProcessorAggregator processor, Class<? extends SQLFunctionLiteral> implClass,
                                      String name, List<ValueExpression> parameters )
    {
        super( processor, implClass );
        Objects.requireNonNull( name, "name" );
        Objects.requireNonNull( parameters, "parameters" );
        for( ValueExpression exp : parameters )
        {
            Objects.requireNonNull( exp, "parameter" );
        }

        this._name = name;
        this._parameters = Collections.unmodifiableList( new ArrayList<ValueExpression>( parameters ) );
    }

    public String getFunctionName()
    {
        return this._name;
    }

    public List<ValueExpression> getParameters()
    {
        return this._parameters;
    }

    @Override
    protected boolean doesEqual( SQLFunctionLiteral another )
    {
        return this._name.equals( another.getFunctionName() ) && this._parameters.equals( another.getParameters() );
    }
}
