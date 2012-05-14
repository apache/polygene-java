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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.shiro.tests.username.UsernameFixtures;
import org.qi4j.library.shiro.tests.username.UsernameTestAssembler;
import org.qi4j.library.shiro.web.servlet.Qi4jShiroServletFilter;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * A unit test showing how to use Shiro in Qi4j Web Applications with Username & Password credentials.
 *
 * Authentication scheme used is HTTP Basic as Shiro does not provide a Digest implementation yet.
 * Remember that the user password goes up from the client to the http server.
 * For now, you can use SSL/TLS to prevent evesdropping but the server code still has access to the
 * password and that can be a problem.
 *
 * TODO Test remember me
 */
public class UsernamePasswordHttpBasicTest
        extends AbstractServletTestSupport
{

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new UsernameTestAssembler().assemble( module );
    }

    @Override
    protected void configureServletContext( ServletContextHandler sch )
    {
        FilterHolder filterHolder = new FilterHolder( new Qi4jShiroServletFilter() );
        filterHolder.setInitParameter( Qi4jShiroServletFilter.REALM_LAYER_PARAM, TEST_LAYER );
        filterHolder.setInitParameter( Qi4jShiroServletFilter.REALM_MODULE_PARAM, TEST_MODULE );
        filterHolder.setInitParameter( Qi4jShiroServletFilter.FILTER_CHAINS_PARAM, "{\"" + SECURED_SERVLET_PATH + "\":\"authcBasic\"}" );

        EnumSet<DispatcherType> dispatches = EnumSet.of( DispatcherType.REQUEST );
        sch.addFilter( filterHolder, SECURED_SERVLET_PATH, dispatches );
    }

    @Test
    public void test()
            throws IOException
    {

        HttpGet get = new HttpGet( SECURED_SERVLET_PATH );
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        DefaultHttpClient client = new DefaultHttpClient();
        client.getCredentialsProvider().setCredentials( new AuthScope( httpHost.getHostName(), httpHost.getPort() ),
                                                        new UsernamePasswordCredentials( UsernameFixtures.USERNAME, new String( UsernameFixtures.PASSWORD ) ) );

        // First request with credentials
        String response = client.execute( httpHost, get, responseHandler );
        assertEquals( ServletUsingSecuredService.OK, response );

        // Cookies logging for the curious
        soutCookies( client.getCookieStore().getCookies() );

        // Second request without credentials, should work thanks to sessions
        client.getCredentialsProvider().clear();
        response = client.execute( httpHost, get, responseHandler );
        assertEquals( ServletUsingSecuredService.OK, response );

    }

}
