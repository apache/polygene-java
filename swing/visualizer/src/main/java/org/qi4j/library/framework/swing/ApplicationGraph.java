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
package org.qi4j.library.framework.swing;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import org.qi4j.structure.Application;

/**
 * TODO
 */
public class ApplicationGraph
{
    static final int TYPE_APPLICATION = 0;
    static final int TYPE_LAYER = 1;
    static final int TYPE_MODULE = 2;
    static final int TYPE_COMPOSITE = 3;

    static final int TYPE_EDGE_HIDDEN = 100;

    private String applicationName;

    public void show( Application application )
    {
        JFrame frame = new JFrame( "Qi4j Application Graph" );

        ApplicationPanel applicationPanel = new ApplicationPanel( application );

        JPanel detailsPanel = new JPanel();
        detailsPanel.add( new JLabel( "Details go here" ) );
        detailsPanel.setPreferredSize( new Dimension( 400, 600 ) );

        JComponent leftPane = createLeftPane( applicationPanel );
        JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
                                               leftPane, new JScrollPane( detailsPanel ) );
//        splitPane.set
        frame.add( splitPane );

        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.pack();
        frame.setVisible( true );

    }

    private JComponent createLeftPane( JPanel applicationPanel )
    {
        JPanel panel = new JPanel();
        panel.add( new JLabel( "Composite goes here" ) );
        panel.setMinimumSize( new Dimension( 400, 200 ) );
        panel.setPreferredSize( new Dimension( 400, 200 ) );

        return new JSplitPane( JSplitPane.VERTICAL_SPLIT,
                               new JScrollPane( applicationPanel ),
                               new JScrollPane( panel ) );
    }

}
