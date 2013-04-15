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
        return new ForEach<T>( iterable );
    }

    private Iterable<T> iterable;

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
        return new ForEach<T>( Iterables.filter( specification, iterable ) );
    }

    public <TO> ForEach<TO> map( Function<? /* super T */, TO> function )
    {
        return new ForEach<TO>( Iterables.map( function, iterable ) );
    }

    public <TO> ForEach<TO> flatten()
    {
        Iterable<Iterable<TO>> original = iterable();
        Iterable<TO> iterable1 = Iterables.flattenIterables( original );
        return new ForEach<TO>( iterable1 );
    }

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
