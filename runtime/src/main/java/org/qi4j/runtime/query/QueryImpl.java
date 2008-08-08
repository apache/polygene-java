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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.qi4j.query.Query;
import org.qi4j.query.QueryExecutionException;
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.query.grammar.ComparisonPredicate;
import org.qi4j.query.grammar.Conjunction;
import org.qi4j.query.grammar.Disjunction;
import org.qi4j.query.grammar.Negation;
import org.qi4j.query.grammar.OrderBy;
import org.qi4j.query.grammar.SingleValueExpression;
import org.qi4j.query.grammar.ValueExpression;
import org.qi4j.query.grammar.impl.VariableValueExpression;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;

/**
 * Default implementation of {@link Query}
 *
 * @author Alin Dreghiciu
 * @since 0.2.0, April 14, 2008
 */
final class QueryImpl<T>
    implements Query<T>
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
     * Type of queried entities.
     */
    private final Class<T> resultType;
    /**
     * Where clause.
     */
    private final BooleanExpression whereClause;
    /**
     * Order by clause segments.
     */
    private OrderBy[] orderBySegments;
    /**
     * First result to be returned.
     */
    private Integer firstResult;
    /**
     * Maximum number of results to be returned.
     */
    private Integer maxResults;
    /**
     * Mapping between variable name and variable value expression.
     */
    private final Map<String, SingleValueExpression> variables;

    /**
     * Constructor.
     *
     * @param unitOfWorkInstance parent unit of work; cannot be null
     * @param entityFinder       entity finder to be used to locate entities; cannot be null
     * @param resultType         type of queried entities; cannot be null
     * @param whereClause        where clause
     */
    QueryImpl( final UnitOfWorkInstance unitOfWorkInstance,
               final EntityFinder entityFinder,
               final Class<T> resultType,
               final BooleanExpression whereClause )
    {
        this.unitOfWorkInstance = unitOfWorkInstance;
        this.entityFinder = entityFinder;
        this.resultType = resultType;
        this.whereClause = whereClause;
        this.variables = new HashMap<String, SingleValueExpression>();
        initializeVariables( whereClause );
    }

    private void initializeVariables( BooleanExpression anExpression )
    {
        if( anExpression instanceof Negation )
        {
            Negation negation = (Negation) anExpression;
            initializeVariables( negation.expression() );
        }
        else if( anExpression instanceof Disjunction )
        {
            Disjunction disjunction = (Disjunction) anExpression;
            initializeVariables( disjunction.leftSideExpression() );
            initializeVariables( disjunction.rightSideExpression() );
        }
        else if( anExpression instanceof Conjunction )
        {
            Conjunction conjunction = (Conjunction) anExpression;
            initializeVariables( conjunction.leftSideExpression() );
            initializeVariables( conjunction.rightSideExpression() );
        }
        else if( anExpression instanceof ComparisonPredicate )
        {
            ComparisonPredicate predicate = (ComparisonPredicate) anExpression;
            ValueExpression valueExpression = predicate.valueExpression();

            if( valueExpression instanceof VariableValueExpression )
            {
                VariableValueExpression variableValueExpression = (VariableValueExpression) valueExpression;
                variables.put( variableValueExpression.getName(), variableValueExpression );
            }
        }
    }

    /**
     * @see Query#orderBy(OrderBy[])
     */
    public Query<T> orderBy( final OrderBy... segments )
    {
        orderBySegments = segments;
        return this;
    }

    /**
     * @see Query#firstResult(int)
     */
    public Query<T> firstResult( int firstResult )
    {
        this.firstResult = firstResult;
        return this;
    }

    /**
     * @see Query#maxResults(int)
     */
    public Query<T> maxResults( int maxResults )
    {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * @see Query#find()
     */
    public T find()
    {
        try
        {
            final QualifiedIdentity foundEntity = entityFinder.findEntity(
                resultType.getName(), whereClause
            );

            if( foundEntity != null )
            {
                final Class<T> entityType = unitOfWorkInstance.module().findClassForName( foundEntity.type() );
                // TODO shall we throw an exception if class cannot be found?
                final T entity = unitOfWorkInstance.getReference( foundEntity.identity(), entityType );
                return entity;
            }
            else
            {
                return null;
            }
        }
        catch( EntityFinderException e )
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @see Query#setVariable(String, Object)
     */
    @SuppressWarnings( "unchecked" )
    public void setVariable( final String name, final Object value )
    {
        // TODO: Casting to VariableValueExpression
        VariableValueExpression variable = getVariable( name );
        if( variable == null )
        {
            throw new IllegalArgumentException( "Variable [" + name + "] is not found." );
        }
        variable.setValue( value );
    }

    /**
     * @see Query#getVariable(String)
     */
    @SuppressWarnings( "unchecked" )
    public <V> V getVariable( final String name )
    {
        return (V) variables.get( name );
    }

    public Class<T> resultType()
    {
        return resultType;
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