/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.runtime.query.grammar.impl;

import org.qi4j.api.query.grammar.VariableValueExpression;

/**
 * A variable value epression, whois value can be set at runtime before query invocation, via
 * {@link org.qi4j.api.query.Query#setVariable(String, Object)}.
 */
public final class VariableValueExpressionImpl<T>
    implements VariableValueExpression<T>
{

    /**
     * Variable name.
     */
    private final String name;
    /**
     * Current variable value.
     */
    private T value;

    /**
     * Constructor.
     *
     * @param name variable name; cannot be null or empty
     *
     * @throws IllegalArgumentException - If name is null or empty
     */
    public VariableValueExpressionImpl( final String name )
    {
        if( name == null || name.trim().length() == 0 )
        {
            throw new IllegalArgumentException( "Variable name cannot be null or empty" );
        }
        this.name = name;
    }

    /**
     * Getter.
     *
     * @return variable name
     */
    public String name()
    {
        return name;
    }

    /**
     * Getter.
     *
     * @return value
     *
     * @throws IllegalStateException - If value of this variable was not set prior
     */
    public T value()
    {
        if( value == null )
        {
            throw new IllegalStateException( "Value of variable [" + name + "] is not set" );
        }
        return value;
    }

    /**
     * Setter.
     *
     * @param value variable value; cannot be null
     *
     * @throws IllegalArgumentException - If value is null
     */
    public void setValue( T value )
    {
        if( value == null )
        {
            throw new IllegalArgumentException( "Value of variable [" + name + "] cannot be null" );
        }
        this.value = value;
    }

    @Override
    public String toString()
    {
        return value == null ? "$" + name : value.toString();
    }
}