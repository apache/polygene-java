package org.qi4j.api.query.decorator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.qi4j.api.query.Query;


/**
 * TODO
 */
public class ResultTypeQuery<T> extends QueryDecorator<T>
{
    private Collection<Class> resultTypes = new HashSet<Class>();

    public ResultTypeQuery( Query<T> query, Class<T> resultType )
    {
        super( query );
        resultType( resultType );
    }

    private ResultTypeQuery( ResultTypeQuery<T> copy )
    {
        super( copy.query.copy() );

        resultTypes = new ArrayList<Class>( copy.resultTypes );
    }

    public void resultType( Class mixinType )
    {
        if( !mixinType.equals( Object.class ) )
        {
            resultTypes.add( mixinType );
        }
    }

    public Iterable<T> prepare()
    {
        if( resultTypes.isEmpty() )
        {
            return query.prepare();
        }
        else
        {
            return new ResultTypeIterable<T>( query.prepare(), resultTypes );
        }
    }

    public Query<T> copy()
    {
        return new ResultTypeQuery<T>( this );
    }

    public T find()
    {
        if( resultTypes.isEmpty() )
        {
            return query.find();
        }

        Iterator<T> iterator = prepare().iterator();
        if( iterator.hasNext() )
        {
            return iterator.next();
        }
        else
        {
            return null;
        }
    }
}