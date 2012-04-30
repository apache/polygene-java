package com.marcgrue.dcisample_a.infrastructure.model;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Callback Wicket model that holds a Qi4j Query object that can be called when needed to
 * retrieve fresh data.
 */
public abstract class QueryModel<T, U extends EntityComposite>
      extends ReadOnlyModel<List<T>>
{
    private Class<T> dtoClass;
    private transient List<T> dtoList;

    public QueryModel( Class<T> dtoClass )
    {
        this.dtoClass = dtoClass;
    }

    public List<T> getObject()
    {
        if (dtoList != null)
            return dtoList;

        dtoList = new ArrayList<T>();
        for (U entity : getQuery())
            dtoList.add( getValue( entity ) );

        return dtoList;
    }

    // Callback to retrieve the (unserializable) Qi4j Query object
    public abstract Query<U> getQuery();

    public T getValue( U entity )
    {
        return valueConverter.convert( dtoClass, entity );
    }

    public void detach()
    {
        dtoList = null;
    }
}