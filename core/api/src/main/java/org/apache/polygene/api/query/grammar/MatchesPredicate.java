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
package org.apache.polygene.api.query.grammar;

import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.property.Property;

/**
 * Regular expression match Specification.
 */
public class MatchesPredicate
    extends ExpressionPredicate
{
    private PropertyFunction<String> property;
    private Object value;

    public MatchesPredicate( PropertyFunction<String> property, String regexp )
    {
        this.property = property;
        this.value = regexp;
    }

    public MatchesPredicate( PropertyFunction<String> property, Variable variable )
    {
        this.property = property;
        this.value = variable;
    }

    public PropertyFunction<String> property()
    {
        return property;
    }

    public Object value()
    {
        return value;
    }

    public String regexp()
    {
        return ( String ) value;
    }

    @Override
    public boolean test( Composite item )
    {
        Property<String> prop = property.apply( item );

        if( prop == null )
        {
            return false;
        }

        String val = prop.get();

        if( val == null )
        {
            return false;
        }

        return val.matches( ( String ) value );
    }

    @Override
    public String toString()
    {
        return "( " + property + " matches " + "\"" + value + "\"" + " )";
    }
}
