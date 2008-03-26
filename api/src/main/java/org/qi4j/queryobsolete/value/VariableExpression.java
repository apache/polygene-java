/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.queryobsolete.value;

import java.util.Map;
import org.qi4j.queryobsolete.Expression;

public final class VariableExpression
    implements Expression, ValueExpression
{
    private String name;
    private Object defaultValue;

    public VariableExpression( String name, Object defaultValue )
    {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName()
    {
        return name;
    }

    public Object getDefaultValue()
    {
        return defaultValue;
    }

    public Object getValue( Object candidate, Map<String, Object> variables )
    {
        Object value = variables.get( name );
        if( value == null )
        {
            value = defaultValue;
        }

        return value;
    }

    public String toString()
    {
        return "?" + name + ( defaultValue == null ? "" : "(=" + defaultValue + ")" );
    }

}
