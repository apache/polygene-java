/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.rest;

import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleName;
import org.qi4j.library.http.JettyServiceAssembler;
import org.qi4j.rest.assembly.RestAssembler;

/**
 * JAVADOC
 */
public class MainAssembler
    implements ApplicationAssembler
{
    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
        throws AssemblyException
    {
        return applicationFactory.newApplicationAssembly( new Assembler[][][]
            {
                // UI
                {
                    {
                        new ModuleName( "Configuration store" ),
                        new MemoryEntityStoreServiceAssembler(),
                    },
                    {
                        new ModuleName( "Restlet servlet" ),
                        new RestletServletAssembler(),
                    },
                    {
                        new ModuleName( "Jetty" ),
                        new JettyServiceAssembler()
                    },
                    {
                        new ModuleName( "JMX Configuration" ),
//                        new JMXAssembler()
                    }
                },
                // Domain
                {
                    {
                        new ModuleName( "Restlet application" ),
                        new RestAssembler(),
                    },
                    {
                        new ModuleName( "RDF" ),
                        new RDFAssembler(),
                    },
                    {
                        new ModuleName( "Domain" ),
                        new DomainAssembler(),
                    }
                },
                // Infrastructure
                {
                    {
                        new ModuleName( "Domain store" ),
                        new MemoryEntityStoreServiceAssembler()
                    }
                }
            } );
    }
}
