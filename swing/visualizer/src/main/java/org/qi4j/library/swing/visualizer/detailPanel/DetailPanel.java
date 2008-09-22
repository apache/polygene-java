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
package org.qi4j.library.swing.visualizer.detailPanel;

import static java.awt.Color.white;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import org.qi4j.composite.NullArgumentException;
import org.qi4j.library.swing.visualizer.detailPanel.internal.DefaultDisplayManager;
import org.qi4j.library.swing.visualizer.overview.descriptor.CompositeDetailDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @author Sonny Gill
 * @since 0.5
 */
public final class DetailPanel extends JSplitPane
{
    private static Dimension methodsPaneSize = new Dimension( 300, 200 );

    private DisplayManager manager;

    public DetailPanel()
    {
        super( HORIZONTAL_SPLIT );

        // Left panel
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize( methodsPaneSize );
        leftPanel.setBackground( white );
        setLeftComponent( leftPanel );

        // right panel
        JComponent rightPanel = createHelpPanel();
        setRightComponent( rightPanel );

        manager = new DefaultDisplayManager();
    }

    public final void setDisplayManager( DisplayManager aManager )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( "aManager", aManager );
        manager = aManager;
    }

    // TODO: This should be localized
    private JComponent createHelpPanel()
    {
        JPanel detailsPanel = new JPanel();
        detailsPanel.setBackground( white );

        StringBuilder buf = new StringBuilder();
        buf.append( "Controls - \n" );
        buf.append( "1. Zoom with mouse scroll wheel\n" );
        buf.append( "2. Pan with mouse\n" );
        buf.append( "3. Double click to zoom in, Shift + Double click to zoom out\n" );
        buf.append( "4. + key to zoom in, - key to zoom out\n" );
        JTextArea textArea = new JTextArea();
        textArea.setText( buf.toString() );

        JScrollPane pane = new JScrollPane( textArea );
        pane.setHorizontalScrollBarPolicy( HORIZONTAL_SCROLLBAR_NEVER );
        return pane;
    }

    public final void display( CompositeDetailDescriptor aDescriptor )
    {
        manager.display( this, aDescriptor );
    }
}
