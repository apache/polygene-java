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
package org.qi4j.entity.index.rdf;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openrdf.query.QueryLanguage;
import org.qi4j.entity.Identity;
import org.qi4j.property.AbstractPropertyInstance;
import org.qi4j.query.grammar.AssociationIsNullPredicate;
import org.qi4j.query.grammar.AssociationNullPredicate;
import org.qi4j.query.grammar.AssociationReference;
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.query.grammar.ComparisonPredicate;
import org.qi4j.query.grammar.Conjunction;
import org.qi4j.query.grammar.Disjunction;
import org.qi4j.query.grammar.MatchesPredicate;
import org.qi4j.query.grammar.Negation;
import org.qi4j.query.grammar.OrderBy;
import org.qi4j.query.grammar.PropertyIsNullPredicate;
import org.qi4j.query.grammar.PropertyNullPredicate;
import org.qi4j.query.grammar.PropertyReference;
import org.qi4j.query.grammar.SingleValueExpression;
import org.qi4j.query.grammar.ValueExpression;
import org.qi4j.spi.composite.MixinTypeModel;
import org.qi4j.spi.entity.association.AssociationModel;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since April 02, 2008
 */
class SPARQLRDFQueryParser
    implements RDFQueryParser
{

    private int namespaceCounter = 0;
    private int valueCounter = 0;

    /**
     * Mapping between namespace and prefix.
     */
    private final Map<String, String> namespaces;
    private final List<Triple> triples;

    /**
     * Constructor.
     */
    SPARQLRDFQueryParser()
    {
        namespaces = new HashMap<String, String>();
        addNamespace( "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#" );
        addNamespace( "rdfs", "http://www.w3.org/2000/01/rdf-schema#" );
        triples = new ArrayList<Triple>();
    }

    public QueryLanguage getQueryLanguage()
    {
        return QueryLanguage.SPARQL;
    }

    public String getQuery( final Class entityType,
                            final BooleanExpression whereClause,
                            final OrderBy[] orderBySegments,
                            final Integer firstResult,
                            final Integer maxResults )
    {
        StringBuilder query = new StringBuilder();
        triples.add(
            new Triple(
                "?entity",
                "rdf:type",
                "<" + MixinTypeModel.toURI( entityType ) + ">",
                false )
        );
        triples.add(
            new Triple(
                "?entity",
                addNamespace( AbstractPropertyInstance.toNamespace( getAccessor( Identity.class, "identity" ) ) ) + ":identity",
                "?identity",
                false
            )
        );
        final String filter = processFilter( whereClause );
        final String orderBy = processOrderBy( orderBySegments );
        for( Map.Entry<String, String> nsEntry : namespaces.entrySet() )
        {
            query
                .append( "PREFIX " )
                .append( nsEntry.getValue() )
                .append( ": <" )
                .append( nsEntry.getKey() )
                .append( "> " );
        }
        query.append( "SELECT DISTINCT ?entity ?identity " );
        if( !triples.isEmpty() )
        {
            query.append( "WHERE {" );
            for( Triple triple : triples )
            {
                query.append( triple ).append( " " );
            }
            if( filter.length() > 0 )
            {
                query.append( " FILTER " ).append( filter );
            }
            query.append( "}" );
        }
        if( orderBy != null )
        {
            query.append( " ORDER BY " ).append( orderBy );
        }
        if( firstResult != null )
        {
            query.append( " OFFSET " ).append( firstResult );
        }
        if( maxResults != null )
        {
            query.append( " LIMIT " ).append( maxResults );
        }
        System.out.println( "Query: " + query );
        return query.toString();
    }

    private String processFilter( final BooleanExpression expression )
    {
        if( expression == null )
        {
            return "";
        }
        final StringBuilder filter = new StringBuilder();
        if( expression instanceof Conjunction )
        {
            filter
                .append( "(" )
                .append( processFilter( ( (Conjunction) expression ).leftSideExpression() ) )
                .append( " && " )
                .append( processFilter( ( (Conjunction) expression ).rightSideExpression() ) )
                .append( ")" );
        }
        else if( expression instanceof Disjunction )
        {
            filter
                .append( "(" )
                .append( processFilter( ( (Disjunction) expression ).leftSideExpression() ) )
                .append( " || " )
                .append( processFilter( ( (Disjunction) expression ).rightSideExpression() ) )
                .append( ")" );
        }
        else if( expression instanceof Negation )
        {
            filter
                .append( "(!" )
                .append( processFilter( ( (Negation) expression ).expression() ) )
                .append( ")" );
        }
        else if( expression instanceof MatchesPredicate )
        {
            processMatchesPredicate( (MatchesPredicate) expression, filter );

        }
        else if( expression instanceof ComparisonPredicate )
        {
            processComparisonPredicate( (ComparisonPredicate) expression, filter );

        }
        else if( expression instanceof PropertyNullPredicate )
        {
            processNullPredicate( (PropertyNullPredicate) expression, filter );
        }
        else if( expression instanceof AssociationNullPredicate )
        {
            processNullPredicate( (AssociationNullPredicate) expression, filter );
        }
        else
        {
            throw new UnsupportedOperationException( "Expression " + expression + " is not supported" );
        }
        return filter.toString();
    }

    private void processMatchesPredicate( final MatchesPredicate predicate,
                                          final StringBuilder filter )
    {
        String valueVariable = addTriple( predicate.propertyReference(), false ).value;
        ValueExpression valueExpression = predicate.valueExpression();
        if( valueExpression instanceof SingleValueExpression )
        {
            filter
                .append( "regex(" )
                .append( valueVariable )
                .append( "," )
                .append( " \"" )
                .append( ( (SingleValueExpression) valueExpression ).value() )
                .append( "\")" );
        }
        else
        {
            throw new UnsupportedOperationException( "Value " + valueExpression + " is not supported" );
        }
    }

    private void processComparisonPredicate( final ComparisonPredicate predicate,
                                             final StringBuilder filter )
    {
        String valueVariable = addTriple( predicate.propertyReference(), false ).value;
        ValueExpression valueExpression = predicate.valueExpression();
        if( valueExpression instanceof SingleValueExpression )
        {
            filter
                .append( "(" )
                .append( valueVariable )
                .append( " " )
                .append( Operators.getOperator( predicate.getClass() ) )
                .append( " \"" )
                .append( ( (SingleValueExpression) valueExpression ).value() )
                .append( "\")" );
        }
        else
        {
            throw new UnsupportedOperationException( "Value " + valueExpression + " is not supported" );
        }
    }

    private void processNullPredicate( final PropertyNullPredicate predicate,
                                       final StringBuilder filter )
    {
        filter.append( "(" );
        if( predicate instanceof PropertyIsNullPredicate )
        {
            filter.append( "!" );
        }
        filter
            .append( "bound(" )
            .append( addTriple( predicate.propertyReference(), true ).value )
            .append( "))" );
    }

    private void processNullPredicate( final AssociationNullPredicate predicate,
                                       final StringBuilder filter )
    {
        filter.append( "(" );
        if( predicate instanceof AssociationIsNullPredicate )
        {
            filter.append( "!" );
        }
        filter
            .append( "bound(" )
            .append( addTriple( predicate.associationReference(), true ).value )
            .append( "))" );
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
                    final String valueVariable = addTriple( orderBySegment.propertyReference(), false ).value;
                    if( orderBySegment.order() == OrderBy.Order.ASCENDING )
                    {
                        orderBy.append( "ASC" );
                    }
                    else
                    {
                        orderBy.append( "DESC" );
                    }
                    orderBy
                        .append( "(" )
                        .append( valueVariable )
                        .append( ")" );
                }
            }
            return orderBy.toString();
        }
        return null;
    }

    private String addNamespace( final String namespace )
    {
        String ns = namespaces.get( namespace );
        if( ns == null )
        {
            ns = "ns" + namespaceCounter++;
            namespaces.put( namespace, ns );
        }
        return ns;
    }

    private String addNamespace( final String prefix,
                                 final String namespace )
    {
        namespaces.put( namespace, prefix );
        return prefix;
    }

    private Triple addTriple( final PropertyReference propertyReference,
                              boolean optional )
    {
        String subject = "?entity";
        if( propertyReference.traversedAssociation() != null )
        {
            subject = addTriple( propertyReference.traversedAssociation(), false ).value;
        }
        String ns = addNamespace( AbstractPropertyInstance.toNamespace( propertyReference.propertyAccessor() ) );
        return addTriple( subject, ns + ":" + propertyReference.propertyName(), optional );
    }

    private Triple addTriple( final AssociationReference associationReference,
                              final boolean optional )
    {
        String subject = "?entity";
        if( associationReference.traversedAssociation() != null )
        {
            subject = addTriple( associationReference.traversedAssociation(), false ).value;
        }
        String ns = addNamespace( AssociationModel.toNamespace( associationReference.associationAccessor() ) );
        return addTriple( subject, ns + ":" + associationReference.associationName(), optional );
    }

    private Triple addTriple( final String subject,
                              final String predicate,
                              final boolean optional )
    {
        Triple triple = getTriple( subject, predicate );
        if( triple == null )
        {
            final String value = "?v" + valueCounter++;
            triple = new Triple( subject, predicate, value, optional );
            triples.add( triple );
        }
        if( !optional && triple.optional )
        {
            triple.optional = false;
        }
        return triple;
    }

    private Triple getTriple( final String subject,
                              final String predicate )
    {
        for( Triple triple : triples )
        {
            if( triple.subject.equals( subject )
                && triple.predicate.equals( predicate ) )
            {
                return triple;
            }
        }
        return null;
    }

    private static Method getAccessor( final Class declaringClass,
                                       final String accessorName )
    {
        try
        {
            return declaringClass.getMethod( accessorName );
        }
        catch( NoSuchMethodException e )
        {
            throw new RuntimeException( "Internal error", e );
        }
    }

    private static class Triple
    {
        String subject;
        String predicate;
        String value;
        boolean optional;

        private Triple( final String subject,
                        final String predicate,
                        final String value,
                        final boolean optional )
        {
            this.subject = subject;
            this.predicate = predicate;
            this.value = value;
            this.optional = optional;
        }

        @Override public boolean equals( Object otherObject )
        {
            if( this == otherObject )
            {
                return true;
            }
            if( otherObject == null || getClass() != otherObject.getClass() )
            {
                return false;
            }

            Triple other = (Triple) otherObject;

            if( predicate != null ? !predicate.equals( other.predicate ) : other.predicate != null )
            {
                return false;
            }
            if( subject != null ? !subject.equals( other.subject ) : other.subject != null )
            {
                return false;
            }
            if( value != null )
            {
                return value.equals( other.value );
            }
            else
            {
                return other.value == null;
            }
        }

        @Override public int hashCode()
        {
            int result;
            result = ( subject != null ? subject.hashCode() : 0 );
            result = 31 * result + ( predicate != null ? predicate.hashCode() : 0 );
            result = 31 * result + ( value != null ? value.hashCode() : 0 );
            return result;
        }

        @Override public String toString()
        {
            final StringBuilder triple = new StringBuilder()
                .append( subject )
                .append( " " )
                .append( predicate )
                .append( " " )
                .append( value );
            if( optional )
            {
                triple
                    .insert( 0, "OPTIONAL {" )
                    .append( "}" );
            }
            triple.append( "." );
            return triple.toString();
        }
    }

}