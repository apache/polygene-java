/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

import org.qi4j.bootstrap.AbstractAssembly;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.ApplicationFactory;
import org.qi4j.bootstrap.Assembly;
import org.qi4j.bootstrap.AssemblyException;
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
                        mb.addAssembly( new DomainApplicationAssembly() );
                    }
                }

                LayerAssembly viewLayer = ab.newLayerBuilder();
                {
                    {
                        ModuleAssembly mb = viewLayer.newModuleAssembly();
                        mb.addAssembly( new ViewAssembly() );
                    }
                    viewLayer.uses( applicationLayer );
                }
            }
            ApplicationContext applicationContext = af.newApplication( ab );
        }
        catch( AssemblyException e )
        {
            e.printStackTrace();
        }

        Assembly[][][] assemblies = new Assembly[][][]
            {
                { // User Interface layer
                  {
                      new ViewAssembly()
                  }
                },
                { // Application layer
                  {
                      new DomainApplicationAssembly()
                  }
                },
                { // Domain layer
                  {
                      new DomainModelAssembly()
                  }
                },
                { // Infrastructure layer
                  {
                      new InfrastructureAssembly()
                  }
                }
            };

        try
        {
            ApplicationContext appContext = af.newApplication( assemblies );
        }
        catch( AssemblyException e )
        {
            e.printStackTrace();
        }
    }

    static class ViewAssembly
        extends AbstractAssembly
    {

    }

    static class DomainApplicationAssembly
        extends AbstractAssembly
    {

    }

    static class DomainModelAssembly
        extends AbstractAssembly
    {

    }

    static class InfrastructureAssembly
        extends AbstractAssembly
    {

    }
}
