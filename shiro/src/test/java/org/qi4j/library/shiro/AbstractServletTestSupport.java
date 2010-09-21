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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpHost;
import org.apache.http.cookie.Cookie;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Before;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.servlet.Qi4jServlet;
import org.qi4j.library.servlet.lifecycle.AbstractQi4jServletBootstrap;
import org.qi4j.library.shiro.tests.SecuredService;

public abstract class AbstractServletTestSupport
        implements Assembler
{

    protected static final String TEST_LAYER = "Layer 1";
    protected static final String TEST_MODULE = "Module 1";
    protected static final String SECURED_SERVLET_PATH = "/test";
    private Server jetty;
    protected HttpHost httpHost;

    @Before
    public void before()
            throws Exception
    {
        InetAddress loopback = InetAddress.getLocalHost();
        int port = findFreePortOnIfaceWithPreference( loopback, 8989 );
        httpHost = new HttpHost( loopback.getHostAddress(), port );
        jetty = new Server( port );
        configureJetty( jetty );
        ServletContextHandler sch = new ServletContextHandler( jetty, "/", ServletContextHandler.SESSIONS | ServletContextHandler.NO_SECURITY );
        sch.addEventListener( new AbstractQi4jServletBootstrap()
        {

            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                    throws AssemblyException
            {
                ApplicationAssembly app = applicationFactory.newApplicationAssembly();
                ModuleAssembly module = app.layerAssembly( TEST_LAYER ).moduleAssembly( TEST_MODULE );

                AbstractServletTestSupport.this.assemble( module );

                return app;
            }

        } );
        sch.addServlet( ServletUsingSecuredService.class, SECURED_SERVLET_PATH );
        configureServletContext( sch );
        jetty.start();
    }

    protected void configureJetty( Server jetty )
            throws Exception
    {
    }

    protected void configureServletContext( ServletContextHandler sch )
            throws Exception
    {
    }

    @After
    public void after()
            throws Exception
    {
        if ( jetty != null ) {
            jetty.stop();
        }
    }

    public static class ServletUsingSecuredService
            extends Qi4jServlet
    {

        public static final String OK = "OK";

        @Override
        protected void doGet( HttpServletRequest req, HttpServletResponse resp )
                throws ServletException, IOException
        {
            Module module = application.findModule( TEST_LAYER, TEST_MODULE );
            SecuredService service = module.serviceFinder().<SecuredService>findService( SecuredService.class ).get();
            service.doSomethingThatRequiresNothing();
            service.doSomethingThatRequiresUser();
            service.doSomethingThatRequiresPermissions();
            service.doSomethingThatRequiresRoles();
            PrintWriter out = resp.getWriter();
            out.print( OK );
            out.close();
        }

    }

    protected static void soutCookies( Iterable<Cookie> cookies )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "\nLogging cookies for the curious" );
        for ( Cookie eachCookie : cookies ) {
            sb.append( "\t" ).append( eachCookie.getName() ).append( ": " ).append( eachCookie.getValue() ).
                    append( " ( " ).append( eachCookie.getDomain() ).append( " - " ).append( eachCookie.getPath() ).append( " )" );
        }
        System.out.println( sb.append( "\n" ).toString() );
    }

    protected static int findFreePortOnIfaceWithPreference( final InetAddress address, final int prefered )
            throws IOException
    {
        ServerSocket server;
        if ( prefered > 0 ) {
            server = new ServerSocket( prefered, 1, address );
        } else {
            server = new ServerSocket( 0, 1, address );
        }
        int port = server.getLocalPort();
        server.close();
        return port;
    }

}
