/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_b.infrastructure.model;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.query.Query;

/**
 * QueryModel
 *
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
        if( dtoList != null )
        {
            return dtoList;
        }

        dtoList = new ArrayList<T>();
        for( U entity : getQuery() )
        {
            dtoList.add( getValue( entity ) );
        }

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