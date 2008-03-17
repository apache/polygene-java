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

import org.junit.Test;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.ApplicationFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.runtime.Energy4Java;
import org.qi4j.runtime.Qi4jRuntime;

/**
 * TODO
 */
public class StructureTest
{

    @Test
    public void createApplicationUsingApplicationAssembly()
        throws AssemblyException
    {
        Qi4jRuntime is = new Energy4Java();
        ApplicationAssemblyFactory aaf = new ApplicationAssemblyFactory();
        ApplicationFactory af = new ApplicationFactory( is, aaf );
        ApplicationAssembly ab = aaf.newApplicationAssembly();
        {
            LayerAssembly applicationLayer = ab.newLayerAssembly();
            {
                {
                    ModuleAssembly mb = applicationLayer.newModuleAssembly();
                    mb.addAssembly( new DomainApplicationAssembler() );
                }
            }

            LayerAssembly viewLayer = ab.newLayerAssembly();
            {
                {
                    ModuleAssembly mb = viewLayer.newModuleAssembly();
                    mb.addAssembly( new ViewAssembler() );
                }
                viewLayer.uses( applicationLayer );
            }
        }
        af.newApplication( ab );
    }

    @Test
    public void createApplicationUsingArrayOfAssemblers()
        throws AssemblyException
    {
        Qi4jRuntime is = new Energy4Java();
        ApplicationAssemblyFactory aaf = new ApplicationAssemblyFactory();
        ApplicationFactory af = new ApplicationFactory( is, aaf );

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

        af.newApplication( assemblers );
    }

    static class ViewAssembler
        implements Assembler
    {
        public void assemble( ModuleAssembly module ) throws AssemblyException
        {
        }
    }

    static class DomainApplicationAssembler
        implements Assembler
    {
        public void assemble( ModuleAssembly module ) throws AssemblyException
        {
        }
    }

    static class DomainModelAssembler
        implements Assembler
    {
        public void assemble( ModuleAssembly module ) throws AssemblyException
        {
        }
    }

    static class InfrastructureAssembler
        implements Assembler
    {
        public void assemble( ModuleAssembly module ) throws AssemblyException
        {
        }
    }
}
