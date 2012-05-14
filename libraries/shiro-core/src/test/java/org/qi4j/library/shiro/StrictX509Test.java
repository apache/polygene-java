/*
 * Copyright (c) 2010 Paul Merlin <paul@nosphere.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.qi4j.library.shiro;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import org.apache.http.HttpHost;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.shiro.tests.x509.StrictX509TestAssembler;
import org.qi4j.library.shiro.tests.x509.X509FixturesData;
import org.qi4j.library.shiro.web.servlet.Qi4jShiroServletFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.Assert.assertEquals;

public class StrictX509Test
        extends AbstractServletTestSupport
{

    private static final Logger LOGGER = LoggerFactory.getLogger( StrictX509Test.class );

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new StrictX509TestAssembler().assemble( module );
    }

    @Override
    protected void configureJetty( Server jetty )
            throws Exception
    {
        InetAddress lo = InetAddress.getLocalHost();
        int sslPort = findFreePortOnIfaceWithPreference( lo, 8443 );
        httpHost = new HttpHost( lo.getHostAddress(), sslPort, "https" );

        SslSelectChannelConnector sslConnector = new SslSelectChannelConnector();
        sslConnector.setPort( httpHost.getPort() );
        sslConnector.setNeedClientAuth( true );
        sslConnector.setSslContext( X509FixturesData.serverSSLContext() );
        sslConnector.setAllowRenegotiate( false );

        jetty.addConnector( sslConnector );
    }

    @Override
    protected void configureServletContext( ServletContextHandler sch )
    {
        FilterHolder filterHolder = new FilterHolder( new Qi4jShiroServletFilter() );
        filterHolder.setInitParameter( Qi4jShiroServletFilter.REALM_LAYER_PARAM, TEST_LAYER );
        filterHolder.setInitParameter( Qi4jShiroServletFilter.REALM_MODULE_PARAM, TEST_MODULE );
        filterHolder.setInitParameter( Qi4jShiroServletFilter.FILTER_CHAINS_PARAM, "{\"" + SECURED_SERVLET_PATH + "\":\"authcX509\"}" );

        EnumSet<DispatcherType> dispatches = EnumSet.of( DispatcherType.REQUEST );
        sch.addFilter( filterHolder, SECURED_SERVLET_PATH, dispatches);
    }

    @Test
    public void test()
            throws IOException
    {
        HttpGet get = new HttpGet( SECURED_SERVLET_PATH );
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        DefaultHttpClient client = new DefaultHttpClient();
        SSLSocketFactory sslsf = new SSLSocketFactory( X509FixturesData.clientSSLContext() );
        sslsf.setHostnameVerifier( new AllowAllHostnameVerifier() ); // For unit testing convenience only, do not use in production
        Scheme https = new Scheme( "https", sslsf, httpHost.getPort() );
        client.getConnectionManager().getSchemeRegistry().register( https );

        String response = client.execute( httpHost, get, responseHandler );
        assertEquals( ServletUsingSecuredService.OK, response );
    }

}
