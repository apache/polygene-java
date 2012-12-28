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
package org.qi4j.envisage.tree;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import org.qi4j.tools.model.descriptor.ApplicationDetailDescriptor;

/**
 * Application Model View as Swing Component.
 * It support 2 view:<br/>
 * - by Structure<br/>
 * - by Type<br/>
 */
public class TreeModelPane
    extends JPanel
{
    protected static final String STRUCTURE_VIEW = "Structure";
    protected static final String TYPE_VIEW = "Type";

    protected ResourceBundle bundle = ResourceBundle.getBundle( this.getClass().getName() );

    protected JPanel mainPane;
    protected CardLayout cardLayout;
    protected JTree structureTree;
    protected JTree typeTree;
    protected JComboBox viewAsCombo;

    protected boolean selectionInProgress;

    protected ApplicationDetailDescriptor descriptor;

    public TreeModelPane()
    {
        setLayout( new BorderLayout() );

        // init mainPane
        structureTree = new JTree();
        structureTree.setRootVisible( false );
        structureTree.setShowsRootHandles( true );
        structureTree.setExpandsSelectedPaths( true );
        structureTree.setScrollsOnExpand( true );
        structureTree.setName( STRUCTURE_VIEW );
        structureTree.setCellRenderer( new TreeModelCellRenderer() );

        typeTree = new JTree();
        typeTree.setRootVisible( false );
        typeTree.setShowsRootHandles( true );
        typeTree.setExpandsSelectedPaths( true );
        typeTree.setScrollsOnExpand( true );
        typeTree.setName( TYPE_VIEW );
        typeTree.setCellRenderer( new TreeModelCellRenderer() );

        mainPane = new JPanel();
        cardLayout = new CardLayout();
        mainPane.setLayout( cardLayout );
        mainPane.add( new JScrollPane( structureTree ), STRUCTURE_VIEW );
        mainPane.add( new JScrollPane( typeTree ), TYPE_VIEW );
        add( mainPane, BorderLayout.CENTER );

        // init viewAsCombo
        JPanel viewAsPane = new JPanel();
        viewAsPane.setBorder( BorderFactory.createEmptyBorder( 3, 6, 3, 0 ) );
        viewAsPane.setLayout( new java.awt.GridBagLayout() );

        GridBagConstraints gridBagConstraints;
        JLabel viewAsLabel = new JLabel( bundle.getString( "CTL_ViewAs.Text" ) );
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets( 0, 0, 0, 6 );
        viewAsPane.add( viewAsLabel, gridBagConstraints );

        viewAsCombo = new JComboBox( new DefaultComboBoxModel( new String[]{ STRUCTURE_VIEW, TYPE_VIEW } ) );
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        viewAsPane.add( viewAsCombo, gridBagConstraints );

        viewAsCombo.addItemListener( new ItemListener()
        {
            @Override
            public void itemStateChanged( ItemEvent evt )
            {
                if( evt.getStateChange() == ItemEvent.DESELECTED )
                {
                    return;
                }
                cardLayout.show( mainPane, evt.getItem().toString() );
                repaint();
            }
        } );

        add( viewAsPane, BorderLayout.PAGE_START );
    }

    /**
     * Initialize Qi4J for this component
     *
     * @param descriptor the Application descriptor
     */
    public void initQi4J( ApplicationDetailDescriptor descriptor )
    {
        this.descriptor = descriptor;

        //Application Application = (Application) application;
        //ApplicationDetailDescriptor descriptor = ApplicationDetailDescriptorBuilder.createApplicationDetailDescriptor( Application );

        // traverse the model and build JTree representation
        MutableTreeNode rootNode1 = StructureModelBuilder.build( descriptor );
        MutableTreeNode rootNode2 = TypeModelBuilder.build( descriptor );

        structureTree.setModel( new DefaultTreeModel( rootNode1 ) );
        typeTree.setModel( new DefaultTreeModel( rootNode2 ) );

        structureTree.addTreeSelectionListener( new TreeSelectionListener()
        {
            @Override
            public void valueChanged( TreeSelectionEvent evt )
            {
                structureTreeValueChanged();
            }
        } );

        typeTree.addTreeSelectionListener( new TreeSelectionListener()
        {
            @Override
            public void valueChanged( TreeSelectionEvent evt )
            {
                typeTreeValueChanged();
            }
        } );
    }

    public Object getLastSelected()
    {
        Object obj = structureTree.getLastSelectedPathComponent();
        if( obj != null )
        {
            return ( (DefaultMutableTreeNode) obj ).getUserObject();
        }
        return null;
    }

    public void setSelectedValue( Object obj )
    {
        if( obj == null )
        {
            return;
        }

        //System.out.println(obj.toString());

        TreeNode node = findNode( structureTree, obj );
        if( node != null )
        {
            DefaultTreeModel treeModel = (DefaultTreeModel) structureTree.getModel();
            TreePath treePath = new TreePath( treeModel.getPathToRoot( node ) );
            structureTree.setSelectionPath( treePath );
            structureTree.scrollPathToVisible( treePath );
        }
        else
        {
            structureTree.clearSelection();
        }
    }

    /**
     * Just a helper method to find the node which contains the userObject
     *
     * @param tree   the JTree to search into
     * @param object the user object
     *
     * @return TreeNode or null
     */
    protected TreeNode findNode( JTree tree, Object object )
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
        return findNode( node, object );
    }

    /**
     * Recurvice search or find node that contains the obj
     *
     * @param node DefaultMutableTreeNode
     * @param obj  userObject
     *
     * @return TreeNode or null if could not find
     */
    private TreeNode findNode( DefaultMutableTreeNode node, Object obj )
    {
        if( obj instanceof String )
        {
            if( node.getUserObject().toString().equals( obj.toString() ) )
            {
                return node;
            }
        }
        else if( node.getUserObject().equals( obj ) )
        {
            return node;
        }

        TreeNode foundNode = null;
        for( int i = 0; i < node.getChildCount(); i++ )
        {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt( i );
            foundNode = findNode( childNode, obj );
            if( foundNode != null )
            {
                break;
            }
        }

        return foundNode;
    }

    public void addTreeSelectionListener( TreeSelectionListener listener )
    {
        structureTree.addTreeSelectionListener( listener );
    }

    public void removeTreeSelectionListener( TreeSelectionListener listener )
    {
        structureTree.removeTreeSelectionListener( listener );
    }

    protected void structureTreeValueChanged()
    {
        if( selectionInProgress )
        {
            return;
        }

        Object userObject = getLastSelected();
        if( userObject == null )
        {
            return;
        }
        TreeNode node = findNode( typeTree, userObject );
        if( node != null )
        {
            DefaultTreeModel treeModel = (DefaultTreeModel) typeTree.getModel();
            TreePath treePath = new TreePath( treeModel.getPathToRoot( node ) );
            typeTree.setSelectionPath( treePath );
            typeTree.scrollPathToVisible( treePath );
        }
    }

    protected void typeTreeValueChanged()
    {
        Object obj = typeTree.getLastSelectedPathComponent();
        if( obj == null )
        {
            return;
        }
        Object userObject = ( (DefaultMutableTreeNode) obj ).getUserObject();
        TreeNode node = findNode( structureTree, userObject );
        if( node != null )
        {
            DefaultTreeModel treeModel = (DefaultTreeModel) structureTree.getModel();
            TreePath treePath = new TreePath( treeModel.getPathToRoot( node ) );

            selectionInProgress = true;
            try
            {
                structureTree.setSelectionPath( treePath );
            }
            finally
            {
                selectionInProgress = false;
            }
            structureTree.scrollPathToVisible( treePath );
        }
    }
}
