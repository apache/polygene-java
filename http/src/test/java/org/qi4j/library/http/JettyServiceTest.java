/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.http;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.qi4j.library.http.Servlets.addServlets;
import static org.qi4j.library.http.Servlets.serve;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.service.ServiceReference;
import org.qi4j.test.AbstractQi4jTest;

/**
 * @author edward.yakop@gmail.com
 */
public final class JettyServiceTest extends AbstractQi4jTest
{
    protected final static int JETTY_PORT = 2020;

    public final void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        HttpConfiguration configuration = new HttpConfiguration( JETTY_PORT );
        aModule.addAssembler( new JettyServiceAssembler( configuration ) );

        // Hello world servlet related assembly
        addServlets( serve( "/helloWorld" ).with( HelloWorldServletService.class ) ).to( aModule );
    }

    @Test
    public final void testInstantiation()
        throws Throwable
    {
        Iterable<ServiceReference<JettyService>> services =
            serviceLocator.findServices( JettyService.class );
        assertNotNull( services );

        Iterator<ServiceReference<JettyService>> iterator = services.iterator();
        assertTrue( iterator.hasNext() );

        ServiceReference<JettyService> serviceRef = iterator.next();
        assertNotNull( serviceRef );

        JettyService jettyService = serviceRef.get();
        assertNotNull( jettyService );

        URL url = new URL( "http://localhost:2020/helloWorld" );
        URLConnection urlConnection = url.openConnection();
        InputStream inputStream = urlConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) );
        String output = bufferedReader.readLine();

        assertEquals( "Hello World", output );
    }
}
