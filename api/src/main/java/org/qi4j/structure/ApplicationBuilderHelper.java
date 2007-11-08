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

package org.qi4j.structure;

/**
 * TODO
 */
public class ApplicationBuilderHelper
{
    ApplicationBuilderFactory applicationBuilderFactory;

    public ApplicationBuilderHelper( ApplicationBuilderFactory applicationBuilder )
    {
        this.applicationBuilderFactory = applicationBuilder;
    }

    public Application newApplication( Assembly assembly )
    {
        return newApplication( new Assembly[][][]{ { { assembly } } } );
    }

    public Application newApplication( Assembly[][][] assemblies )
    {
        ApplicationBuilder applicationBuilder = applicationBuilderFactory.newApplicationBuilder();

        // Build all layers bottom-up
        LayerBuilder below = null;
        for( int layer = assemblies.length - 1; layer >= 0; layer-- )
        {
            // Create Layer
            LayerBuilder lb = applicationBuilder.newLayerBuilder();
            for( int module = 0; module < assemblies[ layer ].length; module++ )
            {
                // Create Module
                ModuleBuilder mb = lb.newModuleBuilder();
                for( int assembly = 0; assembly < assemblies[ layer ][ module ].length; assembly++ )
                {
                    // Register Assembly
                    mb.addAssembly( assemblies[ layer ][ module ][ assembly ] );
                }
            }
            if( below != null )
            {
                lb.uses( below ); // Link layers
            }
            below = lb;
        }
        return applicationBuilder.newApplication();
    }
}
