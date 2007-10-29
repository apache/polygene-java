package org.qi4j.query;

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
    public static <K> ComparableExpression<K> arg( Comparable<K> data )
    {
        return new Argument<K>( data );
    }

    public static BinaryExpression eq( Expression left, Object right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "eq() requires two arguments." );
        }
        Expression op = QueryStack.popExpression();
        return new Equals( left, op );
    }

    private static boolean notOnStack( int expected )
    {
        return QueryStack.getSize() < 1;
    }

    public static BinaryExpression eq( Object left, Expression right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "eq() requires two arguments." );
        }
        Expression op = QueryStack.popExpression();
        return new Equals( op, right );
    }

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
    public static BinaryExpression eq( Object left, Object right )
    {
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "eq() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();
        Expression op2 = QueryStack.popExpression();
        return new Equals( op1, op2 );
    }

    public static BinaryExpression ne( Object left, Expression right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "ne() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();
        return new NotEquals( op1, right );
    }

    public static BinaryExpression ne( Expression left, Object right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "ne() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();
        return new NotEquals( left, op1 );
    }

    public static BinaryExpression ne( Object left, Object right )
    {
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "ne() requires two arguments." );
        }
        Expression op1 = QueryStack.popExpression();
        Expression op2 = QueryStack.popExpression();
        return new NotEquals( op1, op2 );
    }

    public static BinaryExpression lt( ComparableExpression left, Comparable right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "lt() requires two arguments." );
        }
        ComparableExpression op1 = (ComparableExpression) QueryStack.popExpression();
        return new LessThan( left, op1 );
    }

    public static BinaryExpression lt( Comparable left, ComparableExpression right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "lt() requires two arguments." );
        }
        ComparableExpression op1 = (ComparableExpression) QueryStack.popExpression();
        return new LessThan( op1, right );
    }

    public static BinaryExpression lt( Comparable left, Comparable right )
    {
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "lt() requires two arguments." );
        }
        ComparableExpression op1 = (ComparableExpression) QueryStack.popExpression();
        ComparableExpression op2 = (ComparableExpression) QueryStack.popExpression();
        return new LessThan( op1, op2 );
    }

    public static BinaryExpression le( ComparableExpression left, Comparable right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "le() requires two arguments." );
        }
        ComparableExpression op1 = (ComparableExpression) QueryStack.popExpression();
        return new LessThanEquals( left, op1 );
    }

    public static BinaryExpression le( Comparable left, ComparableExpression right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "le() requires two arguments." );
        }
        ComparableExpression op1 = (ComparableExpression) QueryStack.popExpression();
        return new LessThanEquals( op1, right );
    }

    public static BinaryExpression le( Comparable left, Comparable right )
    {
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "le() requires two arguments." );
        }
        ComparableExpression op1 = (ComparableExpression) QueryStack.popExpression();
        ComparableExpression op2 = (ComparableExpression) QueryStack.popExpression();
        return new LessThanEquals( op1, op2 );
    }

    public static BinaryExpression gt( ComparableExpression left, Comparable right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "gt() requires two arguments." );
        }
        ComparableExpression op1 = (ComparableExpression) QueryStack.popExpression();
        return new GreaterThan( left, op1 );
    }

    public static BinaryExpression gt( Comparable left, ComparableExpression right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "gt() requires two arguments." );
        }
        ComparableExpression op1 = (ComparableExpression) QueryStack.popExpression();
        return new GreaterThan( op1, right );
    }

    public static BinaryExpression gt( Comparable left, Comparable right )
    {
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "gt() requires two arguments." );
        }
        ComparableExpression op1 = (ComparableExpression) QueryStack.popExpression();
        ComparableExpression op2 = (ComparableExpression) QueryStack.popExpression();
        return new GreaterThan( op1, op2 );
    }

    public static BinaryExpression ge( ComparableExpression left, Comparable right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "ge() requires two arguments." );
        }
        ComparableExpression op1 = (ComparableExpression) QueryStack.popExpression();
        return new GreaterThanEquals( left, op1 );
    }

    public static BinaryExpression ge( Comparable left, ComparableExpression right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "ge() requires two arguments." );
        }
        ComparableExpression op1 = (ComparableExpression) QueryStack.popExpression();
        return new GreaterThanEquals( op1, right );
    }

    public static BinaryExpression ge( Comparable left, Comparable right )
    {
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "ge() requires two arguments." );
        }
        ComparableExpression op1 = (ComparableExpression) QueryStack.popExpression();
        ComparableExpression op2 = (ComparableExpression) QueryStack.popExpression();
        return new GreaterThanEquals( op1, op2 );
    }

    public static BinaryExpression matches( String source, String expression )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "matches() requires two arguments. The first argument was not derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression(); // The parameter value.
        return new Matches( op1, expression );
    }

    public static <K> BinaryExpression contains( Iterable<K> left, K right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "contains() requires two arguments. The first argument was not derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression(); // The parameter value.
        return new IterableContains( op1, right );
    }

    public static BinaryExpression contains( String value, String substring )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "contains() requires two arguments. The first argument was not derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression(); // The parameter value.
        return new StringContains( op1, substring );
    }

    public static BinaryExpression isNull( Object value )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "isNull() require an argument derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression();
        return new IsNull( op1 );
    }

    public static BinaryExpression isNotNull( Object value )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "isNotNull() require an argument derived from a parameter." );
        }
        Expression op1 = QueryStack.popExpression();
        return new IsNotNull( op1 );
    }

    public static BinaryExpression and( Object left, Object right )
    {
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "and() requires two arguments." );
        }
        BinaryExpression op1 = (BinaryExpression) QueryStack.popExpression();
        BinaryExpression op2 = (BinaryExpression) QueryStack.popExpression();
        return new And( op1, op2 );
    }

    public static BinaryExpression and( Object left, BinaryExpression right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "and() requires two arguments." );
        }
        BinaryExpression op1 = (BinaryExpression) QueryStack.popExpression();
        return new And( op1, right );
    }

    public static BinaryExpression and( BinaryExpression left, Object right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "and() requires two arguments." );
        }
        BinaryExpression op1 = (BinaryExpression) QueryStack.popExpression();
        return new And( left, op1 );
    }

    public static BinaryExpression and( BinaryExpression left, BinaryExpression right )
    {
        return new And( left, right );
    }

    public static BinaryExpression or( Object left, Object right )
    {
        if( notOnStack( 2 ) )
        {
            throw new IllegalQueryFormatException( "or() requires two arguments." );
        }
        BinaryExpression op1 = (BinaryExpression) QueryStack.popExpression();
        BinaryExpression op2 = (BinaryExpression) QueryStack.popExpression();
        return new Or( op1, op2 );
    }

    public static BinaryExpression or( Object left, BinaryExpression right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "or() requires two arguments." );
        }
        BinaryExpression op1 = (BinaryExpression) QueryStack.popExpression();
        return new Or( op1, right );
    }

    public static BinaryExpression or( BinaryExpression left, Object right )
    {
        if( notOnStack( 1 ) )
        {
            throw new IllegalQueryFormatException( "or() requires two arguments." );
        }
        BinaryExpression op1 = (BinaryExpression) QueryStack.popExpression();
        return new Or( left, op1 );
    }

    public static BinaryExpression or( BinaryExpression left, BinaryExpression right )
    {
        return new Or( left, right );
    }

    public static BinaryExpression not( BinaryExpression expression )
    {
        return new Not( expression );
    }

}
