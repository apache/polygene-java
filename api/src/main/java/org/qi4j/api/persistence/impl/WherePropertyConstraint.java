package org.qi4j.api.persistence.impl;

import java.lang.reflect.Method;
import java.util.regex.Pattern;
import org.qi4j.api.persistence.Query;

/**
 * TODO
 */
public class WherePropertyConstraint
    implements WhereConstraint
{
    private Method readMethod;
    private Object value;
    private Query.Is comparisonOperator;
    private Pattern matcher;

    public WherePropertyConstraint( Method readMethod, Object value, Query.Is comparisonOperator )
    {
        this.readMethod = readMethod;
        this.value = value;
        this.comparisonOperator = comparisonOperator;

        if( comparisonOperator == Query.Is.MATCHES )
        {
            matcher = Pattern.compile( value.toString() );
        }
    }

    public boolean accepts( Object anObject )
    {
        try
        {
            Object objectValue = getValue( anObject );

            switch( comparisonOperator )
            {
            // Boolean
            case EQUAL:
            {
                return objectValue.equals( value );
            }

            case NOT_EQUAL:
            {
                return !objectValue.equals( value );
            }

            // Numerical
            case LESS_THAN:
            {
                return ( (Comparable) objectValue ).compareTo( value ) < 0;
            }

            case LESS_THAN_OR_EQUAL:
            {
                return ( (Comparable) objectValue ).compareTo( value ) <= 0;
            }

            case GREATER_THAN:
            {
                return ( (Comparable) objectValue ).compareTo( value ) > 0;
            }

            case GREATER_THAN_OR_EQUAL:
            {
                return ( (Comparable) objectValue ).compareTo( value ) >= 0;
            }

            // String
            case CONTAINS:
            {
                return objectValue.toString().contains( value.toString() );
            }

            case STARTS_WITH:
            {
                return objectValue.toString().startsWith( value.toString() );
            }

            case ENDS_WITH:
            {
                return objectValue.toString().endsWith( value.toString() );
            }

            case MATCHES:
            {
                return matcher.matcher( objectValue.toString() ).matches();
            }

            }

            return false;
        }
        catch( Exception e )
        {
            return false;
        }
    }

    public Method getReadMethod()
    {
        return readMethod;
    }

    public Object getValue()
    {
        return value;
    }

    public Query.Is getComparisonOperator()
    {
        return comparisonOperator;
    }

    private Object getValue( Object anObject )
        throws Exception
    {
        return readMethod.invoke( anObject );
    }
}
