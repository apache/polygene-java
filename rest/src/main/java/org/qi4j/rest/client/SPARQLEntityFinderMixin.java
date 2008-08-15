/*
 * Copyright 2008 Rickard Öberg.
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
package org.qi4j.rest.client;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import org.qi4j.entity.index.rdf.RdfQueryParser;
import org.qi4j.entity.index.rdf.SparqlRdfQueryParser;
import org.qi4j.injection.scope.Service;
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.query.grammar.OrderBy;
import org.qi4j.service.Wrapper;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.restlet.Client;
import org.restlet.data.Response;
import org.restlet.resource.SaxRepresentation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderAdapter;

/**
 * TODO Add JavaDoc
 */
public class SPARQLEntityFinderMixin
    implements EntityFinder
{
    @Service Wrapper<Client> client;

    public Iterable<QualifiedIdentity> findEntities(
        final String resultType,
        final BooleanExpression whereClause,
        final OrderBy[] orderBySegments,
        final Integer firstResult,
        final Integer maxResults )
        throws EntityFinderException
    {
        final Collection<QualifiedIdentity> entities = new ArrayList<QualifiedIdentity>();
        try
        {
            // TODO shall we support different implementation as SERQL?
            final RdfQueryParser parser = new SparqlRdfQueryParser();
            String query = parser.getQuery( resultType, whereClause, orderBySegments, firstResult, maxResults );

            Response response = client.get().get( "http://localhost:8040/qi4j/query.rdf?query=" + URLEncoder.encode( query, "UTF-8" ) );
            SaxRepresentation sax = response.getEntityAsSax();
            sax.parse( new XMLReaderAdapter()
            {
                String element;
                String id;
                String type;

                @Override public void startElement( String uri, String localName, String qName, Attributes atts ) throws SAXException
                {
                    element = localName;
                }

                @Override public void characters( char ch[], int start, int length ) throws SAXException
                {
                    String value = new String( ch, start, length );
                    if( "literal".equals( element ) )
                    {
                        if( type == null )
                        {
                            type = value;
                        }
                        else
                        {
                            id = value;
                        }
                    }
                }

                @Override public void endElement( String uri, String localName, String qName ) throws SAXException
                {
                    element = null;

                    if( localName.equals( "result" ) )
                    {
                        entities.add( new QualifiedIdentity( id, type ) );
                    }
                }
            } );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

        return entities;
    }

    public QualifiedIdentity findEntity( String resultType, BooleanExpression whereClause )
        throws EntityFinderException
    {
/*
        try
        {
            final RepositoryConnection connection = repository.getConnection();
            // TODO shall we support different implementation as SERQL?
            final RdfQueryParser parser = new SparqlRdfQueryParser();
            final TupleQuery tupleQuery = connection.prepareTupleQuery(
                parser.getQueryLanguage(),
                parser.getQuery( resultType, whereClause, null, null, null )
            );
            final TupleQueryResult result = tupleQuery.evaluate();
            try
            {
                while( result.hasNext() )
                {
                    final BindingSet bindingSet = result.next();
                    final Value identifier = bindingSet.getValue( "identity" );
                    final Value entityClass = bindingSet.getValue( "entityType" );
                    //TODO Shall we throw an exception if there is no binding for identifier = query parser is not right
                    if( identifier != null )
                    {
                        System.out.println( entityClass.stringValue() + " -> " + identifier.stringValue() );
                        return new QualifiedIdentity( identifier.stringValue(), entityClass.stringValue() );
                    }
                }

                return null;
            }
            finally
            {
                result.close();
                connection.close();
            }
        }
        catch( RepositoryException e )
        {
            throw new EntityFinderException( e );
        }
        catch( MalformedQueryException e )
        {
            throw new EntityFinderException( e );
        }
        catch( QueryEvaluationException e )
        {
            throw new EntityFinderException( e );
        }
*/
        return null;
    }

    public long countEntities( String resultType, BooleanExpression whereClause )
        throws EntityFinderException
    {
        long entityCount = 0;
/*
        try
        {
            final RepositoryConnection connection = repository.getConnection();
            // TODO shall we support different implementation as SERQL?
            final RdfQueryParser parser = new SparqlRdfQueryParser();
            final TupleQuery tupleQuery = connection.prepareTupleQuery(
                parser.getQueryLanguage(),
                parser.getQuery( resultType, whereClause, null, null, null )
            );
            final TupleQueryResult result = tupleQuery.evaluate();
            try
            {
                while( result.hasNext() )
                {
                    result.next();
                    entityCount++;
                }
                return entityCount;
            }
            finally
            {
                result.close();
                connection.close();
            }
        }
        catch( RepositoryException e )
        {
            throw new EntityFinderException( e );
        }
        catch( MalformedQueryException e )
        {
            throw new EntityFinderException( e );
        }
        catch( QueryEvaluationException e )
        {
            throw new EntityFinderException( e );
        }
*/
        return entityCount;
    }
}