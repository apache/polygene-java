/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.test.structure;

import org.qi4j.bootstrap.AbstractAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.ApplicationFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblerException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.runtime.structure.ApplicationContext;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class StructureTest
    extends AbstractQi4jTest
{
    public void testStructure()
    {
        ApplicationAssemblyFactory aaf = new ApplicationAssemblyFactory();
        ApplicationFactory af = new ApplicationFactory( runtime, aaf );
        try
        {
            ApplicationAssembly ab = aaf.newApplicationAssembly();
            {
                LayerAssembly applicationLayer = ab.newLayerBuilder();
                {
                    {
                        ModuleAssembly mb = applicationLayer.newModuleAssembly();
                        mb.addAssembly( new DomainApplicationAssembler() );
                    }
                }

                LayerAssembly viewLayer = ab.newLayerBuilder();
                {
                    {
                        ModuleAssembly mb = viewLayer.newModuleAssembly();
                        mb.addAssembly( new ViewAssembler() );
                    }
                    viewLayer.uses( applicationLayer );
                }
            }
            ApplicationContext applicationContext = af.newApplication( ab );
        }
        catch( AssemblerException e )
        {
            e.printStackTrace();
        }

        Assembler[][][] assemblers = new Assembler[][][]
            {
                { // User Interface layer
                  {
                      new ViewAssembler()
                  }
                },
                { // Application layer
                  {
                      new DomainApplicationAssembler()
                  }
                },
                { // Domain layer
                  {
                      new DomainModelAssembler()
                  }
                },
                { // Infrastructure layer
                  {
                      new InfrastructureAssembler()
                  }
                }
            };

        try
        {
            ApplicationContext appContext = af.newApplication( assemblers );
        }
        catch( AssemblerException e )
        {
            e.printStackTrace();
        }
    }

    public void assemble( ModuleAssembly module ) throws AssemblerException
    {
    }

    static class ViewAssembler
        extends AbstractAssembler
    {

    }

    static class DomainApplicationAssembler
        extends AbstractAssembler
    {

    }

    static class DomainModelAssembler
        extends AbstractAssembler
    {

    }

    static class InfrastructureAssembler
        extends AbstractAssembler
    {

    }
}
