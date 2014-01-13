/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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
package org.qi4j.functional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility functions. Combine these with methods in Iterables, for example. See FunctionsTest for usages.
 */
public final class Functions
{
    public static <A, B, C> Function2<Function<? super B, C>, Function<A, B>, Function<A, C>> compose()
    {
        return new Function2<Function<? super B, C>, Function<A, B>, Function<A, C>>()
        {
            @Override
            public Function<A, C> map( Function<? super B, C> bcFunction, Function<A, B> abFunction )
            {
                return compose( bcFunction, abFunction );
            }
        };
    }

    /**
     * compose(F1(M,T),F2(F,M)) = F1(F2(F)) -> T
     *
     * @param outer
     * @param inner
     * @param <FROM>
     * @param <MIDDLE>
     * @param <TO>
     *
     * @return
     */
    public static <FROM, MIDDLE, TO> Function<FROM, TO> compose( final Function<? super MIDDLE, TO> outer,
                                                                 final Function<FROM, MIDDLE> inner
    )
    {
        return new Function<FROM, TO>()
        {
            @Override
            public TO map( FROM from )
            {
                return outer.map( inner.map( from ) );
            }
        };
    }

    public static <TO, FROM extends TO> Function<FROM, TO> identity()
    {
        return new Function<FROM, TO>()
        {
            @Override
            public TO map( FROM from )
            {
                return from;
            }
        };
    }

    public static <FROM, TO> Function<FROM, TO> fromMap( final Map<FROM, TO> map )
    {
        return new Function<FROM, TO>()
        {
            @Override
            public TO map( FROM from )
            {
                return map.get( from );
            }
        };
    }

    public static <T> Function<T, T> withDefault( final T defaultValue )
    {
        return new Function<T, T>()
        {
            @Override
            public T map( T from )
            {
                if( from == null )
                {
                    return defaultValue;
                }
                else
                {
                    return from;
                }
            }
        };
    }

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
     * Sample usage: last( map( count( in( "X" ) ), iterable( "X","Y","X","X","Y" ) ) )
     * Returns: 3
     *
     * @param specification
     * @param <T>
     *
     * @return
     */
    public static <T> Function<T, Integer> count( final Specification<T> specification )
    {
        return new Function<T, Integer>()
        {
            int count;

            @Override
            public Integer map( T item )
            {
                if( specification.satisfiedBy( item ) )
                {
                    count++;
                }

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
     *
     * @return
     */
    public static <T> Function<T, Integer> indexOf( final Specification<T> specification )
    {
        return new Function<T, Integer>()
        {
            int index = -1;
            int current = 0;

            @Override
            public Integer map( T item )
            {
                if( index == -1 && specification.satisfiedBy( item ) )
                {
                    index = current;
                }

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
     *
     * @return
     */
    @SuppressWarnings( "unchecked" )
    public static <T> int indexOf( T item, Iterable<T> iterable )
    {
        return Iterables.first( Iterables.filter( Specifications.not( Specifications.in( -1 ) ),
                                                  Iterables.map( indexOf( Specifications.in( item ) ), iterable ) ) );
    }

    /**
     * Only apply given function on objects that satisfies the given specification.
     *
     * @param specification
     * @param function
     * @param <T>
     *
     * @return
     */
    public static <T> Function<T, T> filteredMap( final Specification<T> specification, final Function<T, T> function )
    {
        return new Function<T, T>()
        {
            @Override
            public T map( T from )
            {
                return specification.satisfiedBy( from ) ? function.map( from ) : from;
            }
        };
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
     *
     * @return
     */
    @SuppressWarnings( "raw" )
    public static <T> Comparator<T> comparator( final Function<T, Comparable> comparableFunction )
    {
        return new Comparator<T>()
        {
            Map<T, Comparable> compareKeys = new HashMap<>();

            @Override
            @SuppressWarnings( "unchecked" )
            public int compare( T o1, T o2 )
            {
                Comparable key1 = compareKeys.get( o1 );
                if( key1 == null )
                {
                    key1 = comparableFunction.map( o1 );
                    compareKeys.put( o1, key1 );
                }

                Comparable key2 = compareKeys.get( o2 );
                if( key2 == null )
                {
                    key2 = comparableFunction.map( o2 );
                    compareKeys.put( o2, key2 );
                }

                return key1.compareTo( key2 );
            }
        };
    }

    private Functions()
    {
    }
}
