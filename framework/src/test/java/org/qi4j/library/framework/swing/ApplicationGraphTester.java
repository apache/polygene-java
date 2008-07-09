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

package org.qi4j.library.framework.swing;

import org.qi4j.bootstrap.ApplicationFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.LayerName;
import org.qi4j.bootstrap.ModuleName;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.runtime.structure.ApplicationInstance;
import org.qi4j.composite.Composite;

/**
 * TODO
 */
public class ApplicationGraphTester
{
    public static void main( String[] args )
        throws Exception
    {
        Assembler[][][] assemblers = new Assembler[][][]
            {
                {
                    {
                        new LayerName( "UI" ),
                        new ModuleName( "Swing" )
                    },
                    {
                        new ModuleName( "Plugin 1" )
                    },
                    {
                        new ModuleName( "Plugin 2" ),
                        new Assembler()
                        {
                            public void assemble( ModuleAssembly module ) throws AssemblyException
                            {
                                module.addComposites( UIComposite.class );
                            }
                        }
                    }
                },
                {
                    {
                        new LayerName( "Domain" ),
                        new ModuleName( "Some domain" ),
                        new DomainAssembler(),
                    }
                },
                {
                    {
                        new LayerName( "Infrastructure" ),
                        new ModuleName( "Database" )
                    }
                }
            };

        ApplicationInstance app = (ApplicationInstance) new ApplicationFactory().newApplication( assemblers );

        new ApplicationGraph().show( app.model() );
    }

    private static class DomainAssembler implements Assembler
    {
        public void assemble( ModuleAssembly module ) throws AssemblyException
        {
            module.addComposites( ADomainComposite.class );
            module.addComposites( BDomainComposite.class );
        }
    }

    private static interface ADomainComposite extends Composite
    {
        
    }

    private static interface BDomainComposite extends Composite
    {

    }

    private static interface UIComposite extends Composite
    {

    }
}
