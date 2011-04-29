package org.qi4j.api.util;

import org.qi4j.api.specification.Specification;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static org.qi4j.api.specification.Specifications.in;
import static org.qi4j.api.specification.Specifications.not;
import static org.qi4j.api.util.Iterables.filter;
import static org.qi4j.api.util.Iterables.first;
import static org.qi4j.api.util.Iterables.map;

/**
 * Utility functions. Combine these with methods in Iterables, for example. See FunctionsTest for usages.
 */
public class Functions
{
    public static Function<Number, Long> longSum()
    {
        return new Function<Number, Long>()
        {
            long sum;

            @Override
            public Long map( Number number )
            {
                sum += number.longValue();
                return sum;
            }
        };
    }

    public static Function<Number, Integer> intSum()
    {
        return new Function<Number, Integer>()
        {
            int sum;

            @Override
            public Integer map( Number number )
            {
                sum += number.intValue();
                return sum;
            }
        };
    }

    /**
     * Count the number of items in an iterable that matches a given specification.
     *
     * Sample usage: last( map( indexOf( in( "D" ) ), iterable( "A","B","C","D","D" ) ) )
     * Returns: 3
     *
     * @param specification
     * @param <T>
     * @return
     */
    public static <T> Function<T, Integer> count( final Specification<T> specification)
    {
        return new Function<T, Integer>()
        {
            int count;

            @Override
            public Integer map( T item )
            {
                if (specification.satisfiedBy( item ))
                    count++;

                return count;
            }
        };
    }

    /**
     * Find out the index of an item matching a given specification in an iterable.
     * Returns -1 if it is not found.
     *
     * @param specification
     * @param <T>
     * @return
     */
    public static <T> Function<T, Integer> indexOf( final Specification<T> specification)
    {
        return new Function<T, Integer>()
        {
            int index = -1;
            int current = 0;

            @Override
            public Integer map( T item )
            {
                if (index == -1 && specification.satisfiedBy( item ))
                    index = current;

                current++;

                return index;
            }
        };
    }

    /**
     * Find out the index of an item in an iterable.
     *
     * @param item
     * @param iterable
     * @param <T>
     * @return
     */
    public static <T> int indexOf(T item, Iterable<T> iterable)
    {
        return first( filter( not( in( -1 ) ), map( indexOf( in( item ) ), iterable ) ) );
    }

    /**
     * Creates a comparator that takes a function as input. The returned comparator will use the
     * function once for each item in the list to be sorted by Collections.sort.
     *
     * This should be used if the function to generate the sort key from an object is expensive, so
     * that it is not done many times for each item in a list.
     *
     * @param comparableFunction
     * @param <T>
     * @return
     */
    public static <T> Comparator<T> comparator( final Function<T, Comparable> comparableFunction)
    {
        return new Comparator<T>()
        {
            Map<T, Comparable> compareKeys = new HashMap<T, Comparable>();

            public int compare( T o1, T o2 )
            {
                Comparable key1 = compareKeys.get( o1 );
                if (key1 == null)
                {
                    key1 = comparableFunction.map( o1 );
                    compareKeys.put(o1, key1);
                }

                Comparable key2 = compareKeys.get( o2 );
                if (key2 == null)
                {
                    key2 = comparableFunction.map( o2 );
                    compareKeys.put(o2, key2);
                }

                return key1.compareTo( key2 );
            }
        };
    }
}
