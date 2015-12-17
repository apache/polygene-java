/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.library.rest.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.rest.client.api.ContextResourceClient;
import org.apache.zest.library.rest.client.api.ContextResourceClientFactory;
import org.apache.zest.library.rest.client.api.ErrorHandler;
import org.apache.zest.library.rest.client.api.HandlerCommand;
import org.apache.zest.library.rest.client.spi.ResponseHandler;
import org.apache.zest.library.rest.client.spi.ResponseReader;
import org.apache.zest.library.rest.client.spi.ResultHandler;
import org.apache.zest.library.rest.common.ValueAssembler;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.restlet.Client;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.apache.zest.library.rest.client.api.HandlerCommand.refresh;
import static org.apache.zest.test.util.Assume.assumeConnectivity;

/**
 * Reads Qi4j Github commits on develop ATOM feed and prints out all title and detail url for each entry.
 * This is an example of how to use the RSS client for something more generic that was not produced by Zest REST server
 * library.
 */
public class RssReaderTest
    extends AbstractZestTest
{

    @BeforeClass
    public static void beforeRssReaderTest()
    {
        assumeConnectivity( "github.com", 443 );
    }

    private ContextResourceClient crc;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // General setup of client
        new OrgJsonValueSerializationAssembler().assemble( module );
        new ClientAssembler().assemble( module );
        new ValueAssembler().assemble( module );
    }

    @Test
    public void testReadRssFeed()
    {
        Client client = new Client( Protocol.HTTPS );
        Reference ref = new Reference( "https://github.com/Qi4j/qi4j-sdk/commits/develop.atom" );
        ContextResourceClientFactory contextResourceClientFactory = objectFactory.newObject( ContextResourceClientFactory.class, client );

        contextResourceClientFactory.registerResponseReader( new ResponseReader()
        {
            @Override
            public Object readResponse( Response response, Class<?> resultType )
                throws ResourceException
            {
                if( resultType.equals( Document.class ) )
                {
                    try
                    {
                        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                        documentBuilderFactory.setNamespaceAware( false );
                        return documentBuilderFactory.newDocumentBuilder().parse( response.getEntity().getStream() );
                    }
                    catch( Exception e )
                    {
                        throw new ResourceException( e );
                    }
                }

                return null;
            }
        } );

        contextResourceClientFactory.setErrorHandler( new ErrorHandler().onError( ErrorHandler.RECOVERABLE_ERROR, new ResponseHandler()
        {
            @Override
            public HandlerCommand handleResponse( Response response, ContextResourceClient client )
            {
                System.out.println( ">> REFRESH on recoverable error: " + response.getStatus() );
                return refresh();
            }
        } ) );

        crc = contextResourceClientFactory.newClient( ref );

        crc.onResource( new ResultHandler<Document>()
        {
            Iterator<Node> itemNodes;

            @Override
            public HandlerCommand handleResult( Document result, ContextResourceClient client )
            {
                try
                {
                    final XPath xPath = XPathFactory.newInstance().newXPath();

                    System.out.println( "== " + xPath.evaluate( "feed/title", result ) + " ==" );

                    final NodeList nodes = (NodeList) xPath.evaluate( "feed/entry", result, XPathConstants.NODESET );
                    List<Node> items = new ArrayList<>();
                    for( int i = 0; i < nodes.getLength(); i++ )
                    {
                        items.add( nodes.item( i ) );
                    }

                    itemNodes = items.iterator();

                    return processEntry( xPath );
                }
                catch( XPathExpressionException e )
                {
                    throw new ResourceException( e );
                }
            }

            private HandlerCommand processEntry( final XPath xPath )
                throws XPathExpressionException
            {
                if( !itemNodes.hasNext() )
                {
                    return null;
                }

                Node item = itemNodes.next();

                String title = xPath.evaluate( "title", item );
                String detailUrl = xPath.evaluate( "link/@href", item );

                System.out.println( "-- " + title + " --" );
                System.out.println( "-- " + detailUrl + " --" );

                return processEntry( xPath );
            }
        } );

        crc.start();
    }
}
