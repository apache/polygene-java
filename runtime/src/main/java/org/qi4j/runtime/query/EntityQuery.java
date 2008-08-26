/*
 * Copyright 2007 Niclas Hedhman.
 * Copyright 2008 Alin Dreghiciu.
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

import java.util.Iterator;
import org.qi4j.query.Query;
import org.qi4j.query.QueryExecutionException;
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;

/**
 * Default implementation of {@link Query}
 *
 * @author Alin Dreghiciu
 * @since 0.2.0, April 14, 2008
 */
final class EntityQuery<T>
    extends AbstractQuery<T>
{
    private static final long serialVersionUID = 1L;

    /**
     * Parent unit of work.
     */
    private final UnitOfWorkInstance unitOfWorkInstance;
    /**
     * Entity finder to be used to locate entities.
     */
    private final EntityFinder entityFinder;

    /**
     * Constructor.
     *
     * @param unitOfWorkInstance parent unit of work; cannot be null
     * @param entityFinder       entity finder to be used to locate entities; cannot be null
     * @param resultType         type of queried entities; cannot be null
     * @param whereClause        where clause
     */
    EntityQuery( final UnitOfWorkInstance unitOfWorkInstance,
                   final EntityFinder entityFinder,
                   final Class<T> resultType,
                   final BooleanExpression whereClause )
    {
        super( resultType, whereClause );
        this.unitOfWorkInstance = unitOfWorkInstance;
        this.entityFinder = entityFinder;
    }

    /**
     * @see Query#find()
     */
    public T find()
    {
        QualifiedIdentity foundEntity;
        try
        {
            foundEntity = entityFinder.findEntity( resultType.getName(), whereClause );
        }
        catch( EntityFinderException e )
        {
            throw new QueryExecutionException( "Finder caused exception", e );
        }

        if( foundEntity != null )
        {
            try
            {
                final Class<T> entityType = (Class<T>) unitOfWorkInstance.module().classLoader().loadClass( foundEntity.type() );
                final T entity = unitOfWorkInstance.getReference( foundEntity.identity(), entityType );
                return entity;
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

    /**
     * @see Query#iterator()
     */
    public Iterator<T> iterator()
    {
        try
        {
            final Iterator<QualifiedIdentity> foundEntities = entityFinder.findEntities(
                resultType.getName(), whereClause, orderBySegments, firstResult, maxResults
            ).iterator();

            return new Iterator<T>()
            {
                public boolean hasNext()
                {
                    return foundEntities.hasNext();
                }

                public T next()
                {
                    final QualifiedIdentity foundEntity = foundEntities.next();

                    final ModuleInstance moduleInstance = unitOfWorkInstance.module();
                    final String entityTypeAsString = foundEntity.type();
                    final Class<T> entityType = moduleInstance.findClassForName( entityTypeAsString );

                    // TODO shall we throw an exception if class cannot be found?
                    final String entityIdentity = foundEntity.identity();
                    final T entity = unitOfWorkInstance.getReference( entityIdentity, entityType );
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

    @Override public String toString()
    {
        return "Find all " + resultType.getName() +
               ( whereClause != null ? " where " + whereClause.toString() : "" );
    }

    public long count()
    {
        try
        {
            return entityFinder.countEntities( resultType.getName(), whereClause );
        }
        catch( EntityFinderException e )
        {
            e.printStackTrace();
            return 0;
        }
    }
}