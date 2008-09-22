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
package org.qi4j.library.swing.visualizer.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.qi4j.library.swing.visualizer.common.GraphConstants.FIELD_LAYER_LEVEL;
import static org.qi4j.library.swing.visualizer.common.GraphConstants.FIELD_NAME;
import static org.qi4j.library.swing.visualizer.common.GraphConstants.FIELD_TYPE;
import static org.qi4j.library.swing.visualizer.common.GraphConstants.FIELD_USED_BY_LAYERS;
import static org.qi4j.library.swing.visualizer.common.GraphConstants.FIELD_USED_LAYERS;
import static org.qi4j.library.swing.visualizer.common.GraphConstants.NodeType.APPLICATION;
import static org.qi4j.library.swing.visualizer.common.GraphConstants.NodeType.COMPOSITE;
import static org.qi4j.library.swing.visualizer.common.GraphConstants.NodeType.EDGE_HIDDEN;
import static org.qi4j.library.swing.visualizer.common.GraphConstants.NodeType.GROUP;
import static org.qi4j.library.swing.visualizer.common.GraphConstants.NodeType.LAYER;
import static org.qi4j.library.swing.visualizer.common.GraphConstants.NodeType.MODULE;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.CompositeMethodDescriptor;
import org.qi4j.spi.composite.ConstraintDescriptor;
import org.qi4j.spi.composite.MethodConcernDescriptor;
import org.qi4j.spi.composite.MethodConstraintsDescriptor;
import org.qi4j.spi.composite.MethodSideEffectDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.structure.DescriptorVisitor;
import org.qi4j.spi.structure.LayerDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * @author Sonny Gill
 */
public class ApplicationGraphVisitor extends DescriptorVisitor
{
    private final Graph graph;

    // Root node
    private final Node root;

    // Layer node
    private Node layerNode;

    // Module related temp variables
    private Node moduleNode;
    private Node servicesNode;
    private Node entitiesNode;
    private Node compositesNode;
    private Node objectsNode;

    // Cache to lookup layer descriptor -> node
    private final Map<LayerDescriptor, Node> layerDescriptorToNodeMap = new HashMap<LayerDescriptor, Node>();

    // Cache to lookup node -> descriptor
    private final Map<Node, Object> descriptorsMap = new HashMap<Node, Object>();

    private Map<Method, Map<String, List<Class>>> methodAttributesMap = new HashMap<Method, Map<String, List<Class>>>();
    private Method currentMethod;
    private Map<String, List<Class>> currentMethodAttributesMap;

    public ApplicationGraphVisitor( Graph graph )
    {
        this.graph = graph;

        graph.addColumn( FIELD_NAME, String.class );
        graph.addColumn( FIELD_TYPE, int.class );
        graph.addColumn( FIELD_LAYER_LEVEL, int.class );
        graph.addColumn( FIELD_USED_LAYERS, Collection.class );
        graph.addColumn( FIELD_USED_BY_LAYERS, Collection.class );

        root = graph.addNode();
        root.setInt( FIELD_TYPE, APPLICATION.code() );
    }

    @Override
    public void visit( ApplicationDescriptor applicationDescriptor )
    {
        root.setString( FIELD_NAME, applicationDescriptor.name() );
    }

    @Override
    public void visit( LayerDescriptor layerDescriptor )
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

        addHiddenEdge( root, layerNode );
    }

    private Node getLayerNode( LayerDescriptor layerDescriptor )
    {
        Node layer = layerDescriptorToNodeMap.get( layerDescriptor );
        if( layer == null )
        {
            layer = graph.addNode();
            String name = layerDescriptor.name();
            layer.setString( FIELD_NAME, name );
            layer.setInt( FIELD_TYPE, LAYER.code() );
            layer.setInt( FIELD_LAYER_LEVEL, 1 );
            layer.set( FIELD_USED_LAYERS, new ArrayList<Node>() );
            layer.set( FIELD_USED_BY_LAYERS, new ArrayList<Node>() );
            layerDescriptorToNodeMap.put( layerDescriptor, layer );
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
    public void visit( ModuleDescriptor moduleDescriptor )
    {
        moduleNode = graph.addNode();
        moduleNode.setString( FIELD_NAME, moduleDescriptor.name() );
        moduleNode.setInt( FIELD_TYPE, MODULE.code() );

        addHiddenEdge( layerNode, moduleNode );

        // Reset module related temp variables
        servicesNode = null;
        entitiesNode = null;
        compositesNode = null;
        objectsNode = null;
    }

    public void visit( ServiceDescriptor serviceDescriptor )
    {
        if( servicesNode == null )
        {
            servicesNode = graph.addNode();
            servicesNode.setString( FIELD_NAME, "Services" );
            servicesNode.setInt( FIELD_TYPE, GROUP.code() );
            addHiddenEdge( moduleNode, servicesNode );
        }

        Node node = graph.addNode();
        node.setString( FIELD_NAME, GraphUtils.getCompositeName( serviceDescriptor.type() ) );
        node.setInt( FIELD_TYPE, COMPOSITE.code() );
        addHiddenEdge( servicesNode, node );

        descriptorsMap.put( node, serviceDescriptor );
    }

    public void visit( EntityDescriptor entityDescriptor )
    {
        if( entitiesNode == null )
        {
            entitiesNode = graph.addNode();
            entitiesNode.setString( FIELD_NAME, "Entities" );
            entitiesNode.setInt( FIELD_TYPE, GROUP.code() );
            addHiddenEdge( moduleNode, entitiesNode );
        }

        Node node = graph.addNode();
        node.setString( FIELD_NAME, GraphUtils.getCompositeName( entityDescriptor.type() ) );
        node.setInt( FIELD_TYPE, COMPOSITE.code() );
        addHiddenEdge( entitiesNode, node );

        descriptorsMap.put( node, entityDescriptor );
    }

    public void visit( CompositeDescriptor compositeDescriptor )
    {
        if( compositesNode == null )
        {
            compositesNode = graph.addNode();
            compositesNode.setString( FIELD_NAME, "Composites" );
            compositesNode.setInt( FIELD_TYPE, GROUP.code() );
            addHiddenEdge( moduleNode, compositesNode );
        }

        Node node = graph.addNode();
        node.setString( FIELD_NAME, GraphUtils.getCompositeName( compositeDescriptor.type() ) );
        node.setInt( FIELD_TYPE, COMPOSITE.code() );
        addHiddenEdge( compositesNode, node );

        descriptorsMap.put( node, compositeDescriptor );
    }

    public void visit( ObjectDescriptor objectDescriptor )
    {
        if( objectsNode == null )
        {
            System.out.println( "Creating objects node. Descriptor - " + objectDescriptor.toURI() );
            objectsNode = graph.addNode();
            objectsNode.setString( FIELD_NAME, "Objects" );
            objectsNode.setInt( FIELD_TYPE, GROUP.code() );
            addHiddenEdge( moduleNode, objectsNode );
        }

        Node node = graph.addNode();
        node.setString( FIELD_NAME, GraphUtils.getCompositeName( objectDescriptor.type() ) );
        node.setInt( FIELD_TYPE, COMPOSITE.code() );
        addHiddenEdge( objectsNode, node );

        descriptorsMap.put( node, objectDescriptor );
    }


    public void visit( CompositeMethodDescriptor compositeMethodDescriptor )
    {
        currentMethod = compositeMethodDescriptor.method();
        currentMethodAttributesMap = new HashMap<String, List<Class>>();

        currentMethodAttributesMap.put( "constraints", new ArrayList<Class>() );
        currentMethodAttributesMap.put( "concerns", new ArrayList<Class>() );
        currentMethodAttributesMap.put( "sideEffects", new ArrayList<Class>() );
        currentMethodAttributesMap.put( "mixins", Collections.singletonList( compositeMethodDescriptor.mixin().mixinClass() ) );

        methodAttributesMap.put( currentMethod, currentMethodAttributesMap );
    }

    public void visit( ConstraintDescriptor constraintDescriptor )
    {
        super.visit( constraintDescriptor );
    }

    public void visit( MethodConstraintsDescriptor methodConstraintsDescriptor )
    {
        List list = currentMethodAttributesMap.get( "constraints" );
        list.add( methodConstraintsDescriptor.hashCode() ); // todo
    }

    public void visit( MethodConcernDescriptor methodConcernDescriptor )
    {
        List<Class> list = currentMethodAttributesMap.get( "concerns" );
        list.add( methodConcernDescriptor.modifierClass() );
    }

    public void visit( MethodSideEffectDescriptor methodSideEffectDescriptor )
    {
        List<Class> list = currentMethodAttributesMap.get( "sideEffects" );
        list.add( methodSideEffectDescriptor.modifierClass() );
    }

    public Object getCompositeDescriptor( Node node )
    {
        return descriptorsMap.get( node );
    }

    public Map<String, List<Class>> getMethodAttributes( Method m )
    {
        return methodAttributesMap.get( m );
    }

    private void addHiddenEdge( Node source, Node target )
    {
        Edge edge = graph.addEdge( source, target );
        edge.setInt( FIELD_TYPE, EDGE_HIDDEN.code() );
    }
}
