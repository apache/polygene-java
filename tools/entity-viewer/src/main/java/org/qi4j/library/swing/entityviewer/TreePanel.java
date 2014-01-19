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
package org.qi4j.library.swing.entityviewer;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.tools.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.tools.model.descriptor.ApplicationDetailDescriptorBuilder;
import org.qi4j.tools.model.descriptor.EntityDetailDescriptor;
import org.qi4j.tools.model.descriptor.LayerDetailDescriptor;
import org.qi4j.tools.model.descriptor.ModuleDetailDescriptor;

/**
 * Qi4J Application Tree Viewer as Swing Component.
 */
public class TreePanel
    extends JPanel
{
    protected JTree applicationTree;
    protected Qi4jSPI qi4j;
    private ApplicationDescriptor model;

    public TreePanel()
    {
        this.setLayout( new BorderLayout() );

        this.applicationTree = new JTree();
        this.applicationTree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );

        JScrollPane scrollPane = new JScrollPane();
        this.add( scrollPane, BorderLayout.CENTER );
        scrollPane.setViewportView( this.applicationTree );
    }

    public void initializeQi4J( Qi4jSPI qi4j, ApplicationDescriptor model )
    {
        this.qi4j = qi4j;
        this.model = model;
    }

    public JTree getTreeComponent()
    {
        return applicationTree;
    }

    public void reload()
    {
        // build the visitor which allow traverse qi4j model
        ApplicationDetailDescriptor visitor = ApplicationDetailDescriptorBuilder.createApplicationDetailDescriptor( model );
        DefaultTreeModel treeModel = new DefaultTreeModel( new TreeModelBuilder().build( visitor ) );
        applicationTree.setModel( treeModel );
    }

    /**
     * Helper class to build tree model for up to 3 level depth, which focusing on the entity
     */
    class TreeModelBuilder
    {

        public MutableTreeNode build( ApplicationDetailDescriptor descriptor )
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode( descriptor );
            buildLayersNode( node, descriptor.layers() );
            return node;
        }

        private void buildLayersNode( DefaultMutableTreeNode parent, Iterable<LayerDetailDescriptor> iter )
        {
            for( LayerDetailDescriptor descriptor : iter )
            {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode( descriptor );
                buildModulesNode( node, descriptor.modules() );
                parent.add( node );
            }
        }

        private void buildModulesNode( DefaultMutableTreeNode parent, Iterable<ModuleDetailDescriptor> iter )
        {
            for( ModuleDetailDescriptor descriptor : iter )
            {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode( descriptor );
                buildEntitiesNode( node, descriptor.entities() );
                parent.add( node );
            }
        }

        private void buildEntitiesNode( DefaultMutableTreeNode parent, Iterable<EntityDetailDescriptor> iter )
        {
            for( EntityDetailDescriptor descriptor : iter )
            {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode( descriptor );
                parent.add( node );
            }
        }
    }

}
