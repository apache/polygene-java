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
import org.openrdf.query.QueryLanguage;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.Wrapper;
import org.qi4j.index.rdf.RdfFactory;
import org.qi4j.index.rdf.RdfQueryParser;
import org.qi4j.index.rdf.callback.CollectingQualifiedIdentityResultCallback;
import org.qi4j.index.rdf.callback.QualifiedIdentityResultCallback;
import org.qi4j.index.rdf.callback.SingleQualifiedIdentityResultCallback;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.restlet.Client;
import org.restlet.representation.SaxRepresentation;
import org.restlet.data.Response;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderAdapter;

/**
 * JAVADOC Add JavaDoc
 */
public class SPARQLEntityFinderMixin
    implements EntityFinder
{
    @Service Wrapper<Client> client;
    @Service RdfFactory rdfFactory;

    public Iterable<QualifiedIdentity> findEntities( String resultType, BooleanExpression whereClause,
                                                     OrderBy[] orderBySegments, Integer firstResult, Integer maxResults )
        throws EntityFinderException
    {
        CollectingQualifiedIdentityResultCallback callback = new CollectingQualifiedIdentityResultCallback();
        performQuery( resultType, whereClause, orderBySegments, firstResult, maxResults, callback );
        return callback.getEntities();
    }

    public QualifiedIdentity findEntity( String resultType, BooleanExpression whereClause )
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

    private static class EntityResultXMLReaderAdapter extends XMLReaderAdapter
    {
        private String element;
        private String id;
        private String type;
        private final QualifiedIdentityResultCallback callback;
        private int row = 0;
        private boolean done = false;

        public EntityResultXMLReaderAdapter( QualifiedIdentityResultCallback callback )
            throws SAXException
        {
            this.callback = callback;
        }

        @Override public void startElement( String uri, String localName, String qName, Attributes atts ) throws SAXException
        {
            element = localName;
        }

        @Override public void characters( char ch[], int start, int length ) throws SAXException
        {
            if( "uri".equals( element ) )
            {
                String value = String.valueOf( ch, start, length );
                type = GenericAssociationInfo.toQualifiedName( value );
            }
            else if( "literal".equals( element ) )
            {
                id = String.valueOf( ch, start, length );
            }
        }

        @Override public void endElement( String uri, String localName, String qName ) throws SAXException
        {
            element = null;

            if( localName.equals( "result" ) )
            {
                if( !done && callback != null )
                {
                    final QualifiedIdentity qualifiedIdentity = new QualifiedIdentity( id, type );
                    // todo could also throw flow control exception
                    done = !callback.processRow( row, qualifiedIdentity );
                    id = type = null;
                }
                row++;
            }
        }

        public int getRows()
        {
            return row;
        }
    }

    public int performQuery( String resultType, BooleanExpression whereClause, OrderBy[] orderBySegments, Integer firstResult, Integer maxResults, QualifiedIdentityResultCallback callback )
        throws EntityFinderException
    {
        try
        {
            // TODO shall we support different implementation as SERQL?
            final RdfQueryParser parser = rdfFactory.newQueryParser( QueryLanguage.SPARQL );
            String query = parser.getQuery( resultType, whereClause, orderBySegments, firstResult, maxResults );

            String url = "http://localhost:8040/qi4j/query.rdf?query=" + URLEncoder.encode( query, "UTF-8" );
            Response response = client.get().get( url );
            SaxRepresentation sax = response.getEntityAsSax();
            final EntityResultXMLReaderAdapter xmlReaderAdapter = new EntityResultXMLReaderAdapter( callback );
            sax.parse( xmlReaderAdapter );
            return xmlReaderAdapter.getRows();
        }
        catch( Exception e )
        {
            throw new EntityFinderException( e );
        }
    }

}