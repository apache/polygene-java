/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.vaadin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static org.junit.Assert.*;
import org.junit.Test;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.library.http.JettyConfiguration;
import org.qi4j.library.http.JettyServiceAssembler;
import static org.qi4j.library.http.Servlets.*;
import org.qi4j.test.AbstractQi4jTest;

/**
 * @author Paul Merlin
 */
public class VaadinQi4jTest
        extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly ma )
            throws AssemblyException
    {
        new JettyServiceAssembler().assemble( ma );
        ma.services( MemoryEntityStoreService.class );

        JettyConfiguration config = ma.forMixin( JettyConfiguration.class ).declareDefaults();
        config.port().set( 8041 );
        config.resourcePath().set( "/tmp" );

        addServlets( serve( "/mortals/*" ).with( Qi4jVaadinApplicationServletService.class ) ).to( ma );

        ma.services( GreetService.class );
        ma.objects( MyVaadinApplication.class );

    }

    @Test
    public void test()
            throws InterruptedException, IOException
    {
        String url = "http://localhost:8041/mortals";
        String expected = "You have to enable javascript in your browser to use an application built with Vaadin.";

        String got = "";
        String eachLine;
        BufferedReader r = new BufferedReader( new InputStreamReader( new URL( url ).openStream() ) );
        while ( ( eachLine = r.readLine() ) != null ) {
            got += eachLine;
        }

        assertTrue( got.contains( expected ) );

        // Uncomment this, run the test and go to http://localhost:8041/mortals to see it working :)
        // Thread.sleep( Integer.MAX_VALUE );
    }

}
