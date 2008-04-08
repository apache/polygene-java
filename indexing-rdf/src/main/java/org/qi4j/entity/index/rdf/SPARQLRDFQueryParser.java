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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openrdf.query.QueryLanguage;
import org.qi4j.entity.Identity;
import org.qi4j.query.grammar.AssociationReference;
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.query.grammar.ComparisonPredicate;
import org.qi4j.query.grammar.Conjunction;
import org.qi4j.query.grammar.EqualsPredicate;
import org.qi4j.query.grammar.GreaterOrEqualPredicate;
import org.qi4j.query.grammar.PropertyReference;
import org.qi4j.query.grammar.SingleValueExpression;
import org.qi4j.query.grammar.ValueExpression;
import org.qi4j.query.grammar.Disjunction;
import org.qi4j.query.grammar.GreaterThanPredicate;

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
    private int subjectCounter = 0;
    private int valueCounter = 0;

    /**
     * Mapping between namespace and prefix.
     */
    private final Map<String, String> namespaces;
    private final List<String> triples;

    /**
     * Constructor.
     */
    SPARQLRDFQueryParser()
    {
        namespaces = new HashMap<String, String>();
        addNamespace( "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#" );
        addNamespace( "rdfs", "http://www.w3.org/2000/01/rdf-schema#" );
        triples = new ArrayList<String>();
    }

    public QueryLanguage getQueryLanguage()
    {
        return QueryLanguage.SPARQL;
    }

    public String getQuery( final Class entityType,
                            final BooleanExpression whereClause )
    {
        StringBuilder query = new StringBuilder();
        triples.add( "?entity rdf:type <" + getURI( entityType ) + ">" );
        triples.add( "?entity " + addNamespace( Identity.class ) + ":identity ?identity" );
        String filter = process( whereClause );
        for( Map.Entry<String, String> nsEntry : namespaces.entrySet() )
        {
            query
                .append( "PREFIX " )
                .append( nsEntry.getValue() )
                .append( ": <" )
                .append( nsEntry.getKey() )
                .append( "> " );
        }
        query.append( "SELECT DISTINCT ?identity " );
        if( !triples.isEmpty() )
        {
            query.append( "WHERE {" );
            for( String pattern : triples )
            {
                query.append( pattern ).append( ". " );
            }
            if( filter.length() > 0 )
            {
                query.append( " FILTER " ).append( filter );
            }
            query.append( "}" );
        }
        System.out.println( "Query: " + query );
        return query.toString();
    }

    private String process( BooleanExpression expression )
    {
        StringBuilder filter = new StringBuilder();
        if( expression instanceof Conjunction )
        {
            filter
                .append( "(" )
                .append( process( ( (Conjunction) expression ).leftSideExpression() ) )
                .append( " && " )
                .append( process( ( (Conjunction) expression ).rightSideExpression() ) )
                .append( ")" );
        }
        else if( expression instanceof Disjunction )
        {
            filter
                .append( "(" )
                .append( process( ( (Disjunction) expression ).leftSideExpression() ) )
                .append( " || " )
                .append( process( ( (Disjunction) expression ).rightSideExpression() ) )
                .append( ")" );
        }
        else if( expression instanceof ComparisonPredicate )
        {
            processComparisonPredicate( (ComparisonPredicate) expression, filter );

        }
        return filter.toString();
    }

    private void processComparisonPredicate( final ComparisonPredicate predicate,
                                             final StringBuilder filter )
    {
        String valueVariable = addTriple( predicate.propertyReference() );
        ValueExpression valueExpression = predicate.valueExpression();
        if( valueExpression instanceof SingleValueExpression )
        {
            filter
                .append( "(" )
                .append( valueVariable )
                .append( " " )
                .append( ComparisonOperators.getOperator( predicate.getClass() ))
                .append( " \"" )
                .append( ( (SingleValueExpression) valueExpression ).value() )
                .append( "\")" );
        }
    }

    private String addNamespace( Class declaringType )
    {
        String ns = namespaces.get( getURI( declaringType ) + "/" );
        if( ns == null )
        {
            ns = "ns" + namespaceCounter++;
            namespaces.put( getURI( declaringType ) + "/", ns );
        }
        return ns;
    }

    private String addNamespace( final String prefix,
                                 final String namespace )
    {
        namespaces.put( namespace, prefix );
        return prefix;
    }

    private String addTriple( PropertyReference propertyReference )
    {
        String subject = "?entity";
        if( propertyReference.traversedAssociation() != null )
        {
            subject = addTriple( propertyReference.traversedAssociation() );
        }
        String ns = addNamespace( propertyReference.propertyDeclaringType() );
        return addTriple( subject, ns + ":" + propertyReference.propertyName() );
    }

    private String addTriple( AssociationReference associationReference )
    {
        String subject = "?entity";
        if( associationReference.traversedAssociation() != null )
        {
            subject = addTriple( associationReference.traversedAssociation() );
        }
        String ns = addNamespace( associationReference.associationDeclaringType() );
        return addTriple( subject, ns + ":" + associationReference.associationName() );
    }

    private String addTriple( String subject, String predicate )
    {
        String value = "?v" + valueCounter++;
        triples.add( subject + " " + predicate + " " + value );
        return value;
    }

    private static String getURI( final Class type )
    {
        return "urn:" + type.getName();
    }
}