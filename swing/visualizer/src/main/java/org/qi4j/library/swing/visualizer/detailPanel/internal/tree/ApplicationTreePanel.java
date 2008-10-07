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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
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
    private JButton collapseButton;
    private JTree applicationTree;
    private JPanel treePanel;

    public ApplicationTreePanel( SelectionListener aListener )
        throws IllegalArgumentException
    {
        validateNotNull( "aListener", aListener );

        $$$setupUI$$$();

        // Selection settings
        TreeSelectionModel selectionModel = applicationTree.getSelectionModel();
        selectionModel.setSelectionMode( SINGLE_TREE_SELECTION );
        selectionModel.addTreeSelectionListener( new ApplicationTreeSelectionListener( aListener ) );

        // Cell renderer settings
        TreeCellRenderer treeCellRenderer = new DefaultTreeCellRenderer();
        applicationTree.setCellRenderer( treeCellRenderer );

        collapseButton.addActionListener( new CollapseTreeActionListener() );
    }

    public final void onApplicationSelected( ApplicationDetailDescriptor aDescriptor )
    {
        // Update tree model
        if( !isApplicationDisplayed( aDescriptor ) )
        {
            TreeModelBuilder builder = new TreeModelBuilder();
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

        if( model != null )
        {
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
            if( rootNode != null )
            {
                Object userObject = rootNode.getUserObject();
                if( userObject != null && userObject.equals( aDescriptor ) )
                {
                    return true;
                }
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

    public final void onLayerSelected( LayerDetailDescriptor aLayerDescriptor )
    {
        if( isNodeSelected( aLayerDescriptor ) )
        {
            return;
        }

        DefaultMutableTreeNode layerNode = getLayerNode( aLayerDescriptor );
        selectNode( layerNode );
    }

    private void selectNode( DefaultMutableTreeNode aNode )
    {
        if( aNode != null )
        {
            TreeNode[] path = aNode.getPath();

            TreeSelectionModel selectionModel = applicationTree.getSelectionModel();
            selectionModel.setSelectionPath( new TreePath( path ) );
        }
    }

    private DefaultMutableTreeNode getLayerNode( LayerDetailDescriptor aDescriptor )
    {
        if( aDescriptor != null )
        {
            TreeModel model = applicationTree.getModel();
            if( model != null )
            {
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
                Enumeration children = root.children();
                while( children.hasMoreElements() )
                {
                    DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) children.nextElement();
                    LayerDetailDescriptor layer = (LayerDetailDescriptor) layerNode.getUserObject();
                    if( layer.equals( aDescriptor ) )
                    {
                        return layerNode;
                    }
                }
            }
        }

        return null;
    }

    public final void onModuleSelected( ModuleDetailDescriptor aModuleDescriptor )
    {
        if( isNodeSelected( aModuleDescriptor ) )
        {
            return;
        }

        DefaultMutableTreeNode moduleNode = getModuleNode( aModuleDescriptor );
        selectNode( moduleNode );
    }

    private DefaultMutableTreeNode getModuleNode( ModuleDetailDescriptor aModuleDescriptor )
    {
        if( aModuleDescriptor != null )
        {
            LayerDetailDescriptor layerDescriptor = aModuleDescriptor.layer();
            DefaultMutableTreeNode layerNode = getLayerNode( layerDescriptor );
            if( layerNode != null )
            {
                Enumeration children = layerNode.children();
                while( children.hasMoreElements() )
                {
                    DefaultMutableTreeNode moduleNode = (DefaultMutableTreeNode) children.nextElement();
                    ModuleDetailDescriptor module = (ModuleDetailDescriptor) moduleNode.getUserObject();
                    if( module.equals( aModuleDescriptor ) )
                    {
                        return moduleNode;
                    }
                }
            }
        }

        return null;
    }

    public final void onServiceSelected( ServiceDetailDescriptor aDescriptor )
    {
        ModuleDetailDescriptor module = aDescriptor.module();
        // TODO: Localization
        selectModuleChildNode( module, "services", aDescriptor );
    }

    private void selectModuleChildNode(
        ModuleDetailDescriptor aModuleDescriptor,
        String directModuleChildName,
        Object descriptor )
    {
        if( isNodeSelected( descriptor ) )
        {
            return;
        }

        DefaultMutableTreeNode moduleNode = getModuleNode( aModuleDescriptor );
        DefaultMutableTreeNode moduleChildNode = getModuleChildNode( moduleNode, directModuleChildName, descriptor );
        selectNode( moduleChildNode );
    }

    private DefaultMutableTreeNode getModuleChildNode(
        DefaultMutableTreeNode moduleNode, String searchTerm, Object aNodeUserObject
    )
    {
        DefaultMutableTreeNode directChildNode = getModuleDirectChildNode( moduleNode, searchTerm );
        if( directChildNode != null )
        {

            Enumeration children = directChildNode.children();
            while( children.hasMoreElements() )
            {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
                Object userObject = child.getUserObject();

                if( aNodeUserObject.equals( userObject ) )
                {
                    return child;
                }
            }
        }

        return null;
    }


    private DefaultMutableTreeNode getModuleDirectChildNode( DefaultMutableTreeNode aModuleNode, String aNodeName )
    {
        if( aModuleNode != null )
        {
            Enumeration children = aModuleNode.children();
            while( children.hasMoreElements() )
            {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
                String nodeObject = (String) child.getUserObject();
                if( aNodeName.equals( nodeObject ) )
                {
                    return child;
                }
            }
        }

        return null;
    }

    public final void onEntitySelected( EntityDetailDescriptor aDescriptor )
    {
        ModuleDetailDescriptor module = aDescriptor.module();
        // TODO: Localization
        selectModuleChildNode( module, "entities", aDescriptor );
    }

    public final void onCompositeSelected( CompositeDetailDescriptor aDescriptor )
    {
        ModuleDetailDescriptor module = aDescriptor.module();
        // TODO: Localization
        selectModuleChildNode( module, "composites", aDescriptor );
    }

    public final void onObjectSelected( ObjectDetailDescriptor aDescriptor )
    {
        ModuleDetailDescriptor module = aDescriptor.module();
        // TODO: Localization
        selectModuleChildNode( module, "objects", aDescriptor );
    }

    public void resetSelection()
    {
        TreeSelectionModel selectionModel = applicationTree.getSelectionModel();
        selectionModel.clearSelection();
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
        treePanel = new JPanel();
        treePanel.setLayout( new FormLayout( "fill:d:grow", "center:25px:noGrow,center:d:grow" ) );
        final JToolBar toolBar1 = new JToolBar();
        CellConstraints cc = new CellConstraints();
        treePanel.add( toolBar1, cc.xy( 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT ) );
        collapseButton = new JButton();
        collapseButton.setText( "Collapse" );
        toolBar1.add( collapseButton );
        final JScrollPane scrollPane1 = new JScrollPane();
        treePanel.add( scrollPane1, cc.xy( 1, 2, CellConstraints.FILL, CellConstraints.FILL ) );
        applicationTree = new JTree();
        scrollPane1.setViewportView( applicationTree );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return treePanel;
    }

    private class CollapseTreeActionListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            // Reset the model to force collapsing the tree
            TreeModel model = applicationTree.getModel();
            applicationTree.setModel( null );
            applicationTree.setModel( model );
        }
    }
}