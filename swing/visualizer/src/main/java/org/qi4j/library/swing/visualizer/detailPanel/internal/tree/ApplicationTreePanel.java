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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.tree.TreeModelBuilder.NODE_NAME_COMPOSITES;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.tree.TreeModelBuilder.NODE_NAME_CONSTRUCTORS;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.tree.TreeModelBuilder.NODE_NAME_ENTITIES;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.tree.TreeModelBuilder.NODE_NAME_INJECTED_FIELDS;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.tree.TreeModelBuilder.NODE_NAME_METHODS;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.tree.TreeModelBuilder.NODE_NAME_MIXINS;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.tree.TreeModelBuilder.NODE_NAME_OBJECTS;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.tree.TreeModelBuilder.NODE_NAME_SERVICES;
import org.qi4j.library.swing.visualizer.listener.SelectionListener;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeMethodDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ConstructorDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.InjectedFieldDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.MixinDetailDescriptor;
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
        Qi4jTreeCellRenderer treeCellRenderer = new Qi4jTreeCellRenderer();
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
        TreePath selectedPath = applicationTree.getSelectionPath();
        if( selectedPath == null )
        {
            return false;
        }

        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
        Object userObject = selectedNode.getUserObject();
        return userObject != null && userObject.equals( aUserObject );
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
        if( isNodeSelected( aDescriptor ) )
        {
            return;
        }

        ModuleDetailDescriptor module = aDescriptor.module();
        DefaultMutableTreeNode moduleNode = getModuleNode( module );
        DefaultMutableTreeNode serviceNode = getGrandChildrenChildNode( moduleNode, NODE_NAME_SERVICES, aDescriptor );
        selectNode( serviceNode );
    }

    private DefaultMutableTreeNode getGrandChildrenChildNode(
        DefaultMutableTreeNode aNode, Object aDirectChildrenUserObject, Object aGrandChildrenUserObject
    )
    {
        DefaultMutableTreeNode directChildNode = getDirectChildNodeWithUserObject( aNode, aDirectChildrenUserObject );
        return getDirectChildNodeWithUserObject( directChildNode, aGrandChildrenUserObject );
    }

    private DefaultMutableTreeNode getDirectChildNodeWithUserObject(
        DefaultMutableTreeNode aModuleNode, Object aUserObject )
    {
        if( aModuleNode != null )
        {
            Enumeration children = aModuleNode.children();
            while( children.hasMoreElements() )
            {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
                Object nodeObject = child.getUserObject();
                if( aUserObject.equals( nodeObject ) )
                {
                    return child;
                }
            }
        }

        return null;
    }

    public final void onEntitySelected( EntityDetailDescriptor aDescriptor )
    {
        if( isNodeSelected( aDescriptor ) )
        {
            return;
        }

        DefaultMutableTreeNode entityNode = getEntityNode( aDescriptor );
        selectNode( entityNode );
    }

    private DefaultMutableTreeNode getEntityNode( EntityDetailDescriptor anEntityDescriptor )
    {
        ModuleDetailDescriptor module = anEntityDescriptor.module();
        DefaultMutableTreeNode moduleNode = getModuleNode( module );
        return getGrandChildrenChildNode( moduleNode, NODE_NAME_ENTITIES, anEntityDescriptor );
    }

    public final void onCompositeSelected( CompositeDetailDescriptor aDescriptor )
    {
        if( isNodeSelected( aDescriptor ) )
        {
            return;
        }

        DefaultMutableTreeNode compositeNode = getCompositeNode( aDescriptor );
        selectNode( compositeNode );
    }

    private DefaultMutableTreeNode getCompositeNode( CompositeDetailDescriptor aCompositeDescriptor )
    {
        ModuleDetailDescriptor module = aCompositeDescriptor.module();
        DefaultMutableTreeNode moduleNode = getModuleNode( module );
        return getGrandChildrenChildNode( moduleNode, NODE_NAME_COMPOSITES, aCompositeDescriptor );
    }

    public final void onObjectSelected( ObjectDetailDescriptor aDescriptor )
    {
        if( isNodeSelected( aDescriptor ) )
        {
            return;
        }

        DefaultMutableTreeNode objectNode = getObjectNode( aDescriptor );
        selectNode( objectNode );
    }

    private DefaultMutableTreeNode getObjectNode( ObjectDetailDescriptor anObjectDescriptor )
    {
        ModuleDetailDescriptor module = anObjectDescriptor.module();
        DefaultMutableTreeNode moduleNode = getModuleNode( module );
        return getGrandChildrenChildNode( moduleNode, NODE_NAME_OBJECTS, anObjectDescriptor );
    }

    public final void onMixinSelected( MixinDetailDescriptor aDescriptor )
    {
        if( isNodeSelected( aDescriptor ) )
        {
            return;
        }

        CompositeDetailDescriptor composite = aDescriptor.composite();
        DefaultMutableTreeNode mixinNode = getMixinNode( aDescriptor, composite );
        selectNode( mixinNode );
    }

    private DefaultMutableTreeNode getMixinNode(
        MixinDetailDescriptor aMixinDescriptor, CompositeDetailDescriptor aCompositeDescriptor )
    {
        ModuleDetailDescriptor module = aCompositeDescriptor.module();
        DefaultMutableTreeNode moduleNode = getModuleNode( module );

        if( moduleNode != null )
        {
            DefaultMutableTreeNode compositeNode;
            if( aCompositeDescriptor instanceof EntityDetailDescriptor )
            {
                EntityDetailDescriptor entity = (EntityDetailDescriptor) aCompositeDescriptor;
                compositeNode = getEntityNode( entity );
            }
            else
            {
                compositeNode = getCompositeNode( aCompositeDescriptor );
            }

            return getGrandChildrenChildNode( compositeNode, NODE_NAME_MIXINS, aMixinDescriptor );
        }

        return null;
    }

    public final void onConstructorSelected( ConstructorDetailDescriptor aDescriptor )
    {
        if( isNodeSelected( aDescriptor ) )
        {
            return;
        }

        DefaultMutableTreeNode constructorNode = getConstructorNode( aDescriptor );
        selectNode( constructorNode );
    }

    private DefaultMutableTreeNode getConstructorNode( ConstructorDetailDescriptor aDescriptor )
    {
        DefaultMutableTreeNode mixinOrObjectNode = getMixinOrObjectNode( aDescriptor );
        DefaultMutableTreeNode constructorsNode =
            getDirectChildNodeWithUserObject( mixinOrObjectNode, NODE_NAME_CONSTRUCTORS );
        return getDirectChildNodeWithUserObject( constructorsNode, aDescriptor );
    }

    private DefaultMutableTreeNode getMixinOrObjectNode( ConstructorDetailDescriptor aDescriptor )
    {
        MixinDetailDescriptor mixin = aDescriptor.mixin();
        DefaultMutableTreeNode mixinOrObjectNode;
        if( mixin != null )
        {
            CompositeDetailDescriptor composite = mixin.composite();
            mixinOrObjectNode = getMixinNode( mixin, composite );
        }
        else
        {
            ObjectDetailDescriptor object = aDescriptor.object();
            mixinOrObjectNode = getObjectNode( object );
        }

        return mixinOrObjectNode;
    }

    public void onInjectedFieldSelected( InjectedFieldDetailDescriptor aDescriptor )
    {
        if( isNodeSelected( aDescriptor ) )
        {
            return;
        }

        DefaultMutableTreeNode fieldNode = getInjectedFieldNode( aDescriptor );
        selectNode( fieldNode );
    }

    private DefaultMutableTreeNode getInjectedFieldNode( InjectedFieldDetailDescriptor aDescriptor )
    {
        DefaultMutableTreeNode mixinOrObjectNode = getMixinOrObjectNode( aDescriptor );
        DefaultMutableTreeNode injectedFieldsNode = getDirectChildNodeWithUserObject(
            mixinOrObjectNode, NODE_NAME_INJECTED_FIELDS );
        return getDirectChildNodeWithUserObject( injectedFieldsNode, aDescriptor );
    }

    private DefaultMutableTreeNode getMixinOrObjectNode( InjectedFieldDetailDescriptor aDescriptor )
    {
        MixinDetailDescriptor mixin = aDescriptor.mixin();
        DefaultMutableTreeNode mixinOrObjectNode;
        if( mixin != null )
        {
            CompositeDetailDescriptor composite = mixin.composite();
            mixinOrObjectNode = getMixinNode( mixin, composite );
        }
        else
        {
            ObjectDetailDescriptor object = aDescriptor.object();
            mixinOrObjectNode = getObjectNode( object );
        }

        return mixinOrObjectNode;
    }

    public final void onCompositeMethodSelected( CompositeMethodDetailDescriptor aDescriptor )
    {
        if( isNodeSelected( aDescriptor ) )
        {
            return;
        }

        DefaultMutableTreeNode methodNode = getCompositeMethodNode( aDescriptor );
        selectNode( methodNode );
    }

    private DefaultMutableTreeNode getCompositeMethodNode( CompositeMethodDetailDescriptor aDescriptor )
    {
        CompositeDetailDescriptor composite = aDescriptor.composite();
        DefaultMutableTreeNode compositeNode = getCompositeNode( composite );
        return getGrandChildrenChildNode( compositeNode, NODE_NAME_METHODS, aDescriptor );
    }

    public final void resetSelection()
    {
        TreeSelectionModel selectionModel = applicationTree.getSelectionModel();
        if( !selectionModel.isSelectionEmpty() )
        {
            selectionModel.clearSelection();
        }
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

    private class CollapseTreeActionListener
        implements ActionListener
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