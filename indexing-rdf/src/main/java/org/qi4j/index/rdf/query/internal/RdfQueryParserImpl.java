/*
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
package org.qi4j.index.rdf.query.internal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.query.grammar.AssociationIsNullPredicate;
import org.qi4j.api.query.grammar.AssociationNullPredicate;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.ComparisonPredicate;
import org.qi4j.api.query.grammar.Conjunction;
import org.qi4j.api.query.grammar.Disjunction;
import org.qi4j.api.query.grammar.EqualsPredicate;
import org.qi4j.api.query.grammar.GreaterOrEqualPredicate;
import org.qi4j.api.query.grammar.GreaterThanPredicate;
import org.qi4j.api.query.grammar.LessOrEqualPredicate;
import org.qi4j.api.query.grammar.LessThanPredicate;
import org.qi4j.api.query.grammar.ManyAssociationContainsPredicate;
import org.qi4j.api.query.grammar.MatchesPredicate;
import org.qi4j.api.query.grammar.Negation;
import org.qi4j.api.query.grammar.NotEqualsPredicate;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.Predicate;
import org.qi4j.api.query.grammar.PropertyIsNullPredicate;
import org.qi4j.api.query.grammar.PropertyNullPredicate;
import org.qi4j.api.query.grammar.SingleValueExpression;
import org.qi4j.api.query.grammar.ValueExpression;
import org.qi4j.index.rdf.query.RdfQueryParser;

import static java.lang.String.*;

/**
 * JAVADOC Add JavaDoc
 */
public class RdfQueryParserImpl
    implements RdfQueryParser
{
    private static ThreadLocal<DateFormat> ISO8601_UTC = new ThreadLocal<DateFormat>()
    {
        @Override
        protected DateFormat initialValue()
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
            dateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
            return dateFormat;
        }
    };
    private static final Map<Class<? extends Predicate>, String> m_operators;

    private Namespaces namespaces = new Namespaces();
    private Triples triples = new Triples( namespaces );

    static
    {
        m_operators = new HashMap<Class<? extends Predicate>, String>();
        m_operators.put( EqualsPredicate.class, "=" );
        m_operators.put( GreaterOrEqualPredicate.class, ">=" );
        m_operators.put( GreaterThanPredicate.class, ">" );
        m_operators.put( LessOrEqualPredicate.class, "<=" );
        m_operators.put( LessThanPredicate.class, "<" );
        m_operators.put( NotEqualsPredicate.class, "!=" );
        m_operators.put( ManyAssociationContainsPredicate.class, "=" );
    }

    public String getQuery( final String resultType,
                            final BooleanExpression whereClause,
                            final OrderBy[] orderBySegments,
                            final Integer firstResult,
                            final Integer maxResults
    )
    {
        triples.addDefaultTriples( resultType );

        // and collect namespaces
        final String filter = processFilter( whereClause, true );
        final String orderBy = processOrderBy( orderBySegments );

        StringBuilder query = new StringBuilder();

        for( String namespace : namespaces.getNamespaces() )
        {
            query.append( format( "PREFIX %s: <%s> %n", namespaces.getNamespacePrefix( namespace ), namespace ) );
        }
        query.append( "SELECT DISTINCT ?entityType ?identity\n" );
        if( triples.hasTriples() )
        {
            query.append( "WHERE {\n" );
            for( Triples.Triple triple : triples )
            {
                final String subject = triple.getSubject();
                final String predicate = triple.getPredicate();
                final String value = triple.getValue();

                if( triple.isOptional() )
                {
                    query.append( format( "OPTIONAL {%s %s %s}. ", subject, predicate, value ) );
                }
                else
                {
                    query.append( format( "%s %s %s. ", subject, predicate, value ) );
                }
                query.append( '\n' );
            }

            if( filter.length() > 0 )
            {
                query.append( "FILTER " ).append( filter );
            }
            query.append( "\n}" );
        }
        if( orderBy != null )
        {
            query.append( "\nORDER BY " ).append( orderBy );
        }
        if( firstResult != null )
        {
            query.append( "\nOFFSET " ).append( firstResult );
        }
        if( maxResults != null )
        {
            query.append( "\nLIMIT " ).append( maxResults );
        }

        Logger.getLogger( getClass().getName() ).info( "Query:\n" + query );
        return query.toString();
    }

    private String processFilter( final BooleanExpression expression, boolean allowInline )
    {
        if( expression == null )
        {
            return "";
        }
        if( expression instanceof Conjunction )
        {
            final Conjunction conjunction = (Conjunction) expression;
            String left = processFilter( conjunction.leftSideExpression(), allowInline );
            String right = processFilter( conjunction.rightSideExpression(), allowInline );

            if( left.equals( "" ) )
            {
                return right;
            }
            else if( right.equals( "" ) )
            {
                return left;
            }
            else
            {
                return format( "(%s && %s)",
                               left,
                               right );
            }
        }
        if( expression instanceof Disjunction )
        {
            final Disjunction disjunction = (Disjunction) expression;
            String left = processFilter( disjunction.leftSideExpression(), false );
            String right = processFilter( disjunction.rightSideExpression(), false );
            if( left.equals( "" ) )
            {
                return right;
            }
            else if( right.equals( "" ) )
            {
                return left;
            }
            else
            {
                return format( "(%s || %s)",
                               left,
                               right );
            }
        }
        if( expression instanceof Negation )
        {
            return format( "(!%s)", processFilter( ( (Negation) expression ).expression(), false ) );
        }
        if( expression instanceof MatchesPredicate )
        {
            return processMatchesPredicate( (MatchesPredicate) expression );
        }
        if( expression instanceof ComparisonPredicate )
        {
            return processComparisonPredicate( (ComparisonPredicate) expression, allowInline );
        }
        if( expression instanceof ManyAssociationContainsPredicate )
        {
            return processManyAssociationContainsPredicate( (ManyAssociationContainsPredicate) expression, allowInline );
        }
        if( expression instanceof PropertyNullPredicate )
        {
            return processNullPredicate( (PropertyNullPredicate) expression );
        }
        if( expression instanceof AssociationNullPredicate )
        {
            return processNullPredicate( (AssociationNullPredicate) expression );
        }
        throw new UnsupportedOperationException( "Expression " + expression + " is not supported" );
    }

    private String processMatchesPredicate( final MatchesPredicate predicate )
    {
        ValueExpression valueExpression = predicate.valueExpression();
        if( valueExpression instanceof SingleValueExpression )
        {
            String valueVariable = triples.addTriple( predicate.propertyReference(), false ).getValue();
            final SingleValueExpression singleValueExpression = (SingleValueExpression) valueExpression;
            return format( "regex(%s,\"%s\")", valueVariable, singleValueExpression.value() );
        }
        else
        {
            throw new UnsupportedOperationException( "Value " + valueExpression + " is not supported" );
        }
    }

    private String processComparisonPredicate( final ComparisonPredicate predicate, boolean allowInline )
    {
        ValueExpression valueExpression = predicate.valueExpression();
        if( valueExpression instanceof SingleValueExpression )
        {
            Triples.Triple triple = triples.addTriple( predicate.propertyReference(), false );

            // Don't use FILTER for equals-comparison. Do direct match instead
            if( predicate instanceof EqualsPredicate && allowInline )
            {
                final SingleValueExpression singleValueExpression = (SingleValueExpression) valueExpression;
                triple.setValue( "\"" + toString( singleValueExpression.value() ) + "\"" );
                return "";
            }
            else
            {
                String valueVariable = triple.getValue();
                final SingleValueExpression singleValueExpression = (SingleValueExpression) valueExpression;
                return String.format( "(%s %s \"%s\")", valueVariable, getOperator( predicate.getClass() ),
                                      toString( singleValueExpression.value() ) );
            }
        }
        else
        {
            throw new UnsupportedOperationException( "Value " + valueExpression + " is not supported" );
        }
    }

    private String processManyAssociationContainsPredicate( ManyAssociationContainsPredicate predicate,
                                                            boolean allowInline
    )
    {
        ValueExpression valueExpression = predicate.valueExpression();
        if( valueExpression instanceof SingleValueExpression )
        {
            Triples.Triple triple = triples.addTriple( predicate.associationReference(), false );

            if( allowInline )
            {
                final SingleValueExpression singleValueExpression = (SingleValueExpression) valueExpression;
                triple.setValue( "<" + toString( singleValueExpression.value() ) + ">" );
                return "";
            }
            else
            {
                String valueVariable = triple.getValue();
                final SingleValueExpression singleValueExpression = (SingleValueExpression) valueExpression;
                return String.format( "(%s %s <%s>)", valueVariable, getOperator( predicate.getClass() ),
                                      toString( singleValueExpression.value() ) );
            }
        }
        else
        {
            throw new UnsupportedOperationException( "Value " + valueExpression + " is not supported" );
        }
    }

    private String processNullPredicate( final PropertyNullPredicate predicate )
    {
        final String value = triples.addTriple( predicate.propertyReference(), true ).getValue();
        if( predicate instanceof PropertyIsNullPredicate )
        {
            return format( "(! bound(%s))", value );
        }
        else
        {
            return format( "(bound(%s))", value );
        }
    }

    private String processNullPredicate( final AssociationNullPredicate predicate )
    {
        final String value = triples.addTriple( predicate.associationReference(), true ).getValue();
        if( predicate instanceof AssociationIsNullPredicate )
        {
            return format( "(! bound(%s))", value );
        }
        else
        {
            return format( "(bound(%s))", value );
        }
    }

    private String processOrderBy( OrderBy[] orderBySegments )
    {
        if( orderBySegments != null && orderBySegments.length > 0 )
        {
            final StringBuilder orderBy = new StringBuilder();
            for( OrderBy orderBySegment : orderBySegments )
            {
                if( orderBySegment != null )
                {
                    final String valueVariable = triples.addTriple( orderBySegment.propertyReference(), false )
                        .getValue();
                    if( orderBySegment.order() == OrderBy.Order.ASCENDING )
                    {
                        orderBy.append( format( "ASC(%s)", valueVariable ) );
                    }
                    else
                    {
                        orderBy.append( format( "DESC(%s)", valueVariable ) );
                    }
                }
            }
            return orderBy.length() > 0 ? orderBy.toString() : null;
        }
        return null;
    }

    private String getOperator( final Class<? extends Predicate> predicateClass )
    {
        String operator = null;
        for( Map.Entry<Class<? extends Predicate>, String> entry : m_operators.entrySet() )
        {
            if( entry.getKey().isAssignableFrom( predicateClass ) )
            {
                operator = entry.getValue();
                break;
            }
        }
        if( operator == null )
        {
            throw new UnsupportedOperationException( "Predicate [" + predicateClass.getName() + "] is not supported" );
        }
        return operator;
    }

    private String toString( Object value )
    {
        if( value == null )
        {
            return null;
        }

        if( value instanceof Date )
        {
            return ISO8601_UTC.get().format( (Date) value );
        }
        else if( value instanceof Entity )
        {
            return "urn:qi4j:entity:" + value.toString();
        }
        else
        {
            return value.toString();
        }
    }
}