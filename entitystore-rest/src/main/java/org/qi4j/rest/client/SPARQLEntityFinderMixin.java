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

import org.openrdf.query.QueryLanguage;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.Activatable;
import org.qi4j.index.rdf.query.CollectingQualifiedIdentityResultCallback;
import org.qi4j.index.rdf.query.QualifiedIdentityResultCallback;
import org.qi4j.index.rdf.query.RdfQueryParser;
import org.qi4j.index.rdf.query.RdfQueryParserFactory;
import org.qi4j.index.rdf.query.SingleQualifiedIdentityResultCallback;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.ext.xml.SaxRepresentation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderAdapter;

/**
 * JAVADOC Add JavaDoc
 */
public class SPARQLEntityFinderMixin
    implements EntityFinder, Activatable
{
    @This
    private Configuration<SPARQLEntityFinderConfiguration> config;

    @Service
    private Uniform client;

    @Service
    private RdfQueryParserFactory rdfQueryParserFactory;

    private Reference sparqlQueryRef;

    public void activate()
        throws Exception
    {
        sparqlQueryRef = new Reference( config.configuration().sparqlUrl().get() );
    }

    public void passivate()
        throws Exception
    {
    }

    public Iterable<EntityReference> findEntities( String resultType, BooleanExpression whereClause,
                                                   OrderBy[] orderBySegments, Integer firstResult, Integer maxResults
    )
        throws EntityFinderException
    {
        CollectingQualifiedIdentityResultCallback callback = new CollectingQualifiedIdentityResultCallback();
        performQuery( resultType, whereClause, orderBySegments, firstResult, maxResults, callback );
        return callback.getEntities();
    }

    public EntityReference findEntity( String resultType, BooleanExpression whereClause )
        throws EntityFinderException
    {
        final SingleQualifiedIdentityResultCallback callback = new SingleQualifiedIdentityResultCallback();
        performQuery( resultType, whereClause, null, null, null, callback );
        return callback.getQualifiedIdentity();
    }

    public long countEntities( String resultType, BooleanExpression whereClause )
        throws EntityFinderException
    {
        return performQuery( resultType, whereClause, null, null, null, null );
    }

    public int performQuery( String resultType,
                             BooleanExpression whereClause,
                             OrderBy[] orderBySegments,
                             Integer firstResult,
                             Integer maxResults,
                             QualifiedIdentityResultCallback callback
    )
        throws EntityFinderException
    {
        try
        {
            // TODO shall we support different implementation as SERQL?
            RdfQueryParser parser = rdfQueryParserFactory.newQueryParser( QueryLanguage.SPARQL );
            String query = parser.getQuery( resultType, whereClause, orderBySegments, firstResult, maxResults );

            Reference queryReference = sparqlQueryRef.clone();
            queryReference.addQueryParameter( "query", query );
            Request request = new Request( Method.GET, queryReference );
            Response response = new Response( request );
            client.handle( request, response );
            if( !response.getStatus().isSuccess() )
            {
                throw new SPARQLEntityFinderException( response.getRequest().getResourceRef(), response.getStatus() );
            }

            SaxRepresentation sax = new SaxRepresentation( response.getEntity() );
            final EntityResultXMLReaderAdapter xmlReaderAdapter = new EntityResultXMLReaderAdapter( callback );
            sax.parse( xmlReaderAdapter );
            return xmlReaderAdapter.getRows();
        }
        catch( SPARQLEntityFinderException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            throw new EntityFinderException( e );
        }
    }

    private static class EntityResultXMLReaderAdapter
        extends XMLReaderAdapter
    {
        private String element;
        private String id;
        private final QualifiedIdentityResultCallback callback;
        private int row = 0;
        private boolean done = false;

        public EntityResultXMLReaderAdapter( QualifiedIdentityResultCallback callback )
            throws SAXException
        {
            this.callback = callback;
        }

        @Override
        public void startElement( String uri, String localName, String qName, Attributes atts )
            throws SAXException
        {
            element = localName;
        }

        @Override
        public void characters( char ch[], int start, int length )
            throws SAXException
        {
            if( "literal".equals( element ) )
            {
                id = String.valueOf( ch, start, length );
            }
        }

        @Override
        public void endElement( String uri, String localName, String qName )
            throws SAXException
        {
            element = null;

            if( localName.equals( "result" ) )
            {
                if( !done && callback != null )
                {
                    final EntityReference entityReference = new EntityReference( id );
                    // todo could also throw flow control exception
                    done = !callback.processRow( row, entityReference );
                    id = null;
                }
                row++;
            }
        }

        public int getRows()
        {
            return row;
        }
    }
}