package org.qi4j.api.persistence.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import org.qi4j.api.persistence.Query.OrderBy;

/**
 * TODO
 */
public class OrderedIterable<T> implements Iterable<T>
{
    private Iterable<T> iterable;
    private Method orderByMethod;
    private OrderBy order;

    public OrderedIterable( Iterable<T> iterable, Method orderByMethod, OrderBy orderBy )
    {
        this.iterable = iterable;
        this.orderByMethod = orderByMethod;
        this.order = orderBy;
    }

    public Iterator<T> iterator()
    {
        List<T> objects = new ArrayList<T>();
        for( T t : iterable )
        {
            objects.add( t);
        }

        Collections.sort( objects, new OrderingComparator<T>());

        return objects.iterator();
    }

    class OrderingComparator<T>
        implements Comparator<T>
    {
        IdentityHashMap comparisonValues = new IdentityHashMap( );

        public int compare( T t1, T t2 )
        {
            Object v1 = getValue(t1);
            Object v2 = getValue(t2);

            int comparisonValue;

            if (v1 != null)
                comparisonValue = ((Comparable)v1).compareTo( v2);
            else
                comparisonValue = 1;

            if ( order == OrderBy.DESCENDING)
                comparisonValue = -comparisonValue;

            return comparisonValue;
        }

        Object getValue(Object anObject)
        {
            Object value = comparisonValues.get( anObject);
            if (value == null)
            {
                try
                {
                    value = orderByMethod.invoke( anObject);
                    comparisonValues.put( anObject, value);
                }
                catch( IllegalAccessException e )
                {
                    // Ignore
                }
                catch( InvocationTargetException e )
                {
                    // Ignore
                }
            }
            return value;
        }
    }
}
