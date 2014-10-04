package org.qi4j.runtime.association;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

public class GenericSpliterator<T, R> extends Spliterators.AbstractSpliterator<R>
{
    private final Iterator<T> it;
    private final Function<T, R> mapping;

    public GenericSpliterator( Iterator<T> it, Function<T, R> mapping )
    {
        super( 0L, Spliterator.IMMUTABLE );
        this.it = it;
        if( mapping == null )
        {
            //noinspection unchecked
            this.mapping = t -> (R) t;
        }
        else
        {
            this.mapping = mapping;
        }
    }

    @Override
    public boolean tryAdvance( Consumer<? super R> action )
    {
        if( it.hasNext() )
        {
            T value = it.next();
            action.accept( mapping.apply( value ) );
            return true;
        }
        return false;
    }
}
