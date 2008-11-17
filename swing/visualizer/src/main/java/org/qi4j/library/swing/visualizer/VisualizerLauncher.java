/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.swing.visualizer;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.swing.visualizer.assembly.VisualizerAssembler;
import org.qi4j.object.ObjectBuilderFactory;
import org.qi4j.structure.Application;
import org.qi4j.structure.Module;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class VisualizerLauncher
{
    private static final String LAYER_VISUALIZER = "visualizer";
    private static final String MODULE_VISUALIZER = "visualizer";

    public VisualizerLauncher()
    {
    }

    public final void visualize( Application anApplicationToShow )
        throws Exception
    {
        updateLookAndFeel();

        Application application = bootstrapVisualizer();
        Qi4jApplicationVisualizer visualizer = createVisualizer( application );

        if( anApplicationToShow == null )
        {
            anApplicationToShow = application;
        }
        visualizer.show( anApplicationToShow );
    }

    private Application bootstrapVisualizer()
        throws Exception
    {
        Energy4Java energy4Java = new Energy4Java();
        ApplicationAssembly applicationAssembly = energy4Java.newApplicationAssembly();
        applicationAssembly.setName( "Qi4j visualizer" );

        LayerAssembly visualizerLayer = applicationAssembly.newLayerAssembly( LAYER_VISUALIZER );
        ModuleAssembly visualizerModule = visualizerLayer.newModuleAssembly( MODULE_VISUALIZER );
        visualizerModule.addAssembler( new VisualizerAssembler( false ) );

        Application application = energy4Java.newApplication( applicationAssembly );
        application.activate();
        return application;
    }

    private Qi4jApplicationVisualizer createVisualizer( Application application )
    {
        Module module = application.findModule( LAYER_VISUALIZER, MODULE_VISUALIZER );
        ObjectBuilderFactory objectBuilderFactory = module.objectBuilderFactory();
        return objectBuilderFactory.newObject( Qi4jApplicationVisualizer.class );
    }

    private void updateLookAndFeel()
    {
        try
        {
            UIManager.setLookAndFeel( new Plastic3DLookAndFeel() );
        }
        catch( UnsupportedLookAndFeelException e )
        {
            // Ignore
        }
    }
}
