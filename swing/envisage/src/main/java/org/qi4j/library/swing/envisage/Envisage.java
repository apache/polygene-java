/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.library.swing.envisage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import org.qi4j.library.swing.envisage.tree.ApplicationModelPanel;
import org.qi4j.library.swing.envisage.detail.DetailPanel;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.api.structure.Application;

/**
 * Qi4J Application Viewer
 *
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 *
 */
public class Envisage
{
    protected Energy4Java qi4j;
    protected Application application;

    public void run(Energy4Java qi4j, Application application)
    {
        this.qi4j = qi4j;
        this.application = application;
        
        final JPanel mainPane = createMainPanel();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showMainFrame(mainPane);
            }
        });

    }

    private void showMainFrame(JPanel mainPane) {
        JFrame frame = new JFrame("Envisage");
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        frame.setSize( new Dimension(800, 600) );
        frame.setLayout( new BorderLayout( ) );
        frame.add( mainPane, BorderLayout.CENTER );
        frame.setVisible( true );
    }

    private JPanel createMainPanel()
    {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout( new BorderLayout( ) );

        JSplitPane splitPane = new JSplitPane( );
        mainPanel.add( splitPane, BorderLayout.CENTER );

        ApplicationModelPanel appModelPanel = new ApplicationModelPanel();
        appModelPanel.initQi4J( qi4j, application );

        DetailPanel detailPane = new DetailPanel();

        splitPane.setLeftComponent( appModelPanel );
        splitPane.setRightComponent( detailPane );
        splitPane.setDividerLocation( 300 );

        return mainPanel;
    }
}
