/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.query.operators;

import java.util.Map;
import java.util.regex.Pattern;
import org.qi4j.query.BinaryOperator;
import org.qi4j.query.BooleanExpression;
import org.qi4j.query.Expression;
import org.qi4j.query.value.ValueExpression;

public final class Matches
    implements BinaryOperator, BooleanExpression
{
    private ValueExpression left;
    private ValueExpression right;

    private String lastExpression;
    private Pattern lastPattern;

    public Matches( ValueExpression left, ValueExpression right )
    {
        this.left = left;
        this.right = right;
    }

    public Expression getLeftArgument()
    {
        return left;
    }

    public Expression getRightArgument()
    {
        return right;
    }

    public synchronized boolean evaluate( Object candidate, Map<String, Object> variables )
    {
        String str = right.getValue( candidate, variables ).toString();

        if( lastExpression == null || !lastExpression.equals( str ) )
        {
            lastExpression = str;
            lastPattern = Pattern.compile( str );
        }

        String value = left.getValue( candidate, variables ).toString();

        return lastPattern.matcher( value ).matches();
    }

    public String toString()
    {
        return "(" + left + ".matches(" + right + "))";
    }

}
