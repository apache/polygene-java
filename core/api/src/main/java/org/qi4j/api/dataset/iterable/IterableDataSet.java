package org.qi4j.api.dataset.iterable;

import java.util.function.Function;
import java.util.function.Predicate;
import org.qi4j.api.dataset.DataSet;
import org.qi4j.api.dataset.Query;
import org.qi4j.functional.Iterables;

/**
 * TODO
 */
public class IterableDataSet<T>
    implements DataSet<T>
{
    private Iterable<T> iterable;

    public IterableDataSet( Iterable<T> iterable )
    {
        this.iterable = iterable;
    }

    @Override
    public DataSet<T> constrain( Predicate<T> selection )
    {
        return new IterableDataSet<T>( Iterables.filter( selection, iterable ) );
    }

    @Override
    public <U> DataSet<U> project( Function<T, U> conversion )
    {
        return new IterableDataSet<U>( Iterables.map( conversion, iterable ) );
    }

    @Override
    public Query<T> newQuery()
    {
        return new IterableQuery<T>( iterable );
    }
}
