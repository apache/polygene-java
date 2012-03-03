package org.qi4j.api.dataset;

import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryException;
import org.qi4j.api.query.QueryExecutionException;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Visitor;

/**
 * TODO
 */
public interface Query<T>
{
    public enum Order
    {
        ASCENDING, DESCENDING
    }

    Query filter( Specification<T> filter );

    Query orderBy( final Property<?> property, final Order order );

    Query skip( int skipNrOfResults );

    Query limit( int maxNrOfResults );

    // Variables
    Query<T> setVariable( String name, Object value );

    Object getVariable( String name );

    long count()
        throws QueryExecutionException;

    T first()
        throws QueryExecutionException;

    T single()
        throws QueryException;

    <ThrowableType extends Throwable> boolean execute( Visitor<T, ThrowableType> resultVisitor )
        throws ThrowableType, QueryExecutionException;

    Iterable<T> toIterable()
        throws QueryExecutionException;
}
