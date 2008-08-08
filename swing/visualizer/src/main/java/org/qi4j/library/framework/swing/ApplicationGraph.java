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
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JTextPane;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import org.qi4j.structure.Application;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.composite.CompositeDescriptor;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.controls.ControlAdapter;
import prefuse.visual.VisualItem;

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
    private ApplicationGraphVisitor appGraphVisitor;
    private JSplitPane leftPane;
    private JSplitPane mainPane;

    public void show( Application application )
    {
        JFrame frame = new JFrame( "Qi4j Application Graph" );

        Graph graph = new Graph( true );
        appGraphVisitor = new ApplicationGraphVisitor( graph );
        ( (ApplicationSPI) application ).visitDescriptor( appGraphVisitor );
        ApplicationPanel applicationPanel = new ApplicationPanel( graph, new CompositeSelectionControl() );

        JPanel detailsPanel = new JPanel();
        detailsPanel.setBackground( Color.white );
        detailsPanel.add( new JLabel( "Details go here" ) );
        detailsPanel.setPreferredSize( new Dimension( 400, 800 ) );

        leftPane = createLeftPane( applicationPanel );
        mainPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
                                   leftPane, new JScrollPane( detailsPanel ) );

        frame.add( mainPane );

        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.pack();
        frame.setVisible( true );

        applicationPanel.graphShown();

    }

    private JSplitPane createLeftPane( JPanel applicationPanel )
    {
        applicationPanel.setMinimumSize( new Dimension( 400, 400 ) );
        JPanel panel = new JPanel();
        panel.setBackground( Color.white );
        return new JSplitPane( JSplitPane.VERTICAL_SPLIT, applicationPanel, panel );
    }

    private class CompositeSelectionControl extends ControlAdapter
    {

        public void itemClicked( VisualItem visualItem, MouseEvent event )
        {
            if( GraphUtils.isComposite( visualItem ) )
            {
                Node node = (Node) visualItem.getSourceTuple();
                CompositeDescriptor descriptor = appGraphVisitor.getCompositeDescriptor( node );
                if( descriptor != null )
                {
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode( descriptor.type().getSimpleName() );
                    Iterable<Class> mixinTypes = descriptor.mixinTypes();
                    for( Class mixinType : mixinTypes )
                    {
                        DefaultMutableTreeNode mixinNode = new DefaultMutableTreeNode( mixinType.getSimpleName() );
                        root.add( mixinNode );
                        Method[] methods = mixinType.getMethods();
                        for( Method method : methods )
                        {
                            DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode( method );
                            mixinNode.add( methodNode );
                        }
                    }

                    final JTree tree = new JTree( root );
                    tree.addTreeSelectionListener( new TreeSelectionListener()
                    {
                        public void valueChanged( TreeSelectionEvent e )
                        {
                            Object object = ( (DefaultMutableTreeNode) tree.getLastSelectedPathComponent() ).getUserObject();
                            if( object instanceof Method )
                            {
                                Method m = (Method) object;
                                Map<String, List> map = appGraphVisitor.getMethodAttributes( m );

                                StringBuilder buf = new StringBuilder();

                                List constraints = map.get( "constraints" );
                                buf.append( "Constraints\n" );
                                for( Object constraint : constraints )
                                {
                                    buf.append( constraint.toString() ).append( "\n" );
                                }

                                List concerns = map.get( "concerns" );
                                buf.append( "\n\nConcerns\n" );
                                for( Object concern : concerns )
                                {
                                    buf.append( concern.toString() ).append( "\n" );
                                }

                                List sideEffects = map.get( "sideEffects" );
                                buf.append( "\n\nSide Effects\n" );
                                for( Object sideEffect : sideEffects )
                                {
                                    buf.append( sideEffect.toString() ).append( "\n" );
                                }

//                                JLabel label = new JLabel( buf.toString() );
                                JTextPane textPane = new JTextPane();
                                textPane.setText( buf.toString() );
                                JPanel panel = new JPanel();
                                panel.setBackground( Color.white );
                                panel.add( textPane );

                                mainPane.setRightComponent( panel );
                            }
                        }
                    } );

                    leftPane.setRightComponent( new JScrollPane( tree ) );
                }
            }
        }
    }

}
