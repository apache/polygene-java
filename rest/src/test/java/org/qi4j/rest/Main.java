/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ModuleName;
import org.qi4j.library.http.JettyServiceAssembler;
import org.qi4j.library.rdf.entity.EntitySerializerService;
import org.qi4j.rest.assembly.RestAssembler;
import org.qi4j.structure.Application;
import org.qi4j.structure.Visibility;

/**
 * TODO
 */
public class Main
{
    public static void main( String[] args )
        throws Exception
    {
        Energy4Java qi4j = new Energy4Java();
        Application app = qi4j.newApplication( new Assembler[][][]
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
                        new ModuleName( "Configuration store" ),
                        new MemoryEntityStoreServiceAssembler(),
                    }
                }
            } );
        app.activate();
    }
}

class RDFAssembler
    implements Assembler
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( EntitySerializerService.class ).visibleIn( Visibility.layer );
    }
}