package org.qi4j.api.dataset.iterable;

import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.dataset.Query;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryException;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Visitor;

/**
 * TODO
 */
public class IterableQuery<T> implements Query<T>
{
    private Iterable<T> iterable;
    private int skip;
    private int limit;
    private Map<String, Object> variables = new HashMap<String, Object>();

    public IterableQuery( Iterable<T> iterable )
    {
        this.iterable = iterable;
    }

    @Override
    public Query filter( Specification<T> filter )
    {
        iterable = Iterables.filter( filter, iterable );

        return this;
    }

    @Override
    public Query orderBy( Property<?> property, Order order )
    {
        return this;
    }

    @Override
    public Query skip( int skipNrOfResults )
    {
        this.skip = skipNrOfResults;

        return this;
    }

    @Override
    public Query limit( int maxNrOfResults )
    {
        this.limit = maxNrOfResults;
        return this;
    }

    @Override
    public Query<T> setVariable( String name, Object value )
    {
        variables.put( name, value );
        return this;
    }

    @Override
    public Object getVariable( String name )
    {
        return variables.get( name );
    }

    @Override
    public long count()
    {
        return Iterables.count( Iterables.limit( limit, Iterables.skip( skip, iterable ) ) );
    }

    @Override
    public T first()
    {
        return Iterables.first( Iterables.limit( limit, Iterables.skip( skip, iterable ) ) );
    }

    @Override
    public T single()
        throws QueryException
    {
        return Iterables.single( Iterables.limit( limit, Iterables.skip( skip, iterable ) ) );
    }

    @Override
    public <ThrowableType extends Throwable> boolean execute( Visitor<T, ThrowableType> resultVisitor )
        throws ThrowableType
    {
        for( T t : toIterable() )
        {
            if( !resultVisitor.visit( t ) )
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public Iterable<T> toIterable()
        throws QueryException
    {
        return Iterables.limit( limit, Iterables.skip( skip, iterable ) );
    }
}
