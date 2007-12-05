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
import org.qi4j.query.literals.BooleanLiteral;
import org.qi4j.query.literals.DateLiteral;
import org.qi4j.query.literals.NumberLiteral;
import org.qi4j.query.literals.StringLiteral;
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
    public static BooleanExpression eq( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "eq() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();  // Right
        Expression op2 = QueryStack.popExpression();  // Left
        Equals result = new Equals( op2, op1 );
        QueryStack.pushExpression( result );
        return result;
    }

    public static BooleanExpression ne( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "ne() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();  // Right
        Expression op2 = QueryStack.popExpression();  // Left
        NotEquals result = new NotEquals( op2, op1 );
        QueryStack.pushExpression( result );
        return result;
    }

    public static BooleanExpression lt( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "lt() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();  // Right
        Expression op2 = QueryStack.popExpression();  // Left
        LessThan result = new LessThan( op2, op1 );
        QueryStack.pushExpression( result );
        return result;
    }

    public static BooleanExpression le( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "le() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();
        Expression op2 = QueryStack.popExpression();
        LessThanEquals result = new LessThanEquals( op2, op1 );
        QueryStack.pushExpression( result );
        return result;
    }

    public static BooleanExpression gt( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "gt() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();
        Expression op2 = QueryStack.popExpression();
        GreaterThan result = new GreaterThan( op2, op1 );
        QueryStack.pushExpression( result );
        return result;
    }

    public static BooleanExpression ge( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "ge() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();
        Expression op2 = QueryStack.popExpression();
        GreaterThanEquals result = new GreaterThanEquals( op2, op1 );
        QueryStack.pushExpression( result );
        return result;
    }

    public static BooleanExpression matches( String source, String expression )
    {
        process( source );
        process( expression );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "matches() requires two arguments. The first argument was not derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression(); // The expression.
        Expression op2 = QueryStack.popExpression(); // The source.
        Matches result = new Matches( op2, op1 );
        QueryStack.pushExpression( result );
        return result;
    }

    public static <K> BooleanExpression contains( Iterable<K> left, K right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "contains() requires two arguments. The first argument was not derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression(); // The expression.
        Expression op2 = QueryStack.popExpression(); // The source.
        IterableContains result = new IterableContains( op2, op1 );
        QueryStack.pushExpression( result );
        return result;
    }

    public static BooleanExpression contains( Object source, Object substring )
    {
        process( source );
        process( substring );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "contains() requires two arguments. The first argument was not derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression(); // The substring value.
        Expression op2 = QueryStack.popExpression(); // The source value.
        StringContains result = new StringContains( op2, op1 );
        QueryStack.pushExpression( result );
        return result;
    }

    public static BooleanExpression isNull( Object value )
    {
        process( value );
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "isNull() require an argument derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression();
        IsNull result = new IsNull( op1 );
        QueryStack.pushExpression( result );
        return result;
    }

    public static BooleanExpression isNotNull( Object value )
    {
        process( value );
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "isNotNull() require an argument derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression();
        IsNotNull result = new IsNotNull( op1 );
        QueryStack.pushExpression( result );
        return result;
    }

    public static BooleanExpression and( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "and() requires two arguments." );
        }
        BooleanExpression op1 = (BooleanExpression) QueryStack.popExpression();
        BooleanExpression op2 = (BooleanExpression) QueryStack.popExpression();
        And result = new And( op2, op1 );
        QueryStack.pushExpression( result );
        return result;
    }

    public static BooleanExpression or( Object left, Object right )
    {
        process( left );
        process( right );
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "or() requires two arguments." );
        }
        BooleanExpression op1 = (BooleanExpression) QueryStack.popExpression();
        BooleanExpression op2 = (BooleanExpression) QueryStack.popExpression();
        Or result = new Or( op2, op1 );
        QueryStack.pushExpression( result );
        return result;
    }

    public static BooleanExpression not( Object expression )
    {
        process( expression );
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "not() requires one argument." );
        }
        if( expression instanceof BooleanExpression )
        {
            BooleanExpression op1 = (BooleanExpression) QueryStack.popExpression();
            Not result = new Not( op1 );
            QueryStack.pushExpression( result );
            return result;
        }
        else
        {
            throw new IllegalQueryFormatException( "not() only operates on BooleanExpression." );
        }
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
        if( operand instanceof String )
        {
            StringLiteral expr = new StringLiteral( (String) operand );
            QueryStack.pushExpression( expr );
            return;
        }
        if( operand instanceof Boolean )
        {
            BooleanLiteral expr = new BooleanLiteral( (Boolean) operand );
            QueryStack.pushExpression( expr );
            return;
        }
        if( operand instanceof Date )
        {
            DateLiteral expr = new DateLiteral( (Date) operand );
            QueryStack.pushExpression( expr );
            return;
        }
        if( operand instanceof Number )
        {
            NumberLiteral expr = new NumberLiteral( (Number) operand );
            QueryStack.pushExpression( expr );
            return;
        }
        if( Proxy.isProxyClass( operand.getClass() ) )
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

    private static boolean notOnStack( int expected )
    {
        return QueryStack.getSize() < expected;
    }

}
