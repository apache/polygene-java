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
package org.apache.zest.functional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility methods for working with Iterables. See test for examples of how to use.
 */
public final class Iterables
{
    private static <T, C extends Collection<T>> C addAll( C collection, Iterable<? extends T> iterable )
    {
        for( T item : iterable )
        {
            collection.add( item );
        }
        return collection;
    }

    public static long count( Iterable<?> iterable )
    {
        long c = 0;
        for( Object anIterable : iterable )
        {
            c++;
        }
        return c;
    }

    @SuppressWarnings( "unchecked" )
    public static <X> Iterable<X> filter( Predicate<? /* super X*/> specification, Iterable<X> i )
    {
        return new FilterIterable<>( i, (Predicate<? super X>) specification );
    }

    @SuppressWarnings( "unchecked" )
    public static <FROM, TO> Iterable<TO> map( Function<? /* super FROM */, TO> function, Iterable<FROM> from )
    {
        return new MapIterable<>( from, (Function<FROM, TO>) function );
    }

    @SafeVarargs
    public static <T> Iterable<T> iterable( T... items )
    {
        return Arrays.asList( items );
    }

    public static <T> Iterable<T> prepend( final T item, final Iterable<T> iterable )
    {
        return () -> new Iterator<T>()
        {
            private T first = item;
            private Iterator<T> iterator;

            @Override
            public boolean hasNext()
            {
                if( first != null )
                {
                    return true;
                }
                else
                {
                    if( iterator == null )
                    {
                        iterator = iterable.iterator();
                    }
                }

                return iterator.hasNext();
            }

            @Override
            public T next()
            {
                if( first != null )
                {
                    try
                    {
                        return first;
                    }
                    finally
                    {
                        first = null;
                    }
                }
                else
                {
                    return iterator.next();
                }
            }

            @Override
            public void remove()
            {
            }
        };
    }

    public static <T> List<T> toList( Iterable<T> iterable )
    {
        return addAll( new ArrayList<>(), iterable );
    }

    private static class MapIterable<FROM, TO>
        implements Iterable<TO>
    {
        private final Iterable<FROM> from;
        private final Function<? super FROM, TO> function;

        private MapIterable( Iterable<FROM> from, Function<? super FROM, TO> function )
        {
            this.from = from;
            this.function = function;
        }

        @Override
        public Iterator<TO> iterator()
        {
            return new MapIterator<>( from.iterator(), function );
        }

        static class MapIterator<FROM, TO>
            implements Iterator<TO>
        {
            private final Iterator<FROM> fromIterator;
            private final Function<? super FROM, TO> function;

            private MapIterator( Iterator<FROM> fromIterator, Function<? super FROM, TO> function )
            {
                this.fromIterator = fromIterator;
                this.function = function;
            }

            @Override
            public boolean hasNext()
            {
                return fromIterator.hasNext();
            }

            @Override
            public TO next()
            {
                FROM from = fromIterator.next();
                return function.apply( from );
            }

            @Override
            public void remove()
            {
                fromIterator.remove();
            }
        }
    }

    private static class FilterIterable<T>
        implements Iterable<T>
    {
        private final Iterable<T> iterable;

        private final Predicate<? super T> specification;

        private FilterIterable( Iterable<T> iterable, Predicate<? super T> specification )
        {
            this.iterable = iterable;
            this.specification = specification;
        }

        @Override
        public Iterator<T> iterator()
        {
            return new FilterIterator<>( iterable.iterator(), specification );
        }

        private static class FilterIterator<T>
            implements Iterator<T>
        {
            private final Iterator<T> iterator;

            private final Predicate<? super T> specification;

            private T currentValue;
            boolean finished = false;
            boolean nextConsumed = true;

            private FilterIterator( Iterator<T> iterator, Predicate<? super T> specification )
            {
                this.specification = specification;
                this.iterator = iterator;
            }

            private boolean moveToNextValid()
            {
                boolean found = false;
                while( !found && iterator.hasNext() )
                {
                    T currentValue = iterator.next();
                    boolean satisfies = specification.test( currentValue );

                    if( satisfies )
                    {
                        found = true;
                        this.currentValue = currentValue;
                        nextConsumed = false;
                    }
                }
                if( !found )
                {
                    finished = true;
                }
                return found;
            }

            @Override
            public T next()
            {
                if( !nextConsumed )
                {
                    nextConsumed = true;
                    return currentValue;
                }
                else
                {
                    if( !finished )
                    {
                        if( moveToNextValid() )
                        {
                            nextConsumed = true;
                            return currentValue;
                        }
                    }
                }
                return null;
            }

            @Override
            public boolean hasNext()
            {
                return !finished && ( !nextConsumed || moveToNextValid() );
            }

            @Override
            public void remove()
            {
            }
        }
    }

    private Iterables()
    {
    }
}
