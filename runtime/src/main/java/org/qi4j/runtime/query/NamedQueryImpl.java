/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.runtime.query;

import java.util.HashMap;
import java.util.Iterator;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExecutionException;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueryDescriptor;

public class NamedQueryImpl<T>
    implements Query<T>
{
    private static final Object VARIABLE_NOT_SET = new Object();

    private OrderBy[] orderBySegments;
    private int firstResult;
    private int maxResults;
    private HashMap<String, Object> variables;
    private NamedQueryDescriptor query;
    private Class<T> resultType;
    private UnitOfWork unitOfWork;
    private NamedEntityFinder namedFinder;

    public NamedQueryImpl( NamedEntityFinder namedFinder, UnitOfWork unitOfWork,
                           NamedQueryDescriptor query, Class<T> resultType
    )
    {
        this.namedFinder = namedFinder;
        this.unitOfWork = unitOfWork;
        this.query = query;
        this.resultType = resultType;
        this.variables = new HashMap<String, Object>();
        for( String variableName : query.variableNames() )
        {
            this.variables.put( variableName, VARIABLE_NOT_SET );
        }
    }

    public Query<T> orderBy( OrderBy... segments )
    {
        orderBySegments = segments;
        return this;
    }

    public Query<T> firstResult( int firstResult )
    {
        this.firstResult = firstResult;
        return this;
    }

    public Query<T> maxResults( int maxResults )
    {
        this.maxResults = maxResults;
        return this;
    }

    public Query<T> setVariable( String name, Object value )
    {
        Object oldvalue = variables.put( name, value );
        if( oldvalue == null )
        {
            variables.remove( name );
            throw new IllegalArgumentException( "Variable [" + name + "] is not found." );
        }

        return this;
    }

    public <V> V getVariable( String name )
    {
        Object value = variables.get( name );
        if( value == null )
        {
            throw new IllegalArgumentException( "Variable [" + name + "] is not found." );
        }
        return (V) value;
    }

    public Class<T> resultType()
    {
        return resultType;
    }

    public T find()
    {
        EntityReference foundEntity;
        try
        {
            foundEntity = namedFinder.findEntity( query, resultType.getName(), variables );
        }
        catch( EntityFinderException e )
        {
            throw new QueryExecutionException( "Finder caused exception", e );
        }

        if( foundEntity != null )
        {
            return unitOfWork.get( resultType, foundEntity.identity() );
        }
        else
        {
            // No entity was found
            return null;
        }
    }

    public long count()
    {
        try
        {
            return namedFinder.countEntities( query, resultType.getName(), variables );
        }
        catch( EntityFinderException e )
        {
            e.printStackTrace();
            return 0;
        }
    }

    public Iterator<T> iterator()
    {
        try
        {
            final Iterator<EntityReference> foundEntities =
                namedFinder.findEntities( query, resultType.getName(), variables,
                                          orderBySegments, firstResult, maxResults
                ).iterator();

            return new Iterator<T>()
            {
                public boolean hasNext()
                {
                    return foundEntities.hasNext();
                }

                public T next()
                {
                    EntityReference foundEntity = foundEntities.next();
                    return unitOfWork.get( resultType, foundEntity.identity() );
                }

                public void remove()
                {
                }
            };
        }
        catch( EntityFinderException e )
        {
            throw (QueryExecutionException) new QueryExecutionException( "Query '" + toString() + "' could not be executed" )
                .initCause( e );
        }
    }

    public String toString()
    {
        return query.name() + " : [" + namedFinder.showQuery( query ) + " ]";
    }
}
