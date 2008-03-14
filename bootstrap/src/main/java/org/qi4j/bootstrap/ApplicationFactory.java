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

package org.qi4j.bootstrap;

import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.runtime.structure.ApplicationContext;

/**
 * Factory for ApplicationContext.
 */
public final class ApplicationFactory
{
    private Qi4jRuntime runtime;
    private ApplicationAssemblyFactory applicationAssemblyFactory;

    public ApplicationFactory( Qi4jRuntime runtime, ApplicationAssemblyFactory applicationAssembly )
    {
        this.runtime = runtime;
        this.applicationAssemblyFactory = applicationAssembly;
    }

    public ApplicationContext newApplication( Assembler assembler )
        throws AssemblyException
    {
        return newApplication( new Assembler[][][]{ { { assembler } } } );
    }

    public ApplicationContext newApplication( Assembler[][][] assemblers )
        throws AssemblyException
    {
        ApplicationAssembly applicationAssembly = applicationAssemblyFactory.newApplicationAssembly();
        applicationAssembly.setName( "Application " );

        // Build all layers bottom-up
        LayerAssembly below = null;
        for( int layer = assemblers.length - 1; layer >= 0; layer-- )
        {
            // Create Layer
            LayerAssembly lb = applicationAssembly.newLayerAssembly();
            lb.setName( "Layer " + layer );
            for( int module = 0; module < assemblers[ layer ].length; module++ )
            {
                // Create Module
                ModuleAssembly mb = lb.newModuleAssembly();
                mb.setName( "Module " + ( module + 1 ) );
                for( int assembly = 0; assembly < assemblers[ layer ][ module ].length; assembly++ )
                {
                    // Register Assembler
                    mb.addAssembly( assemblers[ layer ][ module ][ assembly ] );
                }
            }
            if( below != null )
            {
                lb.uses( below ); // Link layers
            }
            below = lb;
        }
        return newApplication( applicationAssembly );
    }

    public ApplicationContext newApplication( ApplicationAssembly applicationAssembly )
    {
        return new ApplicationBuilder( runtime ).newApplicationContext( applicationAssembly );
    }
}
