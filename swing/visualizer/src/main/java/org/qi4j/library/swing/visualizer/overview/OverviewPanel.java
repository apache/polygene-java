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
package org.qi4j.library.swing.visualizer.overview;

import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.PAGE_START;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.overview.internal.toolbar.OverviewToolbar;
import org.qi4j.library.swing.visualizer.overview.internal.visualization.PrefuseJScrollPane;
import org.qi4j.library.swing.visualizer.overview.internal.visualization.Qi4jApplicationDisplay;
import org.qi4j.library.swing.visualizer.overview.internal.visualization.Qi4jApplicationVisualization;

/**
 * TODO
 *
 * @author Sonny Gill
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public class OverviewPanel extends JPanel
{
    private static final Dimension PREFERRED_SIZE = new Dimension( 800, 600 );

    private final Qi4jApplicationDisplay display;

    public OverviewPanel( ApplicationDetailDescriptor anAppDescriptor, SelectionListener aListener )
    {
        super( new BorderLayout() );

        setPreferredSize( PREFERRED_SIZE );

        display = new Qi4jApplicationDisplay( anAppDescriptor, aListener );
        PrefuseJScrollPane displayScrollPane = new PrefuseJScrollPane( display );
        add( displayScrollPane, CENTER );

        // Toolbar
        JToolBar toolbar = new OverviewToolbar( display );
        add( toolbar, PAGE_START );
    }

    public final void showGraph()
    {
        display.zoomToFitContainer();
    }

    @Deprecated
    public void clearAllNodesSelections()
    {
        Qi4jApplicationVisualization visualization = display.getVisualization();
        visualization.clearAllNodeSelections();
    }

    @Deprecated
    public void selectNodeByName( String aNodeName )
        throws IllegalArgumentException
    {
        validateNotNull( "aNodeName", aNodeName );

        Qi4jApplicationVisualization visualization = display.getVisualization();
        visualization.selectNodeByName( aNodeName );
    }
}