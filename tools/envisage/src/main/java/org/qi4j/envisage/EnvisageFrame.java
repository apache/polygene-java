/*
 * Copyright (c) 2009, Tony Kohar. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.envisage;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.envisage.detail.DetailModelPane;
import org.qi4j.envisage.event.LinkEvent;
import org.qi4j.envisage.event.LinkListener;
import org.qi4j.envisage.graph.GraphPane;
import org.qi4j.envisage.print.PDFWriter;
import org.qi4j.envisage.tree.TreeModelPane;
import org.qi4j.tools.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.tools.model.descriptor.ApplicationDetailDescriptorBuilder;

/**
 * Envisage Main Frame
 */
public final class EnvisageFrame
    extends JFrame
{
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle( EnvisageFrame.class.getName() );

    private JPanel contentPane;
    private final JSplitPane graphSplitPane;
    private final JSplitPane modelSplitPane;

    private final GraphPane graphPane;
    private final TreeModelPane treeModelPane;
    private final DetailModelPane detailModelPane;

    private final ApplicationDescriptor application;
    private ApplicationDetailDescriptor descriptor;

    private boolean graphItemSelectionInProgress;

    public EnvisageFrame( ApplicationDescriptor application )
    {
        this.application = application;

        setTitle( BUNDLE.getString( "Application.Title" ) );
        setContentPane( contentPane );

        graphPane = new GraphPane();
        treeModelPane = new TreeModelPane();
        detailModelPane = new DetailModelPane();

        treeModelPane.addTreeSelectionListener( new TreeSelectionListener()
        {
            @Override
            public void valueChanged( TreeSelectionEvent evt )
            {
                applicationModelPaneValueChanged();
            }
        } );

        graphPane.addLinkListener( new LinkListener()
        {
            @Override
            public void activated( LinkEvent evt )
            {
                graphItemLinkActivated( evt );
            }
        } );

        detailModelPane.addLinkListener( new LinkListener()
        {
            @Override
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
        graphSplitPane.setDividerLocation( 384 ); // 768/2
        graphSplitPane.setOneTouchExpandable( true );
        //graphSplitPane.setTopComponent( graphPane );
        //graphSplitPane.setBottomComponent( modelSplitPane );
        graphSplitPane.setTopComponent( modelSplitPane );
        graphSplitPane.setBottomComponent( graphPane );

        this.setJMenuBar( createMenu() );

        contentPane.add( graphSplitPane, BorderLayout.CENTER );
    }

    public void initQi4J()
    {
        modelSplitPane.setDividerLocation( 300 );
        graphSplitPane.setDividerLocation( 384 );

        descriptor = ApplicationDetailDescriptorBuilder.createApplicationDetailDescriptor( application );
        treeModelPane.initQi4J( descriptor );
        graphPane.initQi4J( descriptor );
    }

    /**
     * Create menubar
     *
     * @return JMenuBar
     */
    protected JMenuBar createMenu()
    {
        ActionListener menuActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent evt )
            {
                if( evt.getActionCommand().equals( "export" ) )
                {
                    exportAsPDF();
                }
                if( evt.getActionCommand().equals( "exit" ) )
                {
                    System.exit( 0 );
                }
            }
        };

        // TODO i18n
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu( "File" );
        menu.setMnemonic( KeyEvent.VK_F );
        menuBar.add( menu );

        JMenuItem menuItem = new JMenuItem( "Export As PDF", KeyEvent.VK_E );
        menuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_E, ActionEvent.CTRL_MASK ) );
        menuItem.setActionCommand( "export" );
        menuItem.addActionListener( menuActionListener );
        menu.add( menuItem );

        menuItem = new JMenuItem( "Exit", KeyEvent.VK_X );
        menuItem.setActionCommand( "exit" );
        menuItem.addActionListener( menuActionListener );
        menu.add( menuItem );

        return menuBar;
    }

    protected void applicationModelPaneValueChanged()
    {
        Object obj = treeModelPane.getLastSelected();
        detailModelPane.setDescriptor( obj );
        if( !graphItemSelectionInProgress )
        {
            graphPane.setSelectedValue( obj );
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

    protected void exportAsPDF()
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            @Override
            public void run()
            {
                PDFWriter pdf = new PDFWriter();
                pdf.write( EnvisageFrame.this, descriptor, graphPane.getGraphDisplays() );
            }
        } );
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
     */
    private void $$$setupUI$$$()
    {
        contentPane = new JPanel();
        contentPane.setLayout( new BorderLayout( 0, 0 ) );
    }

    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }
}
