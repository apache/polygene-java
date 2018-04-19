/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.tools.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.activation.PassivationException;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.bootstrap.ApplicationAssembler;
import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.Energy4Java;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.tools.model.descriptor.ApplicationDetailDescriptor;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.joining;
import static org.apache.polygene.tools.model.descriptor.ApplicationDetailDescriptorBuilder.createApplicationDetailDescriptor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DocumentationSupport
{
    interface HttpService {}

    interface MyDomain {}

    @Test
    public void usage() throws ActivationException, PassivationException, IOException
    {
        // START SNIPPET: usage
        ApplicationAssembler assembler = // (1)
            // END SNIPPET: usage
            applicationFactory -> {
                ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
                assembly.setName( "my-app" );
                assembly.setMode( Application.Mode.staging );

                LayerAssembly network = assembly.layer( "network" );
                ModuleAssembly http = network.module( "http" );
                http.services( HttpService.class ).instantiateOnStartup();

                LayerAssembly application = assembly.layer( "application" );
                ModuleAssembly domain = application.module( "domain" );
                domain.transients( MyDomain.class ).visibleIn( Visibility.application );

                network.uses( application );

                return assembly;
            };
        // START SNIPPET: usage
        Energy4Java polygene = new Energy4Java(); // (2)
        ApplicationDescriptor model = polygene.newApplicationModel( assembler ); // (3)
        ApplicationDetailDescriptor detailedModel = createApplicationDetailDescriptor( model ); // (4)

        System.out.println( detailedModel.toJson().toString() ); // (5)

        Application application = model.newInstance( polygene.spi() ); // (6)
        try
        {
            application.activate();
            // END SNIPPET: usage
            ClassLoader loader = getClass().getClassLoader();
            try( InputStream input = loader.getResourceAsStream( "doc-support-report.json" ) )
            {
                String text = new BufferedReader( new InputStreamReader( input ) )
                    .lines()
                    .filter( line -> !line.startsWith( "//" ) )
                    .collect( joining( "\n" ) );
                JsonObject reference = Json.createReader( new StringReader( text ) ).readObject();

                JsonObject detailedModelReport = detailedModel.toJson();

                StringWriter writer = new StringWriter();
                Json.createWriterFactory( singletonMap( JsonGenerator.PRETTY_PRINTING, true ) )
                    .createWriter( writer )
                    .write( detailedModelReport );
                System.out.println( "--------\n" + writer.toString() );

                assertThat( reference, equalTo( detailedModelReport ) );
            }
            // START SNIPPET: usage
        }
        finally
        {
            application.passivate();
        }
        // END SNIPPET: usage
    }
}
