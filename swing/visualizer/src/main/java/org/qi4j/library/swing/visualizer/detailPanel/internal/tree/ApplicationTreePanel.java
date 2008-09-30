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
package org.qi4j.library.swing.visualizer.detailPanel.internal.tree;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.util.Enumeration;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.listener.SelectionListener;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class ApplicationTreePanel
    implements SelectionListener
{
    private JPanel treePanel;
    private JButton button1;
    private JTree applicationTree;

    private final TreeSelectionListener listener;
    private final TreeModelBuilder builder;

    public ApplicationTreePanel( SelectionListener aListener )
        throws IllegalArgumentException
    {
        validateNotNull( "aListener", aListener );
        listener = new ApplicationTreeSelectionListener( aListener );

        $$$setupUI$$$();
        builder = new TreeModelBuilder();
    }

    public final void onApplicationSelected( ApplicationDetailDescriptor aDescriptor )
    {
        // Update tree model
        if( !isApplicationDisplayed( aDescriptor ) )
        {
            DefaultMutableTreeNode root = builder.build( aDescriptor );
            applicationTree.setModel( new DefaultTreeModel( root ) );
        }

        // Update selection
        if( !isNodeSelected( aDescriptor ) )
        {
            TreeSelectionModel model = applicationTree.getSelectionModel();
            Object rootNode = applicationTree.getModel().getRoot();

            if( rootNode != null )
            {
                model.setSelectionPath( new TreePath( rootNode ) );
            }
        }
    }

    private boolean isApplicationDisplayed( ApplicationDetailDescriptor aDescriptor )
    {
        TreeModel model = applicationTree.getModel();

        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
        if( rootNode != null )
        {
            Object userObject = rootNode.getUserObject();
            if( userObject != null && userObject.equals( aDescriptor ) )
            {
                return true;
            }
        }

        return false;
    }

    private boolean isNodeSelected( Object aUserObject )
    {
        boolean isSelected = false;
        TreePath selectedPath = applicationTree.getSelectionPath();
        if( selectedPath != null )
        {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            Object userObject = selectedNode.getUserObject();
            if( userObject != null && userObject.equals( aUserObject ) )
            {
                isSelected = true;
            }
        }
        return isSelected;
    }

    public final void onLayerSelected( LayerDetailDescriptor aDescriptor )
    {
        if( isNodeSelected( aDescriptor ) )
        {
            return;
        }

        TreeModel model = applicationTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        Enumeration children = root.children();
        while( children.hasMoreElements() )
        {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            String nodeObject = (String) child.getUserObject();

            if( "layers".equals( nodeObject ) )
            {
                Enumeration layerNodes = child.children();
                while( layerNodes.hasMoreElements() )
                {
                    DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) layerNodes.nextElement();
                    LayerDetailDescriptor layer = (LayerDetailDescriptor) layerNode.getUserObject();
                    if( layer.equals( aDescriptor ) )
                    {
                        TreeSelectionModel selectionModel = applicationTree.getSelectionModel();
                        TreeNode[] path = layerNode.getPath();
                        selectionModel.setSelectionPath( new TreePath( path ) );

                        return;
                    }
                }
            }
        }
    }

    public final void onModuleSelected( ModuleDetailDescriptor aDescriptor )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public final void onCompositeSelected( CompositeDetailDescriptor aDescriptor )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public final void onEntitySelected( EntityDetailDescriptor aDescriptor )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public final void onServiceSelected( ServiceDetailDescriptor aDescriptor )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public final void onObjectSelected( ObjectDetailDescriptor aDescriptor )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void resetSelection()
    {
        TreeSelectionModel selectionModel = applicationTree.getSelectionModel();
        selectionModel.clearSelection();
    }

    private void createUIComponents()
    {
        applicationTree = new JTree();

        // Selection settings
        TreeSelectionModel selectionModel = applicationTree.getSelectionModel();
        selectionModel.setSelectionMode( SINGLE_TREE_SELECTION );
        selectionModel.addTreeSelectionListener( listener );

        // Cell renderer settings
        TreeCellRenderer treeCellRenderer = new DefaultTreeCellRenderer();
        applicationTree.setCellRenderer( treeCellRenderer );
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
        createUIComponents();
        treePanel = new JPanel();
        treePanel.setLayout( new FormLayout( "fill:d:grow", "center:25px:noGrow,center:d:grow" ) );
        final JToolBar toolBar1 = new JToolBar();
        CellConstraints cc = new CellConstraints();
        treePanel.add( toolBar1, cc.xy( 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT ) );
        button1 = new JButton();
        button1.setText( "Button" );
        toolBar1.add( button1 );
        final JScrollPane scrollPane1 = new JScrollPane();
        treePanel.add( scrollPane1, cc.xy( 1, 2, CellConstraints.FILL, CellConstraints.FILL ) );
        scrollPane1.setViewportView( applicationTree );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return treePanel;
    }


}
