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

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;
import org.qi4j.library.swing.visualizer.detailPanel.DetailPanel;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptorBuilder;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.overview.OverviewPanel;
import org.qi4j.library.swing.visualizer.overview.SelectionListener;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.structure.Application;

/**
 * TODO
 *
 * @author Sonny Gill
 * @author edward.yakop@gmail.com
 * @since 0.4
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
        ApplicationDetailDescriptorBuilder appDescBuilder = new ApplicationDetailDescriptorBuilder();
        ApplicationDetailDescriptor appDetailDescriptor =
            appDescBuilder.createApplicationDetailDescriptor( applicationSPI );

        OverviewPanel overviewPanel = new OverviewPanel( appDetailDescriptor, new CompositeSelectionControl() );
        overviewPanel.setPreferredSize( new Dimension( 800, 400 ) );

        // detail panel
        detailPanel = new DetailPanel();
        // Default display the application
        detailPanel.displayApplication( appDetailDescriptor );
        detailPanel.setPreferredSize( new Dimension( 800, 300 ) );

        // Assemble the frame
        String applicatioName = anApplication.name();
        JFrame frame = new JFrame( "Application [" + applicatioName + "] Graph" );

        JSplitPane mainPane = new JSplitPane( VERTICAL_SPLIT, overviewPanel, detailPanel );
        frame.add( mainPane );
        mainPane.setOneTouchExpandable( true );

        overviewPanel.showGraph();

        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.pack();
        frame.setVisible( true );
    }

    private class CompositeSelectionControl
        implements SelectionListener
    {
        public void onApplicationSelected( ApplicationDetailDescriptor aDescriptor )
        {
            detailPanel.displayApplication( aDescriptor );
        }

        public void onLayerSelected( LayerDetailDescriptor aDescriptor )
        {
            detailPanel.displayLayer( aDescriptor );
        }

        public void onModuleSelected( ModuleDetailDescriptor aDescriptor )
        {
            detailPanel.displayModule( aDescriptor );
        }

        public void onServiceSelected( ServiceDescriptor aDescriptor )
        {
            detailPanel.displayService( aDescriptor );
        }

        public void onEntitySelected( EntityDetailDescriptor aDescriptor )
        {
            detailPanel.displayEntity( aDescriptor );
        }

        public void onCompositeSelected( CompositeDetailDescriptor aDescriptor )
        {
            detailPanel.displayComposite( aDescriptor );
        }

        public void onObjectSelected( ObjectDetailDescriptor aDescriptor )
        {
            detailPanel.displayObject( aDescriptor );
        }
    }
}
