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

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.library.swing.envisage.detail.DetailModelPane;
import org.qi4j.library.swing.envisage.event.LinkEvent;
import org.qi4j.library.swing.envisage.event.LinkListener;
import org.qi4j.library.swing.envisage.graph.GraphPane;
import org.qi4j.library.swing.envisage.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ApplicationDetailDescriptorBuilder;
import org.qi4j.library.swing.envisage.tree.TreeModelPane;
import org.qi4j.spi.structure.ApplicationSPI;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class EnvisageFrame extends JFrame
{
    private ResourceBundle bundle = ResourceBundle.getBundle( this.getClass().getName() );

    private JPanel contentPane;
    private JSplitPane graphSplitPane;
    private JSplitPane modelSplitPane;

    private GraphPane graphPane;
    private TreeModelPane treeModelPane;
    private DetailModelPane detailModelPane;

    private Application application;
    private ApplicationDetailDescriptor descriptor;

    private boolean graphItemSelectionInProgress;

    public EnvisageFrame( Energy4Java qi4j, Application application )
    {

        this.application = application;

        setTitle( bundle.getString( "Application.Title" ) );
        setContentPane( contentPane );

        graphPane = new GraphPane();
        treeModelPane = new TreeModelPane();
        detailModelPane = new DetailModelPane();

        treeModelPane.addTreeSelectionListener( new TreeSelectionListener()
        {
            public void valueChanged( TreeSelectionEvent evt )
            {
                applicationModelPaneValueChanged( evt );
            }
        } );

        graphPane.getGraphDisplay().addLinkListener( new LinkListener()
        {
            public void activated( LinkEvent evt )
            {
                graphItemLinkActivated( evt );
            }
        } );

        detailModelPane.addLinkListener( new LinkListener()
        {
            public void activated( LinkEvent evt )
            {
                detailModelPaneLinkActivated( evt );
            }
        } );

        modelSplitPane = new JSplitPane();
        modelSplitPane.setOrientation( JSplitPane.HORIZONTAL_SPLIT );
        modelSplitPane.setDividerLocation( 300 );
        modelSplitPane.setOneTouchExpandable( true );
        modelSplitPane.setLeftComponent( treeModelPane );
        modelSplitPane.setRightComponent( detailModelPane );

        graphSplitPane = new JSplitPane();
        graphSplitPane.setOrientation( JSplitPane.VERTICAL_SPLIT );
        graphSplitPane.setDividerLocation( 512 );
        graphSplitPane.setOneTouchExpandable( true );
        //graphSplitPane.setTopComponent( graphPane );
        //graphSplitPane.setBottomComponent( modelSplitPane );
        graphSplitPane.setTopComponent( modelSplitPane );
        graphSplitPane.setBottomComponent( graphPane );

        contentPane.add( graphSplitPane, BorderLayout.CENTER );
    }

    public void initQi4J()
    {
        graphSplitPane.setDividerLocation( 384 );    // 768/2
        modelSplitPane.setDividerLocation( 300 );

        ApplicationSPI applicationSPI = (ApplicationSPI) application;
        descriptor = ApplicationDetailDescriptorBuilder.createApplicationDetailDescriptor( applicationSPI );
        treeModelPane.initQi4J( application, descriptor );
        graphPane.initQi4J( application, descriptor );
    }

    protected void applicationModelPaneValueChanged( TreeSelectionEvent evt )
    {
        Object obj = treeModelPane.getLastSelected();
        detailModelPane.setDescriptor( obj );
        if( !graphItemSelectionInProgress )
        {
            graphPane.getGraphDisplay().setSelectedValue( obj );
        }
    }

    protected void graphItemLinkActivated( LinkEvent evt )
    {
        graphItemSelectionInProgress = true;
        try
        {
            treeModelPane.setSelectedValue( evt.getObject() );
        }
        finally
        {
            graphItemSelectionInProgress = false;
        }
    }

    protected void detailModelPaneLinkActivated( LinkEvent evt )
    {
        treeModelPane.setSelectedValue( evt.getObject() );
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        contentPane = new JPanel();
        contentPane.setLayout( new BorderLayout( 0, 0 ) );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }
}
