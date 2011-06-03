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

package org.qi4j.api.util;

import java.text.MessageFormat;
import java.util.*;

import org.qi4j.api.specification.Specification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for working with Iterables. See test for examples of how to use.
 *
 */
public class Iterables
{
    private static Logger debugLogger = LoggerFactory.getLogger( Iterables.class );

    public static <T,C extends Collection<T>> C addAll( C collection, Iterable<? extends T> iterable )
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
        Iterator<?> iterator = iterable.iterator();
        while( iterator.hasNext() )
        {
            iterator.next();
            c++;
        }
        return c;
    }

    public static <X> Iterable<X> filter( Specification<? super X> specification, Iterable<X> i )
    {
        return new FilterIterable<X>( i, specification );
    }

    public static <X> X first( Iterable<? extends X> i )
    {
        Iterator<? extends X> iter = i.iterator();
        if( iter.hasNext() )
        {

            return iter.next();
        }
        else
        {
            return null;
        }
    }

    public static <X> X last( Iterable<? extends X> i )
    {
        Iterator<? extends X> iter = i.iterator();
        X item = null;
        while (iter.hasNext())
            item = iter.next();

        return item;
    }

    public static <X> Iterable<X> reverse(Iterable<X> iterable)
    {
        ArrayList<X> list = addAll( new ArrayList<X>(), iterable );
        Collections.reverse( list );

        return list;
    }

    public static <T> boolean matchesAny( Specification<? super T> specification, Iterable<T> iterable )
    {
        boolean result = false;

        for( T item : iterable )
        {
            if( specification.satisfiedBy( item ) )
            {
                result = true;
                break;
            }
        }

        return result;
    }

    public static <T> boolean matchesAll( Specification<? super T> specification, Iterable<T> iterable )
    {
        boolean result = true;
        for( T item : iterable )
        {
            if( !specification.satisfiedBy( item ) )
            {
                result = false;
            }
        }

        return result;
    }

    public static <X,I extends Iterable<? extends X>> Iterable<X> flatten( I... multiIterator )
    {
        return new FlattenIterable<X,I>( Arrays.asList( multiIterator ) );
    }

    public static <X,I extends Iterable<? extends X>> Iterable<X> flattenIterables( Iterable<I> multiIterator )
    {
        return new FlattenIterable<X,I>( multiIterator );
    }

    public static <FROM, TO> Iterable<TO> map( Function<? super FROM, TO> function, Iterable<FROM> from )
    {
        return new MapIterable<FROM, TO>( from, function );
    }

    public static <T> Iterable<T> iterable( Enumeration<T> enumeration )
    {
        List<T> list = new ArrayList<T>();
        while( enumeration.hasMoreElements() )
        {
            T item = enumeration.nextElement();
            list.add( item );
        }

        return list;
    }

    public static <T, C extends T> Iterable<T> iterable( C... items )
    {
        return (Iterable<T>) Arrays.asList( items );
    }

    public static <FROM, TO> TO fold( Function<? super FROM, TO> function, Iterable<? extends FROM> i )
    {
        return last(map(function, i));
    }

    public static <T> Iterable<T> debug(String format, final Iterable<T> iterable, final Function<T, String>... functions)
    {
        final MessageFormat msgFormat = new MessageFormat(format);

        return map( new Function<T, T>()
        {
            @Override
            public T map( T t )
            {
                if (functions.length == 0)
                    debugLogger.info( msgFormat.format( new Object[]{t} ) );
                else
                {
                    String[] mapped = new String[functions.length];
                    for( int i = 0; i < functions.length; i++ )
                    {
                        Function<T, String> function = functions[i];
                        mapped[i] = function.map( t );
                        debugLogger.info( msgFormat.format( mapped ) );
                    }
                }

                return t;
            }
        }, iterable );
    }

    private static class MapIterable<FROM, TO>
        implements Iterable<TO>
    {
        private final Iterable<FROM> from;
        private final Function<? super FROM, TO> function;

        public MapIterable( Iterable<FROM> from, Function<? super FROM, TO> function )
        {
            this.from = from;
            this.function = function;
        }

        public Iterator<TO> iterator()
        {
            return new MapIterator<FROM, TO>( from.iterator(), function );
        }

        static class MapIterator<FROM, TO>
            implements Iterator<TO>
        {
            private final Iterator<FROM> fromIterator;
            private final Function<? super FROM, TO> function;

            public MapIterator( Iterator<FROM> fromIterator, Function<? super FROM, TO> function )
            {
                this.fromIterator = fromIterator;
                this.function = function;
            }

            public boolean hasNext()
            {
                return fromIterator.hasNext();
            }

            public TO next()
            {
                FROM from = fromIterator.next();

                return function.map( from );
            }

            public void remove()
            {
                fromIterator.remove();
            }
        }
    }

    private static class FilterIterable<T>
        implements Iterable<T>
    {
        private Iterable<T> iterable;

        private Specification<? super T> specification;

        public FilterIterable( Iterable<T> iterable, Specification<? super T> specification )
        {
            this.iterable = iterable;
            this.specification = specification;
        }

        public Iterator<T> iterator()
        {
            return new FilterIterator<T>( iterable.iterator(), specification );
        }

        static class FilterIterator<T>
            implements Iterator<T>
        {
            private Iterator<T> iterator;

            private Specification<? super T> specification;

            private T currentValue;
            boolean finished = false;
            boolean nextConsumed = true;

            public FilterIterator( Iterator<T> iterator, Specification<? super T> specification )
            {
                this.specification = specification;
                this.iterator = iterator;
            }

            public boolean moveToNextValid()
            {
                boolean found = false;
                while( !found && iterator.hasNext() )
                {
                    T currentValue = iterator.next();
                    boolean satisfies = specification.satisfiedBy( currentValue );

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

            public boolean hasNext()
            {
                return !finished &&
                       ( !nextConsumed || moveToNextValid() );
            }

            public void remove()
            {
            }
        }
    }

    private static class FlattenIterable<T,I extends Iterable<? extends T>>
        implements Iterable<T>
    {
        private Iterable<I> iterable;

        public FlattenIterable( Iterable<I> iterable )
        {
            this.iterable = iterable;
        }

        public Iterator<T> iterator()
        {
            return new FlattenIterator<T,I>( iterable.iterator() );
        }

        static class FlattenIterator<T,I extends Iterable<? extends T>>
            implements Iterator<T>
        {
            private Iterator<I> iterator;
            private Iterator<? extends T> currentIterator;

            public FlattenIterator( Iterator<I> iterator )
            {
                this.iterator = iterator;
                currentIterator = null;
            }

            public boolean hasNext()
            {
                if( currentIterator == null )
                {
                    if( iterator.hasNext() )
                    {
                        I next = iterator.next();
                        currentIterator = next.iterator();
                    }
                    else
                    {
                        return false;
                    }
                }

                while( !currentIterator.hasNext() &&
                       iterator.hasNext() )
                {
                    currentIterator = iterator.next().iterator();
                }

                return currentIterator.hasNext();
            }

            public T next()
            {
                return currentIterator.next();
            }

            public void remove()
            {
                if( currentIterator == null )
                {
                    throw new IllegalStateException();
                }

                currentIterator.remove();
            }
        }
    }
}
