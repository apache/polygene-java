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

package org.apache.zest.runtime.bootstrap;

import org.apache.zest.bootstrap.ApplicationAssembly;
import org.apache.zest.bootstrap.ApplicationAssemblyFactory;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;

/**
 * Factory for ApplicationAssembly.
 */
public final class ApplicationAssemblyFactoryImpl
    implements ApplicationAssemblyFactory
{
    @Override
    public ApplicationAssembly newApplicationAssembly( Assembler assembler )
        throws AssemblyException
    {
        return newApplicationAssembly( new Assembler[][][]{ { { assembler } } } );
    }

    @Override
    public ApplicationAssembly newApplicationAssembly( Assembler[][][] assemblers )
        throws AssemblyException
    {
        ApplicationAssembly applicationAssembly = newApplicationAssembly();

        // Build all layers bottom-up
        LayerAssembly below = null;
        for( int layer = assemblers.length - 1; layer >= 0; layer-- )
        {
            // Create Layer
            LayerAssembly layerAssembly = applicationAssembly.layer( "Layer " + ( layer + 1 ) );
            for( int module = 0; module < assemblers[ layer ].length; module++ )
            {
                // Create Module
                ModuleAssembly moduleAssembly = layerAssembly.module( "Module " + ( module + 1 ) );
                for( Assembler assembler : assemblers[ layer ][ module ] )
                {
                    // Register Assembler
                    assembler.assemble( moduleAssembly );
                }
            }
            if( below != null )
            {
                layerAssembly.uses( below ); // Link layers
            }
            below = layerAssembly;
        }
        return applicationAssembly;
    }

    @Override
    public ApplicationAssembly newApplicationAssembly()
    {
        return new ApplicationAssemblyImpl();
    }
}
