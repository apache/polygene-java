/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
 * Copyright (c) 2008, Sonny Gill. All Rights Reserved.
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.library.framework.swing;

import org.qi4j.spi.structure.DescriptorVisitor;
import org.qi4j.spi.structure.LayerDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import static org.qi4j.library.framework.swing.GraphConstants.FIELD_USED_LAYERS;
import static org.qi4j.library.framework.swing.GraphConstants.FIELD_USED_BY_LAYERS;
import static org.qi4j.library.framework.swing.GraphConstants.FIELD_NAME;
import static org.qi4j.library.framework.swing.GraphConstants.FIELD_TYPE;
import static org.qi4j.library.framework.swing.GraphConstants.FIELD_LAYER_LEVEL;
import prefuse.data.Node;
import prefuse.data.Graph;
import prefuse.data.Edge;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Sonny Gill
 */
class ApplicationGraphVisitor extends DescriptorVisitor
{
    private final Graph graph;
    private final Node root;
    private Node layerNode;
    private Node moduleNode;
    private final Map<LayerDescriptor, Node> layerNodes;

    public ApplicationGraphVisitor( Graph graph )
    {
        this.layerNodes = new HashMap<LayerDescriptor, Node>();
        this.graph = graph;
        this.root = graph.addNode();
        root.setInt( FIELD_TYPE, ApplicationGraph.TYPE_APPLICATION );
    }

    @Override public void visit( ApplicationDescriptor applicationDescriptor )
    {
        root.setString( FIELD_NAME, applicationDescriptor.name() );
    }

    @Override public void visit( LayerDescriptor layerDescriptor )
    {
        layerNode = getLayerNode( layerDescriptor );

        Iterable<? extends LayerDescriptor> usedLayers = layerDescriptor.usedLayers().layers();
        for( LayerDescriptor usedLayerModel : usedLayers )
        {
            Node usedLayerNode = getLayerNode( usedLayerModel );
            addUsedLayer( layerNode, usedLayerNode );
            graph.addEdge( layerNode, usedLayerNode );
            incrementLayerLevel( usedLayerNode );
        }

        Edge edge = graph.addEdge( root, layerNode );
        edge.setInt( FIELD_TYPE, ApplicationGraph.TYPE_EDGE_HIDDEN );
    }

    private Node getLayerNode( LayerDescriptor layerDescriptor )
    {
        Node layer = layerNodes.get( layerDescriptor );
        if( layer == null )
        {
            layer = graph.addNode();
            String name = layerDescriptor.name();
            layer.setString( FIELD_NAME, name );
            layer.setInt( FIELD_TYPE, ApplicationGraph.TYPE_LAYER );
            layer.setInt( FIELD_LAYER_LEVEL, 1 );
            layer.set( FIELD_USED_LAYERS, new ArrayList() );
            layer.set( FIELD_USED_BY_LAYERS, new ArrayList() );
            layerNodes.put( layerDescriptor, layer );
        }

        return layer;
    }

    private void addUsedLayer( Node layer, Node usedLayer )
    {
        Collection<Node> usedLayers = (Collection<Node>) layer.get( FIELD_USED_LAYERS );
        usedLayers.add( usedLayer );
        Collection<Node> usedByLayers = (Collection<Node>) usedLayer.get( FIELD_USED_BY_LAYERS );
        usedByLayers.add( layer );
    }

    private void incrementLayerLevel( Node layer )
    {
        Collection<Node> usedLayers = (Collection<Node>) layer.get( FIELD_USED_LAYERS );
        for( Node usedLayer : usedLayers )
        {
            incrementLayerLevel( usedLayer );
        }

        int level = layer.getInt( FIELD_LAYER_LEVEL );
        layer.setInt( FIELD_LAYER_LEVEL, ++level );
    }

    @Override public void visit( ModuleDescriptor moduleDescriptor )
    {
        moduleNode = graph.addNode();
        moduleNode.setString( FIELD_NAME, moduleDescriptor.name() );
        moduleNode.setInt( FIELD_TYPE, ApplicationGraph.TYPE_MODULE );
        Edge edge = graph.addEdge( layerNode, moduleNode );
        edge.setInt( FIELD_TYPE, ApplicationGraph.TYPE_EDGE_HIDDEN );
    }

    public void visit( CompositeDescriptor compositeDescriptor )
    {
        Node node = graph.addNode();
        node.setString( FIELD_NAME, compositeDescriptor.type().getSimpleName() );
        node.setInt( FIELD_TYPE, ApplicationGraph.TYPE_COMPOSITE );
        Edge edge = graph.addEdge( moduleNode, node );
        edge.setInt( FIELD_TYPE, ApplicationGraph.TYPE_EDGE_HIDDEN );
    }

}
