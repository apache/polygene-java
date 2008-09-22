/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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
package org.qi4j.library.swing.visualizer.overview.descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_DESCRIPTOR;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_LAYER_LEVEL;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_NAME;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_TYPE;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_USED_BY_LAYERS;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_USED_LAYERS;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.APPLICATION;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.COMPOSITE;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.EDGE_HIDDEN;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.ENTITY;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.GROUP;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.LAYER;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.MODULE;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.OBJECT;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.SERVICE;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.CompositeMethodDescriptor;
import org.qi4j.spi.composite.MethodConcernDescriptor;
import org.qi4j.spi.composite.MethodConstraintsDescriptor;
import org.qi4j.spi.composite.MethodSideEffectDescriptor;
import org.qi4j.spi.composite.MixinDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.structure.DescriptorVisitor;
import org.qi4j.spi.structure.LayerDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;
import org.qi4j.spi.structure.UsedLayersDescriptor;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * TODO: Makes this internal
 *
 * @author Sonny Gill
 */
public final class ApplicationGraphVisitor extends DescriptorVisitor
{
    private final Graph graph;

    // Root node
    private final Node root;

    // Layer node
    private Node currLayerNode;
    private LayerDetailDescriptor currLayerDescriptor;

    // Module related temp variables
    private Node moduleNode;
    private ModuleDetailDescriptor currModuleDescriptor;
    private Node servicesNode;
    private Node entitiesNode;
    private Node objectsNode;

    // Temp application descriptor
    private ApplicationDetailDescriptor currApplicationDescriptor;

    // Cache to lookup layer descriptor -> node
    private final Map<LayerDescriptor, Node> layerDescriptorToNodeMap = new HashMap<LayerDescriptor, Node>();

    // Cache of current composite
    private Node currCompositeNode;
    private CompositeDetailDescriptor currCompositeDescriptor;

    // Cache of current composite method descriptor
    private CompositeMethodDetailDescriptor currMethodDesciptor;

    public ApplicationGraphVisitor( Graph aGraph )
    {
        graph = aGraph;

        aGraph.addColumn( FIELD_NAME, String.class );
        aGraph.addColumn( FIELD_TYPE, GraphConstants.NodeType.class );
        aGraph.addColumn( FIELD_LAYER_LEVEL, int.class );
        aGraph.addColumn( FIELD_USED_LAYERS, Collection.class );
        aGraph.addColumn( FIELD_USED_BY_LAYERS, Collection.class );
        aGraph.addColumn( FIELD_DESCRIPTOR, Object.class );

        root = aGraph.addNode();
    }

    @Override
    public void visit( ApplicationDescriptor aDescriptor )
    {
        root.set( FIELD_TYPE, APPLICATION );
        root.setString( FIELD_NAME, aDescriptor.name() );

        currApplicationDescriptor = new ApplicationDetailDescriptor( aDescriptor );
        root.set( FIELD_DESCRIPTOR, currApplicationDescriptor );
    }

    @Override
    public void visit( LayerDescriptor layerDescriptor )
    {
        currLayerNode = getLayerNode( layerDescriptor );
        currLayerDescriptor = (LayerDetailDescriptor) currLayerNode.get( FIELD_DESCRIPTOR );
        currApplicationDescriptor.addLayer( currLayerDescriptor );

        UsedLayersDescriptor usedLayesDescriptor = layerDescriptor.usedLayers();
        Iterable<? extends LayerDescriptor> usedLayers = usedLayesDescriptor.layers();
        for( LayerDescriptor usedLayer : usedLayers )
        {
            Node usedLayerNode = getLayerNode( usedLayer );

            LayerDetailDescriptor usedLayerDetailDesc = (LayerDetailDescriptor) usedLayerNode.get( FIELD_DESCRIPTOR );
            currLayerDescriptor.addUsedLayer( usedLayerDetailDesc );

            addUsedLayer( currLayerNode, usedLayerNode );
            graph.addEdge( currLayerNode, usedLayerNode );
            incrementLayerLevel( usedLayerNode );
        }

        addHiddenEdge( root, currLayerNode );
    }

    private Node getLayerNode( LayerDescriptor aDescriptor )
    {
        Node layer = layerDescriptorToNodeMap.get( aDescriptor );
        if( layer == null )
        {
            layer = graph.addNode();
            String name = aDescriptor.name();
            layer.setString( FIELD_NAME, name );

            LayerDetailDescriptor descriptor = new LayerDetailDescriptor( aDescriptor );
            layer.set( FIELD_DESCRIPTOR, descriptor );

            layer.set( FIELD_TYPE, LAYER );
            layer.setInt( FIELD_LAYER_LEVEL, 1 );
            layer.set( FIELD_USED_LAYERS, new ArrayList<Node>() );
            layer.set( FIELD_USED_BY_LAYERS, new ArrayList<Node>() );
            layerDescriptorToNodeMap.put( aDescriptor, layer );
        }

        return layer;
    }

    @SuppressWarnings( "unchecked" )
    private void addUsedLayer( Node layer, Node usedLayer )
    {
        Collection<Node> usedLayers = (Collection<Node>) layer.get( FIELD_USED_LAYERS );
        usedLayers.add( usedLayer );

        Collection<Node> usedByLayers = (Collection<Node>) usedLayer.get( FIELD_USED_BY_LAYERS );
        usedByLayers.add( layer );
    }

    @SuppressWarnings( "unchecked" )
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

    @Override
    public void visit( ModuleDescriptor aDescriptor )
    {
        moduleNode = graph.addNode();
        moduleNode.setString( FIELD_NAME, aDescriptor.name() );
        moduleNode.set( FIELD_TYPE, MODULE );

        currModuleDescriptor = new ModuleDetailDescriptor( aDescriptor );
        moduleNode.set( FIELD_DESCRIPTOR, currModuleDescriptor );
        currLayerDescriptor.addModule( currModuleDescriptor );

        addHiddenEdge( currLayerNode, moduleNode );

        // Reset module related temp variables
        servicesNode = null;
        entitiesNode = null;
        currCompositeNode = null;
        objectsNode = null;
    }

    @Override
    public final void visit( ServiceDescriptor aDescriptor )
    {
        if( servicesNode == null )
        {
            servicesNode = graph.addNode();
            servicesNode.setString( FIELD_NAME, "Services" );
            servicesNode.set( FIELD_TYPE, GROUP );
            addHiddenEdge( moduleNode, servicesNode );
        }

        Node node = graph.addNode();

        Class<?> serviceClass = aDescriptor.type();
        node.setString( FIELD_NAME, serviceClass.getSimpleName() );
        node.set( FIELD_TYPE, SERVICE );

        currModuleDescriptor.addService( aDescriptor );
        node.set( FIELD_DESCRIPTOR, aDescriptor );
        addHiddenEdge( servicesNode, node );
    }

    @Override
    public final void visit( EntityDescriptor aDescriptor )
    {
        if( entitiesNode == null )
        {
            entitiesNode = graph.addNode();
            entitiesNode.setString( FIELD_NAME, "Entities" );
            entitiesNode.set( FIELD_TYPE, GROUP );
            addHiddenEdge( moduleNode, entitiesNode );
        }

        Node node = graph.addNode();
        node.setString( FIELD_NAME, aDescriptor.type().getSimpleName() );
        node.set( FIELD_TYPE, ENTITY );

        EntityDetailDescriptor descriptor = new EntityDetailDescriptor( aDescriptor );
        currModuleDescriptor.addEntity( descriptor );
        currCompositeDescriptor = descriptor;
        node.set( FIELD_DESCRIPTOR, currCompositeDescriptor );

        addHiddenEdge( entitiesNode, node );
    }

    @Override
    public final void visit( CompositeDescriptor aDescriptor )
    {
        if( currCompositeNode == null )
        {
            currCompositeNode = graph.addNode();
            currCompositeNode.setString( FIELD_NAME, "Composites" );
            currCompositeNode.set( FIELD_TYPE, GROUP );
            addHiddenEdge( moduleNode, currCompositeNode );
        }

        Node node = graph.addNode();
        node.setString( FIELD_NAME, aDescriptor.type().getSimpleName() );
        node.set( FIELD_TYPE, COMPOSITE );

        currCompositeDescriptor = new CompositeDetailDescriptor<CompositeDescriptor>( aDescriptor );
        currModuleDescriptor.addComposite( currCompositeDescriptor );
        node.set( FIELD_DESCRIPTOR, currCompositeDescriptor );
        addHiddenEdge( currCompositeNode, node );
    }

    @Override
    public final void visit( CompositeMethodDescriptor aDescriptor )
    {
        currMethodDesciptor = new CompositeMethodDetailDescriptor( aDescriptor );
        currCompositeDescriptor.addMethod( currMethodDesciptor );
    }

    @Override
    public final void visit( MethodConstraintsDescriptor methodConstraintsDescriptor )
    {
        currMethodDesciptor.addConstraint( methodConstraintsDescriptor );
    }

    @Override
    public final void visit( MethodConcernDescriptor methodConcernDescriptor )
    {
        currMethodDesciptor.addConcern( methodConcernDescriptor );
    }

    @Override
    public final void visit( MethodSideEffectDescriptor methodSideEffectDescriptor )
    {
        currMethodDesciptor.addSideEffect( methodSideEffectDescriptor );
    }

    @Override
    public final void visit( MixinDescriptor mixinDescriptor )
    {
        currCompositeDescriptor.addMixin( mixinDescriptor );
    }

    @Override
    public final void visit( ObjectDescriptor aDescriptor )
    {
        if( objectsNode == null )
        {
            System.out.println( "Creating objects node. Descriptor - " + aDescriptor.toURI() );
            objectsNode = graph.addNode();
            objectsNode.setString( FIELD_NAME, "Objects" );
            objectsNode.set( FIELD_TYPE, GROUP );
            addHiddenEdge( moduleNode, objectsNode );
        }

        Node node = graph.addNode();
        node.setString( FIELD_NAME, aDescriptor.type().getSimpleName() );
        node.set( FIELD_TYPE, OBJECT );
        node.set( FIELD_DESCRIPTOR, aDescriptor );
        currModuleDescriptor.addObject( aDescriptor );
        addHiddenEdge( objectsNode, node );
    }

    private void addHiddenEdge( Node source, Node target )
    {
        Edge edge = graph.addEdge( source, target );
        edge.set( FIELD_TYPE, EDGE_HIDDEN );
    }
}
