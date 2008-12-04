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

import org.qi4j.query.Query;
import org.qi4j.query.QueryExecutionException;
import org.qi4j.query.grammar.OrderBy;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.named.NamedEntityFinder;
import org.qi4j.spi.entity.QualifiedIdentity;
import java.util.Iterator;
import java.util.HashMap;

public class NamedQueryImpl<T>
    implements Query<T>
{
    private OrderBy[] orderBySegments;
    private int firstResult;
    private int maxResults;
    private HashMap<String, Object> variables;
    private Class<T> resultType;
    private String queryName;
    private UnitOfWorkInstance unitOfWorkInstance;
    private NamedEntityFinder namedFinder;

    public NamedQueryImpl( NamedEntityFinder namedFinder, UnitOfWorkInstance unitOfWorkInstance, String queryName, Class<T> resultType )
    {
        this.namedFinder = namedFinder;
        this.unitOfWorkInstance = unitOfWorkInstance;
        this.queryName = queryName;
        this.resultType = resultType;
        this.variables = new HashMap<String, Object>();
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

    public void setVariable( String name, Object value )
    {
        Object oldvalue = variables.put( name, value );
        if( oldvalue == null )
        {
            variables.remove( name );
            throw new IllegalArgumentException( "Variable [" + name + "] is not found." );
        }
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
        QualifiedIdentity foundEntity;
        try
        {
            foundEntity = namedFinder.findEntity( queryName, resultType.getName(), variables );
        }
        catch( EntityFinderException e )
        {
            throw new QueryExecutionException( "Finder caused exception", e );
        }

        if( foundEntity != null )
        {
            try
            {
                ModuleInstance moduleInstance = unitOfWorkInstance.module();
                ClassLoader loader = moduleInstance.classLoader();
                final Class<T> entityType = (Class<T>) loader.loadClass( foundEntity.type() );
                return unitOfWorkInstance.getReference( foundEntity.identity(), entityType );
            }
            catch( ClassNotFoundException e )
            {
                throw new QueryExecutionException( "Entity type not found", e );
            }
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
            return namedFinder.countEntities( queryName, resultType.getName(), variables );
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
            final Iterator<QualifiedIdentity> foundEntities = namedFinder.findEntities( queryName,
                resultType.getName(), variables, orderBySegments, firstResult, maxResults
            ).iterator();

            return new Iterator<T>()
            {
                public boolean hasNext()
                {
                    return foundEntities.hasNext();
                }

                public T next()
                {
                    QualifiedIdentity foundEntity = foundEntities.next();
                    final Class<T> entityType = unitOfWorkInstance.module().findClassForName( foundEntity.type() );
                    // TODO shall we throw an exception if class cannot be found?
                    final T entity = unitOfWorkInstance.getReference( foundEntity.identity(), entityType );
                    return entity;
                }

                public void remove()
                {
                }
            };
        }
        catch( EntityFinderException e )
        {
            throw (QueryExecutionException) new QueryExecutionException( "Query '" + toString() + "' could not be executed" ).initCause( e );
        }
    }
}
