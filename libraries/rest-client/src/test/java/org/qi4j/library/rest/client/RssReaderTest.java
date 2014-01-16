package org.qi4j.library.rest.client;

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
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rest.client.api.ContextResourceClient;
import org.qi4j.library.rest.client.api.ContextResourceClientFactory;
import org.qi4j.library.rest.client.api.ErrorHandler;
import org.qi4j.library.rest.client.api.HandlerCommand;
import org.qi4j.library.rest.client.spi.ResponseHandler;
import org.qi4j.library.rest.client.spi.ResponseReader;
import org.qi4j.library.rest.client.spi.ResultHandler;
import org.qi4j.library.rest.common.ValueAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.restlet.Client;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.qi4j.library.rest.client.api.HandlerCommand.refresh;
import static org.qi4j.test.util.Assume.assumeConnectivity;

/**
 * Reads Qi4j Github commits on develop ATOM feed and prints out all title and detail url for each entry.
 * This is an example of how to use the RSS client for something more generic that was not produced by Qi4j REST server
 * library.
 */
public class RssReaderTest
    extends AbstractQi4jTest
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
        ContextResourceClientFactory contextResourceClientFactory = module.newObject( ContextResourceClientFactory.class, client );

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
