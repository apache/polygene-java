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
package org.qi4j.library.swing.envisage.graph;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tree;
import prefuse.data.Table;
import org.qi4j.library.swing.envisage.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ObjectDetailDescriptor;

/**
 * Build Qi4J application model as Prefuse Tree Graph
 *
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class GraphBuilder
{
    private Tree tree = null;

    public static Graph buildGraph( ApplicationDetailDescriptor descriptor )
    {
        GraphBuilder builder = new GraphBuilder();
        return builder.buildApplicationNode( descriptor );
    }

    private GraphBuilder()
    {
        tree = new Tree();
        Table nodeTable = tree.getNodeTable();
        nodeTable.addColumn(GraphDisplay.NAME_LABEL, String.class);
        nodeTable.addColumn(GraphDisplay.USER_OBJECT, Object.class);
    }

    private Graph buildApplicationNode( ApplicationDetailDescriptor descriptor )
    {
        Node node = tree.addRoot();
        node.set(GraphDisplay.NAME_LABEL, descriptor.descriptor().name());
        node.set(GraphDisplay.USER_OBJECT, descriptor);

        buildLayersNode( node, descriptor.layers() );

        return tree;
    }


    private void buildLayersNode( Node parent, Iterable<LayerDetailDescriptor> iter )
    {
        for( LayerDetailDescriptor descriptor : iter )
        {
            Node childNode = tree.addChild( parent );
            childNode.set(GraphDisplay.NAME_LABEL, descriptor.descriptor().name());
            childNode.set(GraphDisplay.USER_OBJECT, descriptor );
            buildModulesNode( childNode, descriptor.modules() );
        }
    }

    private void buildModulesNode( Node parent, Iterable<ModuleDetailDescriptor> iter )
    {
        for( ModuleDetailDescriptor descriptor : iter )
        {
            Node childNode = tree.addChild( parent );
            childNode.set(GraphDisplay.NAME_LABEL, descriptor.descriptor().name());
            childNode.set(GraphDisplay.USER_OBJECT, descriptor );
            buildServicesNode( childNode, descriptor.services() );
            buildEntitiesNode( childNode, descriptor.entities() );
            buildObjectsNode( childNode, descriptor.objects() );
        }
    }

    private void buildServicesNode( Node parent, Iterable<ServiceDetailDescriptor> iter )
    {
        for( ServiceDetailDescriptor descriptor : iter )
        {
            Node childNode = tree.addChild( parent );
            childNode.set(GraphDisplay.NAME_LABEL, descriptor.descriptor().identity());
            childNode.set(GraphDisplay.USER_OBJECT, descriptor );
        }
    }

    private void buildEntitiesNode( Node parent, Iterable<EntityDetailDescriptor> iter )
    {
        for( EntityDetailDescriptor descriptor : iter )
        {
            Node childNode = tree.addChild( parent );
            childNode.set(GraphDisplay.NAME_LABEL, descriptor.descriptor().type().getSimpleName());
            childNode.set(GraphDisplay.USER_OBJECT, descriptor );
        }
    }

    private void buildObjectsNode( Node parent, Iterable<ObjectDetailDescriptor> iter )
    {
        for( ObjectDetailDescriptor descriptor : iter )
        {
            Node childNode = tree.addChild( parent );
            childNode.set(GraphDisplay.NAME_LABEL, descriptor.descriptor().type().getSimpleName());
            childNode.set(GraphDisplay.USER_OBJECT, descriptor );
        }
    }
}
