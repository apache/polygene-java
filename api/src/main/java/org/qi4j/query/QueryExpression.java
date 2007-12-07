/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.query;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Date;
import org.qi4j.query.operators.And;
import org.qi4j.query.operators.Equals;
import org.qi4j.query.operators.GreaterThan;
import org.qi4j.query.operators.GreaterThanEquals;
import org.qi4j.query.operators.IsNotNull;
import org.qi4j.query.operators.IsNull;
import org.qi4j.query.operators.IterableContains;
import org.qi4j.query.operators.LessThan;
import org.qi4j.query.operators.LessThanEquals;
import org.qi4j.query.operators.Matches;
import org.qi4j.query.operators.Not;
import org.qi4j.query.operators.NotEquals;
import org.qi4j.query.operators.Or;
import org.qi4j.query.operators.StringContains;
import org.qi4j.query.value.BooleanValueExpression;
import org.qi4j.query.value.DateValueExpression;
import org.qi4j.query.value.NumberValueExpression;
import org.qi4j.query.value.StringValueExpression;
import org.qi4j.query.value.ValueExpression;
import org.qi4j.query.value.VariableExpression;

/**
 * TODO
 */
public class QueryExpression
{
    /**
     * Operator for equals() checks on two return values of parameters.
     * <p/>
     * For instance;
     * <code><pre>
     * Invoice p = queryBuilder.parameter( Invoice.class );
     * :
     * queryBuilder.where( eq( p.getCreatedDate(), p.getModifiedDate() ) );
     * <p/>
     * </pre></code>
     *
     * @param left
     * @param right
     * @return
     */
    public static Equals eq( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "eq() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();  // Right
        Expression op2 = QueryStack.popExpression();  // Left
        Equals result = new Equals( (ValueExpression) op2, (ValueExpression) op1 );
        return result;
    }

    public static NotEquals ne( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "ne() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();  // Right
        Expression op2 = QueryStack.popExpression();  // Left
        NotEquals result = new NotEquals( (ValueExpression) op2, (ValueExpression) op1 );
        return result;
    }

    public static LessThan lt( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "lt() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();  // Right
        Expression op2 = QueryStack.popExpression();  // Left
        LessThan result = new LessThan( (ValueExpression) op2, (ValueExpression) op1 );
        return result;
    }

    public static LessThanEquals le( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "le() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();
        Expression op2 = QueryStack.popExpression();
        LessThanEquals result = new LessThanEquals( (ValueExpression) op2, (ValueExpression) op1 );
        return result;
    }

    public static GreaterThan gt( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "gt() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();
        Expression op2 = QueryStack.popExpression();
        GreaterThan result = new GreaterThan( (ValueExpression) op2, (ValueExpression) op1 );
        return result;
    }

    public static GreaterThanEquals ge( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "ge() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();
        Expression op2 = QueryStack.popExpression();
        GreaterThanEquals result = new GreaterThanEquals( (ValueExpression) op2, (ValueExpression) op1 );
        return result;
    }

    public static Matches matches( String source, String expression )
    {
        process( source );
        process( expression );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "matches() requires two arguments. The first argument was not derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression(); // The expression.
        Expression op2 = QueryStack.popExpression(); // The source.
        Matches result = new Matches( (ValueExpression) op2, (ValueExpression) op1 );
        return result;
    }

    public static <K> IterableContains contains( Iterable<K> left, K right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "contains() requires two arguments. The first argument was not derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression(); // The expression.
        Expression op2 = QueryStack.popExpression(); // The source.
        IterableContains result = new IterableContains( (ValueExpression) op2, (ValueExpression) op1 );
        return result;
    }

    public static StringContains contains( Object source, Object substring )
    {
        process( source );
        process( substring );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "contains() requires two arguments. The first argument was not derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression(); // The substring value.
        Expression op2 = QueryStack.popExpression(); // The source value.
        StringContains result = new StringContains( (ValueExpression) op2, (ValueExpression) op1 );
        return result;
    }

    public static IsNull isNull( Object value )
    {
        process( value );
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "isNull() require an argument derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression();
        IsNull result = new IsNull( (ValueExpression) op1 );
        return result;
    }

    public static IsNotNull isNotNull( Object value )
    {
        process( value );
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "isNotNull() require an argument derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression();
        IsNotNull result = new IsNotNull( (ValueExpression) op1 );
        return result;
    }

    public static And and( BooleanExpression left, BooleanExpression right )
    {
        And result = new And( left, right );
        return result;
    }

    public static Or or( BooleanExpression left, BooleanExpression right )
    {
        Or result = new Or( left, right );
        return result;
    }

    public static Not not( BooleanExpression expression )
    {
        Not result = new Not( expression );
        return result;
    }

    public static VariableExpression var( String name, Object defaultValue )
    {
        VariableExpression variable = new VariableExpression( name, defaultValue );
        QueryStack.pushExpression( variable );
        return variable;
    }

    private static void process( Object operand )
    {
        if( operand instanceof Expression )
        {
            return;
        }
        if( operand == null )
        {
            return;
        }

        ValueExpression expr = getValueExpression( operand );
        if( expr != null )
        {
            QueryStack.pushExpression( expr );
            return;
        }
        else if( Proxy.isProxyClass( operand.getClass() ) )
        {
            InvocationHandler handler = Proxy.getInvocationHandler( operand );
            if( handler instanceof InterfaceInvocationHandler )
            {
                return;
            }
            else
            {
                throw new IllegalArgumentException( "Proxy handled by " + handler + " is not allowed as operands." );
            }
        }

        throw new IllegalArgumentException( "Type " + operand.getClass().getName() + " is not allowed as operands." );
    }

    static ValueExpression getValueExpression( Object operand )
    {
        if( operand instanceof String )
        {
            StringValueExpression expr = new StringValueExpression( (String) operand );
            return expr;
        }
        if( operand instanceof Boolean )
        {
            BooleanValueExpression expr = new BooleanValueExpression( (Boolean) operand );
            return expr;
        }
        if( operand instanceof Date )
        {
            DateValueExpression expr = new DateValueExpression( (Date) operand );
            return expr;
        }
        if( operand instanceof Number )
        {
            NumberValueExpression expr = new NumberValueExpression( (Number) operand );
            return expr;
        }
        return null;
    }

    private static boolean notOnStack( int expected )
    {
        return QueryStack.getSize() < expected;
    }

}
