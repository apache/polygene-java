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
package org.qi4j.library.swing.visualizer.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.qi4j.library.swing.visualizer.ApplicationGraph;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * @author Sonny Gill
 */
public class ApplicationGraphVisitor extends DescriptorVisitor
{
    private final Graph graph;
    private final Node root;
    private Node layerNode;
    private Node moduleNode;

    private Node servicesNode;
    private Node entitiesNode;
    private Node compositesNode;
    private Node objectsNode;

    private final Map<LayerDescriptor, Node> layerDescriptorToNodeMap = new HashMap<LayerDescriptor, Node>();
    private final Map<Node, Object> descriptorsMap = new HashMap<Node, Object>();

    public ApplicationGraphVisitor( Graph graph )
    {
        this.graph = graph;

        graph.addColumn( GraphConstants.FIELD_NAME, String.class );
        graph.addColumn( GraphConstants.FIELD_TYPE, int.class );
        graph.addColumn( GraphConstants.FIELD_LAYER_LEVEL, int.class );
        graph.addColumn( GraphConstants.FIELD_USED_LAYERS, Collection.class );
        graph.addColumn( GraphConstants.FIELD_USED_BY_LAYERS, Collection.class );
        root = graph.addNode();
        root.setInt( GraphConstants.FIELD_TYPE, ApplicationGraph.TYPE_APPLICATION );
    }

    @Override public void visit( ApplicationDescriptor applicationDescriptor )
    {
        root.setString( GraphConstants.FIELD_NAME, applicationDescriptor.name() );
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

        addHiddenEdge( root, layerNode );
    }

    private Node getLayerNode( LayerDescriptor layerDescriptor )
    {
        Node layer = layerDescriptorToNodeMap.get( layerDescriptor );
        if( layer == null )
        {
            layer = graph.addNode();
            String name = layerDescriptor.name();
            layer.setString( GraphConstants.FIELD_NAME, name );
            layer.setInt( GraphConstants.FIELD_TYPE, ApplicationGraph.TYPE_LAYER );
            layer.setInt( GraphConstants.FIELD_LAYER_LEVEL, 1 );
            layer.set( GraphConstants.FIELD_USED_LAYERS, new ArrayList() );
            layer.set( GraphConstants.FIELD_USED_BY_LAYERS, new ArrayList() );
            layerDescriptorToNodeMap.put( layerDescriptor, layer );
        }

        return layer;
    }

    private void addUsedLayer( Node layer, Node usedLayer )
    {
        Collection<Node> usedLayers = (Collection<Node>) layer.get( GraphConstants.FIELD_USED_LAYERS );
        usedLayers.add( usedLayer );
        Collection<Node> usedByLayers = (Collection<Node>) usedLayer.get( GraphConstants.FIELD_USED_BY_LAYERS );
        usedByLayers.add( layer );
    }

    private void incrementLayerLevel( Node layer )
    {
        Collection<Node> usedLayers = (Collection<Node>) layer.get( GraphConstants.FIELD_USED_LAYERS );
        for( Node usedLayer : usedLayers )
        {
            incrementLayerLevel( usedLayer );
        }

        int level = layer.getInt( GraphConstants.FIELD_LAYER_LEVEL );
        layer.setInt( GraphConstants.FIELD_LAYER_LEVEL, ++level );
    }

    @Override public void visit( ModuleDescriptor moduleDescriptor )
    {
        moduleNode = graph.addNode();
        moduleNode.setString( GraphConstants.FIELD_NAME, moduleDescriptor.name() );
        moduleNode.setInt( GraphConstants.FIELD_TYPE, ApplicationGraph.TYPE_MODULE );

        addHiddenEdge( layerNode, moduleNode );

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
            servicesNode.setString( GraphConstants.FIELD_NAME, "Services" );
            servicesNode.setInt( GraphConstants.FIELD_TYPE, ApplicationGraph.TYPE_GROUP );
            addHiddenEdge( moduleNode, servicesNode );
        }

        Node node = graph.addNode();
        node.setString( GraphConstants.FIELD_NAME, GraphUtils.getCompositeName( serviceDescriptor.type() ) );
        node.setInt( GraphConstants.FIELD_TYPE, ApplicationGraph.TYPE_COMPOSITE );
        addHiddenEdge( servicesNode, node );

        descriptorsMap.put( node, serviceDescriptor );
    }

    public void visit( EntityDescriptor entityDescriptor )
    {
        if( entitiesNode == null )
        {
            entitiesNode = graph.addNode();
            entitiesNode.setString( GraphConstants.FIELD_NAME, "Entities" );
            entitiesNode.setInt( GraphConstants.FIELD_TYPE, ApplicationGraph.TYPE_GROUP );
            addHiddenEdge( moduleNode, entitiesNode );
        }

        Node node = graph.addNode();
        node.setString( GraphConstants.FIELD_NAME, GraphUtils.getCompositeName( entityDescriptor.type() ) );
        node.setInt( GraphConstants.FIELD_TYPE, ApplicationGraph.TYPE_COMPOSITE );
        addHiddenEdge( entitiesNode, node );

        descriptorsMap.put( node, entityDescriptor );
    }

    public void visit( CompositeDescriptor compositeDescriptor )
    {
        if( compositesNode == null )
        {
            compositesNode = graph.addNode();
            compositesNode.setString( GraphConstants.FIELD_NAME, "Composites" );
            compositesNode.setInt( GraphConstants.FIELD_TYPE, ApplicationGraph.TYPE_GROUP );
            addHiddenEdge( moduleNode, compositesNode );
        }

        Node node = graph.addNode();
        node.setString( GraphConstants.FIELD_NAME, GraphUtils.getCompositeName( compositeDescriptor.type() ) );
        node.setInt( GraphConstants.FIELD_TYPE, ApplicationGraph.TYPE_COMPOSITE );
        addHiddenEdge( compositesNode, node );

        descriptorsMap.put( node, compositeDescriptor );
    }

    public void visit( ObjectDescriptor objectDescriptor )
    {
        if( objectsNode == null )
        {
            System.out.println( "Creating objects node. Descriptor - " + objectDescriptor.toURI() );
            objectsNode = graph.addNode();
            objectsNode.setString( GraphConstants.FIELD_NAME, "Objects" );
            objectsNode.setInt( GraphConstants.FIELD_TYPE, ApplicationGraph.TYPE_GROUP );
            addHiddenEdge( moduleNode, objectsNode );
        }

        Node node = graph.addNode();
        node.setString( GraphConstants.FIELD_NAME, GraphUtils.getCompositeName( objectDescriptor.type() ) );
        node.setInt( GraphConstants.FIELD_TYPE, ApplicationGraph.TYPE_COMPOSITE );
        addHiddenEdge( objectsNode, node );

        descriptorsMap.put( node, objectDescriptor );
    }

    private Map<Method, Map<String, List>> methodAttributesMap = new HashMap<Method, Map<String, List>>();
    private Method currentMethod;
    private Map<String, List> currentMethodAttributesMap;

    public void visit( CompositeMethodDescriptor compositeMethodDescriptor )
    {
        currentMethod = compositeMethodDescriptor.method();
        currentMethodAttributesMap = new HashMap<String, List>();

        currentMethodAttributesMap.put( "constraints", new ArrayList() );
        currentMethodAttributesMap.put( "concerns", new ArrayList() );
        currentMethodAttributesMap.put( "sideEffects", new ArrayList() );
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
        List list = currentMethodAttributesMap.get( "concerns" );
        list.add( methodConcernDescriptor.modifierClass() );
    }

    public void visit( MethodSideEffectDescriptor methodSideEffectDescriptor )
    {
        List list = currentMethodAttributesMap.get( "sideEffects" );
        list.add( methodSideEffectDescriptor.modifierClass() );
    }

    public Object getCompositeDescriptor( Node node )
    {
        return descriptorsMap.get( node );
    }

    public Map<String, List> getMethodAttributes( Method m )
    {
        return methodAttributesMap.get( m );
    }

    private void addHiddenEdge( Node source, Node target )
    {
        Edge edge = graph.addEdge( source, target );
        edge.setInt( GraphConstants.FIELD_TYPE, ApplicationGraph.TYPE_EDGE_HIDDEN );
    }
}
