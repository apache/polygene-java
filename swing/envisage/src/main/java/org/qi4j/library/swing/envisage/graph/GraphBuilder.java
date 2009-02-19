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

/**
 * Build Qi4J application model as Prefuse Tree Graph
 *
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class GraphBuilder
{
    private static final String NAME   = "name";

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
        nodeTable.addColumn(NAME, String.class);
    }

    private Graph buildApplicationNode( ApplicationDetailDescriptor descriptor )
    {
        Node node = tree.addRoot();
        node.set(NAME, descriptor.descriptor().name());

        buildLayersNode( node, descriptor.layers() );

        return tree;
    }


    private void buildLayersNode( Node parent, Iterable<LayerDetailDescriptor> iter )
    {
        for( LayerDetailDescriptor descriptor : iter )
        {
            Node childNode = tree.addChild( parent );
            childNode.set(NAME, descriptor.descriptor().name());

            //System.out.println("Layer: " + descriptor.descriptor().name());

            buildModulesNode( childNode, descriptor.modules() );
        }
    }

    private void buildModulesNode( Node parent, Iterable<ModuleDetailDescriptor> iter )
    {
        for( ModuleDetailDescriptor descriptor : iter )
        {
            Node childNode = tree.addChild( parent );
            childNode.set(NAME, descriptor.descriptor().name());

            //System.out.println("...mod: " + descriptor.descriptor().name());

            buildServicesNode( childNode, descriptor.services() );

            /*

            buildEntitiesNode( node, descriptor.entities() );
            buildObjectsNode( node, descriptor.objects() );
            */
        }
    }

    private void buildServicesNode( Node parent, Iterable<ServiceDetailDescriptor> iter )
    {
        for( ServiceDetailDescriptor descriptor : iter )
        {
            Node childNode = tree.addChild( parent );
            childNode.set(NAME, descriptor.descriptor().identity());

            //System.out.println("... ... service: " + descriptor.descriptor().identity());
        }
    }
}
