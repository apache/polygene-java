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

import java.util.Iterator;

/**
 * When using Iterables with map() and filter() the code often reads "in reverse", with the first item last in the code.
 * Example: Iterables.map(function,Iterables.filter(specification, iterable))
 * <p/>
 * This ForEach class reverses that order and makes the code more readable, and allows easy application of visitors on iterables.
 * Example: forEach(iterable).filter(specification).map(function).visit(visitor)
 */
public final class ForEach<T>
    implements Iterable<T>
{
    public static <T> ForEach<T> forEach( Iterable<T> iterable )
    {
        return new ForEach<>( iterable );
    }

    private final Iterable<T> iterable;

    public ForEach( Iterable<T> iterable )
    {
        this.iterable = iterable;
    }

    @Override
    public Iterator<T> iterator()
    {
        return iterable.iterator();
    }

    public ForEach<T> filter( Specification<? super T> specification )
    {
        return new ForEach<>( Iterables.filter( specification, iterable ) );
    }

    public <TO> ForEach<TO> map( Function<? /* super T */, TO> function )
    {
        return new ForEach<>( Iterables.map( function, iterable ) );
    }

    public <TO> ForEach<TO> flatten()
    {
        Iterable<Iterable<TO>> original = iterable();
        Iterable<TO> iterable1 = Iterables.flattenIterables( original );
        return new ForEach<>( iterable1 );
    }

    @SuppressWarnings( "unchecked" )
    private <TO> Iterable<Iterable<TO>> iterable()
    {
        return (Iterable<Iterable<TO>>) iterable;
    }

    public T last()
    {
        T lastItem = null;
        for( T item : iterable )
        {
            lastItem = item;
        }
        return lastItem;
    }

    public <ThrowableType extends Throwable> boolean visit( final Visitor<T, ThrowableType> visitor )
        throws ThrowableType
    {
        for( T item : iterable )
        {
            if( !visitor.visit( item ) )
            {
                return false;
            }
        }

        return true;
    }
}
