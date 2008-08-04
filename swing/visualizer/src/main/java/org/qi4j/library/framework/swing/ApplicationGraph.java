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

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
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

        JPanel leftPanel = createLeftPanel( applicationPanel );

        JPanel detailsPanel = new JPanel();
        detailsPanel.add( new JLabel( "Details go here" ) );

        frame.getContentPane();
        frame.add( leftPanel, BorderLayout.WEST );
        frame.add( detailsPanel, BorderLayout.CENTER );

        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.pack();
        frame.setVisible( true );

    }

    private JPanel createLeftPanel( JPanel applicationPanel )
    {
        JPanel panel = new JPanel( new BorderLayout() );

        JPanel compositePanel = new JPanel();
        compositePanel.add( new JLabel( "Composite goes here" ) );

        panel.add( applicationPanel, BorderLayout.CENTER );
        panel.add( compositePanel, BorderLayout.SOUTH );

        return panel;
    }

}
