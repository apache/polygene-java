/*
 * Copyright (c) 2012, Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.servlet.lifecycle.AbstractQi4jServletBootstrap;
import org.qi4j.test.util.FreePortFinder;

public class ServletTest
{

    private static final String APP_NAME = "FooApplication";

    // START SNIPPET: bootstrap
    public static class FooServletContextListener
            extends AbstractQi4jServletBootstrap
    {

        public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
        {
            ApplicationAssembly appass = applicationFactory.newApplicationAssembly();
            // END SNIPPET: bootstrap
            // Assemble your application here
            appass.setName( APP_NAME );
            ModuleAssembly module = appass.layer( "Layer 1" ).module( "Module 1" );
            module.transients( UselessComposite.class );
            // START SNIPPET: bootstrap
            return appass;
        }

    }
    // END SNIPPET: bootstrap

    public interface UselessComposite
    {
    }

    // START SNIPPET: usage
    public static class FooServlet
            extends Qi4jServlet
    {

        @Override
        protected void doGet( HttpServletRequest req, HttpServletResponse resp )
                throws ServletException, IOException
        {
            // Output the assembled Application's name as an example
            resp.getWriter().println( application().name() );
        }

    }
    // END SNIPPET: usage

    @Test
    public void test()
            throws Exception
    {
        int port = FreePortFinder.findFreePortOnLoopback( 9001 );
        Server server = new Server( port );
        try {

            ServletContextHandler context = new ServletContextHandler();
            context.setContextPath( "/" );
            context.addEventListener( new FooServletContextListener() );
            context.addServlet( FooServlet.class, "/*" );

            server.setHandler( context );
            server.start();

            HttpClient client = new DefaultHttpClient();
            String result = client.execute( new HttpGet( "http://127.0.0.1:" + port + "/" ), new BasicResponseHandler() );
            Assert.assertEquals( APP_NAME, result.trim() );

        } finally {
            server.stop();
        }
    }

}
