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
package org.qi4j.library.swing.visualizer.overview.internal.visualization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.composite.Composite;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ServiceDetailDescriptor;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_DESCRIPTOR;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_LAYER_LEVEL;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_NAME;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_TYPE;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_USED_BY_LAYERS;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_USED_LAYERS;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.GROUP_NAME_COMPOSITES;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.GROUP_NAME_ENTITIES;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.GROUP_NAME_OBJECTS;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.GROUP_NAME_SERVICES;
import org.qi4j.library.swing.visualizer.overview.internal.common.NodeType;
import static org.qi4j.library.swing.visualizer.overview.internal.common.NodeType.APPLICATION;
import static org.qi4j.library.swing.visualizer.overview.internal.common.NodeType.COMPOSITE;
import static org.qi4j.library.swing.visualizer.overview.internal.common.NodeType.EDGE_HIDDEN;
import static org.qi4j.library.swing.visualizer.overview.internal.common.NodeType.ENTITY;
import static org.qi4j.library.swing.visualizer.overview.internal.common.NodeType.GROUP;
import static org.qi4j.library.swing.visualizer.overview.internal.common.NodeType.LAYER;
import static org.qi4j.library.swing.visualizer.overview.internal.common.NodeType.MODULE;
import static org.qi4j.library.swing.visualizer.overview.internal.common.NodeType.OBJECT;
import static org.qi4j.library.swing.visualizer.overview.internal.common.NodeType.SERVICE;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.structure.LayerDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * This class is not thread safe.
 *
 * @author Sonny Gill
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class ApplicationGraphBuilder
{
    // Temp application descriptor
    private ApplicationDetailDescriptor appDetailDescriptor;

    // Cache to lookup layer descriptor -> node
    private final Map<LayerDetailDescriptor, Node> layerDescriptorToNodeMap;

    public ApplicationGraphBuilder( ApplicationDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        appDetailDescriptor = aDescriptor;
        layerDescriptorToNodeMap = new HashMap<LayerDetailDescriptor, Node>();
    }

    public void populate( Graph aGraph )
    {
        validateNotNull( "aGraph", aGraph );

        // Clear cache
        layerDescriptorToNodeMap.clear();

        aGraph.addColumn( FIELD_NAME, String.class );
        aGraph.addColumn( FIELD_TYPE, NodeType.class );
        aGraph.addColumn( FIELD_LAYER_LEVEL, int.class );
        aGraph.addColumn( FIELD_USED_LAYERS, Collection.class );
        aGraph.addColumn( FIELD_USED_BY_LAYERS, Collection.class );
        aGraph.addColumn( FIELD_DESCRIPTOR, Object.class );

        addApplicationNode( aGraph );
    }

    private void addApplicationNode( Graph aGraph )
    {
        Node appNode = aGraph.addNode();
        appNode.set( FIELD_TYPE, APPLICATION );

        ApplicationDescriptor descriptor = appDetailDescriptor.descriptor();
        appNode.setString( FIELD_NAME, descriptor.name() );
        appNode.set( FIELD_DESCRIPTOR, appDetailDescriptor );

        Iterable<LayerDetailDescriptor> layers = appDetailDescriptor.layers();
        for( LayerDetailDescriptor layer : layers )
        {
            addLayerNode( aGraph, appNode, layer );
        }
    }

    private void addLayerNode( Graph aGraph, Node anAppNode, LayerDetailDescriptor aDetailDescriptor )
    {
        Node layerNode = getLayerNode( aGraph, aDetailDescriptor );
        addHiddenEdge( aGraph, anAppNode, layerNode );

        // Used layers
        Iterable<LayerDetailDescriptor> usedLayers = aDetailDescriptor.usedLayers();
        for( LayerDetailDescriptor usedLayer : usedLayers )
        {
            Node usedLayerNode = getLayerNode( aGraph, usedLayer );

            addUsedLayer( layerNode, usedLayerNode );
            aGraph.addEdge( layerNode, usedLayerNode );
            incrementLayerLevel( usedLayerNode );
        }

        // Modules
        Iterable<ModuleDetailDescriptor> modules = aDetailDescriptor.modules();
        for( ModuleDetailDescriptor module : modules )
        {
            addModuleNode( aGraph, layerNode, module );
        }
    }

    private Node getLayerNode( Graph aGraph, LayerDetailDescriptor aDetailDescriptor )
    {
        Node layer = layerDescriptorToNodeMap.get( aDetailDescriptor );
        if( layer == null )
        {
            layer = aGraph.addNode();

            LayerDescriptor descriptor = aDetailDescriptor.descriptor();
            String name = descriptor.name();
            layer.setString( FIELD_NAME, name );

            layer.set( FIELD_DESCRIPTOR, aDetailDescriptor );
            layer.set( FIELD_TYPE, LAYER );
            layer.setInt( FIELD_LAYER_LEVEL, 1 );
            layer.set( FIELD_USED_LAYERS, new ArrayList<Node>() );
            layer.set( FIELD_USED_BY_LAYERS, new ArrayList<Node>() );

            layerDescriptorToNodeMap.put( aDetailDescriptor, layer );
        }

        return layer;
    }

    @SuppressWarnings( "unchecked" )
    private void addUsedLayer( Node layer, Node usedLayer )
    {
        List<Node> usedLayers = (List<Node>) layer.get( FIELD_USED_LAYERS );
        usedLayers.add( usedLayer );

        List<Node> usedByLayers = (List<Node>) usedLayer.get( FIELD_USED_BY_LAYERS );
        usedByLayers.add( layer );
    }

    @SuppressWarnings( "unchecked" )
    private void incrementLayerLevel( Node layer )
    {
        List<Node> usedLayers = (List<Node>) layer.get( FIELD_USED_LAYERS );
        for( Node usedLayer : usedLayers )
        {
            incrementLayerLevel( usedLayer );
        }

        int level = layer.getInt( FIELD_LAYER_LEVEL );
        layer.setInt( FIELD_LAYER_LEVEL, ++level );
    }

    private void addModuleNode( Graph aGraph, Node aLayerNode, ModuleDetailDescriptor aDetailDescriptor )
    {
        Node moduleNode = aGraph.addNode();
        addHiddenEdge( aGraph, aLayerNode, moduleNode );

        // Populate general fields
        ModuleDescriptor descriptor = aDetailDescriptor.descriptor();
        moduleNode.setString( FIELD_NAME, descriptor.name() );
        moduleNode.set( FIELD_TYPE, MODULE );
        moduleNode.set( FIELD_DESCRIPTOR, aDetailDescriptor );

        addServiceNodes( aGraph, moduleNode, aDetailDescriptor );
        addEntityNodes( aGraph, moduleNode, aDetailDescriptor );
        addCompositeNodes( aGraph, moduleNode, aDetailDescriptor );
        addObjectNodes( aGraph, moduleNode, aDetailDescriptor );
    }


    private void addServiceNodes( Graph aGraph, Node aModuleNode, ModuleDetailDescriptor aDetailDescriptor )
    {
        Iterable<ServiceDetailDescriptor> serviceDescriptors = aDetailDescriptor.services();

        Node servicesNode = null;
        for( ServiceDetailDescriptor detailDescriptor : serviceDescriptors )
        {
            // Add service group if not exists
            if( servicesNode == null )
            {
                servicesNode = aGraph.addNode();
                addHiddenEdge( aGraph, aModuleNode, servicesNode );

                servicesNode.setString( FIELD_NAME, GROUP_NAME_SERVICES );
                servicesNode.set( FIELD_TYPE, GROUP );
            }

            // Add service node
            Node serviceNode = aGraph.addNode();
            addHiddenEdge( aGraph, servicesNode, serviceNode );

            ServiceDescriptor descriptor = detailDescriptor.descriptor();
            Class<?> serviceClass = descriptor.type();
            String nodeName = serviceClass.getSimpleName();
            serviceNode.setString( FIELD_NAME, nodeName );
            serviceNode.set( FIELD_TYPE, SERVICE );
            serviceNode.set( FIELD_DESCRIPTOR, detailDescriptor );
        }
    }

    private void addEntityNodes( Graph aGraph, Node aModuleNode, ModuleDetailDescriptor aDetailDescriptor )
    {
        Iterable<EntityDetailDescriptor> entityDetailDescriptors = aDetailDescriptor.entities();

        Node entitiesNode = null;
        for( EntityDetailDescriptor entityDetailDescriptor : entityDetailDescriptors )
        {
            // Add entities group if not exists
            if( entitiesNode == null )
            {
                entitiesNode = aGraph.addNode();
                addHiddenEdge( aGraph, aModuleNode, entitiesNode );

                entitiesNode.setString( FIELD_NAME, GROUP_NAME_ENTITIES );
                entitiesNode.set( FIELD_TYPE, GROUP );
            }

            Node entityNode = aGraph.addNode();
            addHiddenEdge( aGraph, entitiesNode, entityNode );

            EntityDescriptor descriptor = entityDetailDescriptor.descriptor();
            Class<? extends Composite> entityClass = descriptor.type();
            entityNode.setString( FIELD_NAME, entityClass.getSimpleName() );
            entityNode.set( FIELD_TYPE, ENTITY );
            entityNode.set( FIELD_DESCRIPTOR, entityDetailDescriptor );
        }
    }

    private void addCompositeNodes( Graph aGraph, Node moduleNode, ModuleDetailDescriptor aDetailDescriptor )
    {
        Iterable<CompositeDetailDescriptor> compositeDetailDescriptors = aDetailDescriptor.composites();

        Node compositesNode = null;
        for( CompositeDetailDescriptor compositeDetailDescriptor : compositeDetailDescriptors )
        {
            // Add composite nodes if not exists
            if( compositesNode == null )
            {
                compositesNode = aGraph.addNode();
                addHiddenEdge( aGraph, moduleNode, compositesNode );

                compositesNode.setString( FIELD_NAME, GROUP_NAME_COMPOSITES );
                compositesNode.set( FIELD_TYPE, GROUP );
            }

            Node compositeNode = aGraph.addNode();
            addHiddenEdge( aGraph, compositesNode, compositeNode );

            CompositeDescriptor descriptor = compositeDetailDescriptor.descriptor();
            Class<? extends Composite> compositeClass = descriptor.type();
            String nodeName = compositeClass.getSimpleName();
            compositeNode.setString( FIELD_NAME, nodeName );
            compositeNode.set( FIELD_TYPE, COMPOSITE );

            compositeNode.set( FIELD_DESCRIPTOR, compositeDetailDescriptor );
        }
    }

    private void addObjectNodes( Graph aGraph, Node aModuleNode, ModuleDetailDescriptor aDetailDescriptor )
    {
        Iterable<ObjectDetailDescriptor> objectDetailDescriptors = aDetailDescriptor.objects();

        Node objectsNode = null;
        for( ObjectDetailDescriptor objectDetailDescriptor : objectDetailDescriptors )
        {
            // Add object nodes if not exists
            if( objectsNode == null )
            {
                objectsNode = aGraph.addNode();
                addHiddenEdge( aGraph, aModuleNode, objectsNode );

                objectsNode.setString( FIELD_NAME, GROUP_NAME_OBJECTS );
                objectsNode.set( FIELD_TYPE, GROUP );
            }

            Node objectNode = aGraph.addNode();
            addHiddenEdge( aGraph, objectsNode, objectNode );

            ObjectDescriptor objectDescriptor = objectDetailDescriptor.descriptor();
            Class<? extends Composite> objectClassName = objectDescriptor.type();
            objectNode.setString( FIELD_NAME, objectClassName.getSimpleName() );
            objectNode.set( FIELD_TYPE, OBJECT );
            objectNode.set( FIELD_DESCRIPTOR, objectDetailDescriptor );
        }
    }

    private void addHiddenEdge( Graph aGraph, Node source, Node target )
    {
        Edge edge = aGraph.addEdge( source, target );
        edge.set( FIELD_TYPE, EDGE_HIDDEN );
    }
}
