/*
 * Copyright 2011 Rickard Öberg.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.apache.commons.lang.StringEscapeUtils;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.query.grammar.AndSpecification;
import org.qi4j.api.query.grammar.AssociationNotNullSpecification;
import org.qi4j.api.query.grammar.AssociationNullSpecification;
import org.qi4j.api.query.grammar.ComparisonSpecification;
import org.qi4j.api.query.grammar.ContainsAllSpecification;
import org.qi4j.api.query.grammar.ContainsSpecification;
import org.qi4j.api.query.grammar.EqSpecification;
import org.qi4j.api.query.grammar.GeSpecification;
import org.qi4j.api.query.grammar.GtSpecification;
import org.qi4j.api.query.grammar.LeSpecification;
import org.qi4j.api.query.grammar.LtSpecification;
import org.qi4j.api.query.grammar.ManyAssociationContainsSpecification;
import org.qi4j.api.query.grammar.MatchesSpecification;
import org.qi4j.api.query.grammar.NeSpecification;
import org.qi4j.api.query.grammar.NotSpecification;
import org.qi4j.api.query.grammar.OrSpecification;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.PropertyFunction;
import org.qi4j.api.query.grammar.PropertyNotNullSpecification;
import org.qi4j.api.query.grammar.PropertyNullSpecification;
import org.qi4j.api.query.grammar.QuerySpecification;
import org.qi4j.api.query.grammar.Variable;
import org.qi4j.api.value.ValueSerializer;
import org.qi4j.api.value.ValueSerializer.Options;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.index.rdf.query.RdfQueryParser;
import org.qi4j.spi.Qi4jSPI;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * JAVADOC Add JavaDoc
 */
public class RdfQueryParserImpl
    implements RdfQueryParser
{
    private static final ThreadLocal<DateFormat> ISO8601_UTC = new ThreadLocal<DateFormat>()
    {
        @Override
        protected DateFormat initialValue()
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
            dateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
            return dateFormat;
        }
    };

    private static final Map<Class<? extends ComparisonSpecification>, String> OPERATORS;
    private static final Set<Character> RESERVED_CHARS;

    private final Namespaces namespaces = new Namespaces();
    private final Triples triples = new Triples( namespaces );
    private final Qi4jSPI spi;
    private final ValueSerializer valueSerializer;
    private Map<String, Object> variables;

    static
    {
        OPERATORS = new HashMap<>( 6 );
        OPERATORS.put( EqSpecification.class, "=" );
        OPERATORS.put( GeSpecification.class, ">=" );
        OPERATORS.put( GtSpecification.class, ">" );
        OPERATORS.put( LeSpecification.class, "<=" );
        OPERATORS.put( LtSpecification.class, "<" );
        OPERATORS.put( NeSpecification.class, "!=" );

        RESERVED_CHARS = new HashSet<>( Arrays.asList(
            '\"', '^', '.', '\\', '?', '*', '+', '{', '}', '(', ')', '|', '$', '[', ']'
        ) );
    }

    public RdfQueryParserImpl( Qi4jSPI spi, ValueSerializer valueSerializer )
    {
        this.spi = spi;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public String constructQuery( final Class<?> resultType,
                                  final Specification<Composite> specification,
                                  final OrderBy[] orderBySegments,
                                  final Integer firstResult,
                                  final Integer maxResults,
                                  final Map<String, Object> variables
    )
    {
        this.variables = variables;

        if( QuerySpecification.isQueryLanguage( "SPARQL", specification ) )
        {
            // Custom query
            StringBuilder queryBuilder = new StringBuilder();
            String query = ( (QuerySpecification) specification ).query();
            queryBuilder.append( query );

            if( orderBySegments != null )
            {
                queryBuilder.append( "\nORDER BY " );
                processOrderBy( orderBySegments, queryBuilder );
            }
            if( firstResult != null )
            {
                queryBuilder.append( "\nOFFSET " ).append( firstResult );
            }
            if( maxResults != null )
            {
                queryBuilder.append( "\nLIMIT " ).append( maxResults );
            }

            return queryBuilder.toString();
        }
        else
        {
            // Add type+identity triples last. This makes queries faster since the query engine can reduce the number
            // of triples to check faster
            triples.addDefaultTriples( resultType.getName() );
        }

        // and collect namespaces
        StringBuilder filter = new StringBuilder();
        processFilter( specification, true, filter );
        StringBuilder orderBy = new StringBuilder();
        processOrderBy( orderBySegments, orderBy );

        StringBuilder query = new StringBuilder();

        for( String namespace : namespaces.namespaces() )
        {
            query.append( format( "PREFIX %s: <%s> %n", namespaces.namespacePrefix( namespace ), namespace ) );
        }
        query.append( "SELECT DISTINCT ?identity\n" );
        if( triples.hasTriples() )
        {
            query.append( "WHERE {\n" );
            StringBuilder optional = new StringBuilder();
            for( Triples.Triple triple : triples )
            {
                final String subject = triple.subject();
                final String predicate = triple.predicate();
                final String value = triple.value();

                if( triple.isOptional() )
                {
                    optional.append( format( "OPTIONAL {%s %s %s}. ", subject, predicate, value ) );
                    optional.append( '\n' );
                }
                else
                {
                    query.append( format( "%s %s %s. ", subject, predicate, value ) );
                    query.append( '\n' );
                }
            }

            // Add OPTIONAL statements last
            if( optional.length() > 0 )
            {
                query.append( optional.toString() );
            }

            if( filter.length() > 0 )
            {
                query.append( "FILTER " ).append( filter );
            }
            query.append( "\n}" );
        }
        if( orderBy.length() > 0 )
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

        LoggerFactory.getLogger( getClass() ).debug( "Query:\n" + query );
        return query.toString();
    }

    private void processFilter( final Specification<Composite> expression, boolean allowInline, StringBuilder builder )
    {
        if( expression == null )
        {
            return;
        }

        if( expression instanceof AndSpecification )
        {
            final AndSpecification conjunction = (AndSpecification) expression;

            int start = builder.length();
            boolean first = true;
            for( Specification<Composite> operand : conjunction.operands() )
            {
                int size = builder.length();
                processFilter( operand, allowInline, builder );
                if( builder.length() > size )
                {
                    if( first )
                    {
                        first = false;
                    }
                    else
                    {
                        builder.insert( size, " && " );
                    }
                }
            }

            if( builder.length() > start )
            {
                builder.insert( start, '(' );
                builder.append( ')' );
            }
        }
        else if( expression instanceof OrSpecification )
        {
            final OrSpecification disjunction = (OrSpecification) expression;

            int start = builder.length();
            boolean first = true;
            for( Specification<Composite> operand : disjunction.operands() )
            {
                int size = builder.length();
                processFilter( operand, false, builder );
                if( builder.length() > size )
                {
                    if( first )
                    {
                        first = false;
                    }
                    else
                    {
                        builder.insert( size, "||" );
                    }
                }
            }

            if( builder.length() > start )
            {
                builder.insert( start, '(' );
                builder.append( ')' );
            }
        }
        else if( expression instanceof NotSpecification )
        {
            builder.insert( 0, "(!" );
            processFilter( ( (NotSpecification) expression ).operand(), false, builder );
            builder.append( ")" );
        }
        else if( expression instanceof ComparisonSpecification )
        {
            processComparisonPredicate( expression, allowInline, builder );
        }
        else if( expression instanceof ContainsAllSpecification )
        {
            processContainsAllPredicate( (ContainsAllSpecification) expression, builder );
        }
        else if( expression instanceof ContainsSpecification<?> )
        {
            processContainsPredicate( (ContainsSpecification<?>) expression, builder );
        }
        else if( expression instanceof MatchesSpecification )
        {
            processMatchesPredicate( (MatchesSpecification) expression, builder );
        }
        else if( expression instanceof PropertyNotNullSpecification<?> )
        {
            processNotNullPredicate( (PropertyNotNullSpecification) expression, builder );
        }
        else if( expression instanceof PropertyNullSpecification<?> )
        {
            processNullPredicate( (PropertyNullSpecification) expression, builder );
        }
        else if( expression instanceof AssociationNotNullSpecification<?> )
        {
            processNotNullPredicate( (AssociationNotNullSpecification) expression, builder );
        }
        else if( expression instanceof AssociationNullSpecification<?> )
        {
            processNullPredicate( (AssociationNullSpecification) expression, builder );
        }
        else if( expression instanceof ManyAssociationContainsSpecification<?> )
        {
            processManyAssociationContainsPredicate( (ManyAssociationContainsSpecification) expression, allowInline, builder );
        }
        else
        {
            throw new UnsupportedOperationException( "Expression " + expression + " is not supported" );
        }
    }

    private static void join( String[] strings, String delimiter, StringBuilder builder )
    {
        for( Integer x = 0; x < strings.length; ++x )
        {
            builder.append( strings[ x] );
            if( x + 1 < strings.length )
            {
                builder.append( delimiter );
            }
        }
    }

    private String createAndEscapeJSONString( Object value )
    {
        return escapeJSONString( valueSerializer.serialize( new Options().withoutTypeInfo(), value ) );
    }

    private String createRegexStringForContaining( String valueVariable, String containedString )
    {
        // The matching value must start with [, then contain something (possibly nothing),
        // then our value, then again something (possibly nothing), and end with ]
        return format( "regex(str(%s), \"^\\\\u005B.*%s.*\\\\u005D$\", \"s\")", valueVariable, containedString );
    }

    private String escapeJSONString( String jsonStr )
    {
        StringBuilder builder = new StringBuilder();
        char[] chars = jsonStr.toCharArray();
        for( int i = 0; i < chars.length; i++ )
        {
            char c = chars[ i];

            /*
             if ( reservedJsonChars.contains( c ))
             {
             builder.append( "\\\\u" ).append( format( "%04X", (int) '\\' ) );
             }
             */
            if( RESERVED_CHARS.contains( c ) )
            {
                builder.append( "\\\\u" ).append( format( "%04X", (int) c ) );
            }
            else
            {
                builder.append( c );
            }
        }

        return builder.toString();
    }

    private void processContainsAllPredicate( final ContainsAllSpecification<?> predicate, StringBuilder builder )
    {
        Iterable<?> values = predicate.containedValues();
        String valueVariable = triples.addTriple( predicate.collectionProperty(), false ).value();
        String[] strings;
        if( values instanceof Collection )
        {
            strings = new String[ ( (Collection<?>) values ).size() ];
        }
        else
        {
            strings = new String[ ( (int) Iterables.count( values ) ) ];
        }
        Integer x = 0;
        for( Object item : (Collection<?>) values )
        {
            String jsonStr = "";
            if( item != null )
            {
                String serialized = valueSerializer.serialize( item, false );
                if( item instanceof String )
                {
                    serialized = "\"" + StringEscapeUtils.escapeJava( serialized ) + "\"";
                }
                jsonStr = escapeJSONString( serialized );
            }
            strings[ x] = this.createRegexStringForContaining( valueVariable, jsonStr );
            x++;
        }

        if( strings.length > 0 )
        {
            // For some reason, just "FILTER ()" causes error in SPARQL query
            builder.append( "(" );
            join( strings, " && ", builder );
            builder.append( ")" );
        }
        else
        {
            builder.append( this.createRegexStringForContaining( valueVariable, "" ) );
        }
    }

    private void processContainsPredicate( final ContainsSpecification<?> predicate, StringBuilder builder )
    {
        Object value = predicate.value();
        String valueVariable = triples.addTriple( predicate.collectionProperty(), false ).value();
        builder.append( this.createRegexStringForContaining(
            valueVariable,
            this.createAndEscapeJSONString( value )
        ) );
    }

    private void processMatchesPredicate( final MatchesSpecification predicate, StringBuilder builder )
    {
        String valueVariable = triples.addTriple( predicate.property(), false ).value();
        builder.append( format( "regex(%s,\"%s\")", valueVariable, predicate.regexp() ) );
    }

    private void processComparisonPredicate( final Specification<Composite> predicate,
                                             boolean allowInline,
                                             StringBuilder builder
    )
    {
        if( predicate instanceof ComparisonSpecification )
        {
            ComparisonSpecification<?> comparisonSpecification = (ComparisonSpecification<?>) predicate;
            Triples.Triple triple = triples.addTriple( (PropertyFunction) comparisonSpecification.property(), false );

            // Don't use FILTER for equals-comparison. Do direct match instead
            if( predicate instanceof EqSpecification && allowInline )
            {
                triple.setValue( "\"" + toString( comparisonSpecification.value() ) + "\"" );
            }
            else
            {
                String valueVariable = triple.value();
                builder.append( String.format(
                    "(%s %s \"%s\")",
                    valueVariable,
                    getOperator( comparisonSpecification.getClass() ),
                    toString( comparisonSpecification.value() ) ) );
            }
        }
        else
        {
            throw new UnsupportedOperationException( "Operator " + predicate.getClass()
                .getName() + " is not supported" );
        }
    }

    private void processNullPredicate( final PropertyNullSpecification<?> predicate, StringBuilder builder )
    {
        final String value = triples.addTriple( predicate.property(), true ).value();
        builder.append( format( "(! bound(%s))", value ) );
    }

    private void processNotNullPredicate( final PropertyNotNullSpecification<?> predicate, StringBuilder builder )
    {
        final String value = triples.addTriple( predicate.property(), true ).value();
        builder.append( format( "(bound(%s))", value ) );
    }

    private void processNullPredicate( final AssociationNullSpecification<?> predicate, StringBuilder builder )
    {
        final String value = triples.addTripleAssociation( predicate.association(), true ).value();
        builder.append( format( "(! bound(%s))", value ) );
    }

    private void processNotNullPredicate( final AssociationNotNullSpecification<?> predicate, StringBuilder builder )
    {
        final String value = triples.addTripleAssociation( predicate.association(), true ).value();
        builder.append( format( "(bound(%s))", value ) );
    }

    private void processManyAssociationContainsPredicate( ManyAssociationContainsSpecification<?> predicate,
                                                          boolean allowInline, StringBuilder builder
    )
    {
        Triples.Triple triple = triples.addTripleManyAssociation( predicate.manyAssociation(), false );

        if( allowInline )
        {
            triple.setValue( "<" + toString( predicate.value() ) + ">" );
        }
        else
        {
            String valueVariable = triple.value();
            builder.append( String.format( "(%s %s <%s>)", valueVariable, "=", toString( predicate.value() ) ) );
        }
    }

    private void processOrderBy( OrderBy[] orderBySegments, StringBuilder builder )
    {
        if( orderBySegments != null && orderBySegments.length > 0 )
        {
            for( OrderBy orderBySegment : orderBySegments )
            {
                processOrderBy( builder, orderBySegment );
            }
        }
    }

    private void processOrderBy( StringBuilder builder, OrderBy orderBySegment )
    {
        if( orderBySegment != null )
        {
            final String valueVariable = triples.addTriple( orderBySegment.property(), false ).value();
            if( orderBySegment.order() == OrderBy.Order.ASCENDING )
            {
                builder.append( format( "ASC(%s)", valueVariable ) );
            }
            else
            {
                builder.append( format( "DESC(%s)", valueVariable ) );
            }
        }
    }

    private String getOperator( final Class<? extends ComparisonSpecification> predicateClass )
    {
        String operator = OPERATORS.get( predicateClass );
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
        else if( value instanceof EntityComposite )
        {
            return "urn:qi4j:entity:" + value.toString();
        }
        else if( value instanceof Variable )
        {
            Object realValue = variables.get( ( (Variable) value ).variableName() );

            if( realValue == null )
            {
                throw new IllegalArgumentException( "Variable " + ( (Variable) value ).variableName() + " not bound" );
            }

            return toString( realValue );
        }
        else
        {
            return value.toString();
        }
    }
}
