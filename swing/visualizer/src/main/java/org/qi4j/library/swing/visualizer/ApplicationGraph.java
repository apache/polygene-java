/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Sonny Gill. All Rights Reserved.
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.library.swing.visualizer;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;
import org.qi4j.library.swing.visualizer.detailPanel.DetailPanel;
import org.qi4j.library.swing.visualizer.overview.OverviewPanel;
import org.qi4j.library.swing.visualizer.overview.SelectionListener;
import org.qi4j.library.swing.visualizer.overview.descriptor.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.overview.descriptor.EntityDetailDescriptor;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.structure.LayerDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;
import org.qi4j.structure.Application;

/**
 * TODO
 */
public class ApplicationGraph
{
    private DetailPanel detailPanel;

    public void show( Application anApplication )
        throws IllegalArgumentException
    {
        // Application Panel
        if( !( anApplication instanceof ApplicationSPI ) )
        {
            String className = ApplicationSPI.class.getName();
            throw new IllegalArgumentException(
                "Argument [anApplication] must not be an instance of [" + className + "]"
            );
        }
        ApplicationSPI applicationSPI = (ApplicationSPI) anApplication;
        OverviewPanel overviewPanel = new OverviewPanel( applicationSPI, new CompositeSelectionControl() );

        // detail panel
        detailPanel = new DetailPanel();

        // Assemble the frame
        String applicatioName = anApplication.name();
        JFrame frame = new JFrame( "Application [" + applicatioName + "] Graph" );
        JSplitPane mainPane = new JSplitPane( VERTICAL_SPLIT, overviewPanel, detailPanel );
        mainPane.setOneTouchExpandable( true );
        frame.add( mainPane );

        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.pack();
        frame.setVisible( true );

        overviewPanel.graphShown();
    }

    private class CompositeSelectionControl
        implements SelectionListener
    {
        public void onCompositeSelected( CompositeDetailDescriptor aDescriptor )
        {
            detailPanel.display( aDescriptor );
        }

        public void onEntitySelected( EntityDetailDescriptor aDescriptor )
        {
            onCompositeSelected( aDescriptor );
        }

        public void onServiceSelected( ServiceDescriptor aDescriptor )
        {
            // TODO
        }

        public void onObjectSelected( ObjectDescriptor aDescriptor )
        {
            // TODO
        }

        public void onApplicationSelected( ApplicationDescriptor aDescriptor )
        {
            // TODO
        }

        public void onLayerSelected( LayerDescriptor aDescriptor )
        {
            // TODO
        }

        public void onModuleSelected( ModuleDescriptor aDescriptor )
        {
            // TODO
        }
    }
}
