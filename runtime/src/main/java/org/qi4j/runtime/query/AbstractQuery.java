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
import java.util.Map;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.ComparisonPredicate;
import org.qi4j.api.query.grammar.Conjunction;
import org.qi4j.api.query.grammar.Disjunction;
import org.qi4j.api.query.grammar.Negation;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.SingleValueExpression;
import org.qi4j.api.query.grammar.ValueExpression;
import org.qi4j.runtime.query.grammar.impl.VariableValueExpressionImpl;

/**
 * Default implementation of {@link org.qi4j.api.query.Query}
 */
abstract class AbstractQuery<T>
    implements Query<T>
{
    private static final long serialVersionUID = 1L;

    /**
     * Type of queried entities.
     */
    protected final Class<T> resultType;
    /**
     * Where clause.
     */
    protected final BooleanExpression whereClause;
    /**
     * Order by clause segments.
     */
    protected OrderBy[] orderBySegments;
    /**
     * First result to be returned.
     */
    protected Integer firstResult;
    /**
     * Maximum number of results to be returned.
     */
    protected Integer maxResults;
    /**
     * Mapping between variable name and variable value expression.
     */
    private final Map<String, SingleValueExpression> variables;

    /**
     * Constructor.
     *
     * @param resultType  type of queried entities; cannot be null
     * @param whereClause where clause
     */
    AbstractQuery( final Class<T> resultType,
                   final BooleanExpression whereClause
    )
    {
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

            if( valueExpression instanceof VariableValueExpressionImpl )
            {
                VariableValueExpressionImpl variableValueExpression = (VariableValueExpressionImpl) valueExpression;
                variables.put( variableValueExpression.name(), variableValueExpression );
            }
        }
    }

    /**
     * @see org.qi4j.api.query.Query#orderBy(org.qi4j.api.query.grammar.OrderBy[])
     */
    public Query<T> orderBy( final OrderBy... segments )
    {
        orderBySegments = segments;
        return this;
    }

    /**
     * @see org.qi4j.api.query.Query#firstResult(int)
     */
    public Query<T> firstResult( int firstResult )
    {
        this.firstResult = firstResult;
        return this;
    }

    /**
     * @see org.qi4j.api.query.Query#maxResults(int)
     */
    public Query<T> maxResults( int maxResults )
    {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * @see org.qi4j.api.query.Query#setVariable(String, Object)
     */
    @SuppressWarnings( "unchecked" )
    public Query<T> setVariable( final String name, final Object value )
    {
        // TODO: Casting to VariableValueExpression
        VariableValueExpressionImpl variable = getVariable( name );
        if( variable == null )
        {
            throw new IllegalArgumentException( "Variable [" + name + "] is not found." );
        }
        variable.setValue( value );

        return this;
    }

    /**
     * @see org.qi4j.api.query.Query#getVariable(String)
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
}