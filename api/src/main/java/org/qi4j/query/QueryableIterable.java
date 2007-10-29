package org.qi4j.query;

/**
 * TODO
 */
public class QueryableIterable
    implements Queryable
{
    Iterable source;

    public QueryableIterable( Iterable source )
    {
        this.source = source;
    }

    public <T> T find( Query<T> query )
    {
        QueryImpl<T> impl = (QueryImpl<T>) query;

        for( Object o : source )
        {
            return (T) o;
        }

        return null;
    }

    public <T> Iterable<T> iterable( Query<T> query )
    {
        return source;
    }
}
