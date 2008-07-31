/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Sonny Gill. All Rights Reserved.
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.framework.swing;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JFrame;
import org.qi4j.runtime.composite.CompositeModel;
import org.qi4j.runtime.structure.ApplicationModel;
import org.qi4j.runtime.structure.LayerModel;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleModel;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.Action;
import prefuse.action.assignment.ColorAction;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Edge;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.sort.ItemSorter;

import static org.qi4j.library.framework.swing.GraphConstants.*;
import org.qi4j.library.framework.swing.render.ApplicationRenderer;
import org.qi4j.library.framework.swing.render.CompositeRenderer;
import org.qi4j.library.framework.swing.render.LayerRenderer;
import org.qi4j.library.framework.swing.render.ModuleRenderer;
import org.qi4j.library.framework.swing.render.VerticalEdgeRenderer;

/**
 * TODO
 */
public class ApplicationGraph
{
    private static final int TYPE_APPLICATION = 0;
    private static final int TYPE_LAYER = 1;
    private static final int TYPE_MODULE = 2;
    private static final int TYPE_COMPOSITE = 3;

    private static final int TYPE_EDGE_HIDDEN = 100;

    //    private static final int ENTITY_COMPOSITE = 4;
//    private static final int SERVICE_COMPOSITE = 5;

    public void show( ApplicationModel applicationModel )
    {
        Graph graph = createData( applicationModel );
        Visualization visualization = createVisualization( graph );
        createRenderers( visualization );
        createProcessingActions( visualization );
        Display display = createDisplay( visualization );
        launchDisplay( applicationModel, visualization, display );
    }

    private void launchDisplay( ApplicationModel applicationModel, Visualization visualization, Display display )
    {
        JFrame frame = new JFrame( "Qi4j Application Graph - " + applicationModel.name() );
        frame.add( display );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.pack();
        frame.setVisible( true );

        // assign the colors
        visualization.run( "color" );
        // start up the layout
        visualization.run( "layout" );
        visualization.run( "hideEdges" );
//        visualization.run("repaint");
    }

    private Display createDisplay( Visualization visualization )
    {
        Display display = new Display( visualization );
        display.setSize( 800, 600 );
//        display.setItemSorter( new TreeDepthItemSorter() );
        // drag, pan and zoom controls
//        display.addControlListener( new SubtreeDragControl() );
        display.addControlListener( new PanControl() );
        display.addControlListener( new ZoomControl() );
        display.setItemSorter( new ItemSorter()
        {
            public int score( VisualItem item )
            {
                // First draw the Application box, then the edges, then other nodes
                if( item.getInt( FIELD_TYPE ) == TYPE_APPLICATION )
                {
                    return 0;
                }
                else if( item instanceof EdgeItem )
                {
                    return 1;
                }
                else
                {
                    return 2;
                }
            }
        } );
        return display;
    }

    private void createProcessingActions( Visualization visualization )
    {
        ActionList color = establishColors();
        ApplicationLayout layout = new ApplicationLayout( "graph" );

        visualization.putAction( "color", color );
        visualization.putAction( "layout", layout );
        visualization.putAction( "repaint", new RepaintAction() );
        visualization.putAction( "hideEdges", new Action()
        {

            public void run( double frac )
            {
                Iterator itr = m_vis.items( "graph.edges", "type=100" );
                while( itr.hasNext() )
                {
                    VisualItem item = (VisualItem) itr.next();
                    item.setVisible( false );
                }
            }
        } );
    }

    private void createRenderers( Visualization visualization )
    {

        DefaultRendererFactory rendererFactory = new DefaultRendererFactory();

        rendererFactory.add( "type = 0", new ApplicationRenderer() );
        rendererFactory.add( "type = 1", new LayerRenderer() );
        rendererFactory.add( "type = 2", new ModuleRenderer() );
        rendererFactory.add( "type = 3", new CompositeRenderer() );

        rendererFactory.setDefaultEdgeRenderer( new VerticalEdgeRenderer() );
        visualization.setRendererFactory( rendererFactory );
    }

    private Graph createData( ApplicationModel applicationModel )
    {
        final Graph graph = new Graph( true );
        graph.addColumn( FIELD_NAME, String.class );
        graph.addColumn( FIELD_TYPE, int.class );
        graph.addColumn( FIELD_LAYER_LEVEL, int.class );
        graph.addColumn( FIELD_USED_LAYERS, Collection.class );
        graph.addColumn( FIELD_USED_BY_LAYERS, Collection.class );

        final Node root = graph.addNode();
        root.setString( FIELD_NAME, "Application" );
        root.setInt( FIELD_TYPE, TYPE_APPLICATION );

        applicationModel.visitModel( new AppModelVisitor( graph, root ) );
        return graph;
    }

    private Visualization createVisualization( Graph graph )
    {
        // add the graph to the visualization as the data group "graph"
        // nodes and edges are accessible as "graph.nodes" and "graph.edges"
        Visualization visualization = new Visualization();
        visualization.add( "graph", graph );
        return visualization;
    }

    private ActionList establishColors()
    {
        // color for edges
        ColorAction edgesStroke = new ColorAction( "graph.edges", VisualItem.STROKECOLOR, ColorLib.gray( 100 ) );
        ColorAction edgesFill = new ColorAction( "graph.edges", VisualItem.FILLCOLOR, ColorLib.gray( 100 ) );

        // an action list containing all color assignments
        ActionList color = new ActionList();
        color.add( edgesStroke );
        color.add( edgesFill );
        return color;
    }


    private static class AppModelVisitor extends ModelVisitor
    {
        private Node layerNode;
        private Node moduleNode;
        private final Map<LayerModel, Node> layerNodes;
        private final Graph graph;
        private final Node root;

        public AppModelVisitor( Graph graph, Node root )
        {
            this.layerNodes = new HashMap<LayerModel, Node>();
            this.graph = graph;
            this.root = root;
        }

        @Override public void visit( LayerModel layerModel )
        {
            layerNode = getLayerNode( layerModel );

            Iterable<LayerModel> usedLayers = layerModel.usedLayers().layers();
            for( LayerModel usedLayerModel : usedLayers )
            {
                Node usedLayerNode = getLayerNode( usedLayerModel );
                addUsedLayer( layerNode, usedLayerNode );
                graph.addEdge( layerNode, usedLayerNode );
                incrementLayerLevel( usedLayerNode );
            }

            Edge edge = graph.addEdge( root, layerNode );
            edge.setInt( FIELD_TYPE, TYPE_EDGE_HIDDEN );
        }

        private Node getLayerNode( LayerModel layerModel )
        {
            Node layer = layerNodes.get( layerModel );
            if( layer == null )
            {
                layer = graph.addNode();
                String name = layerModel.name();
                layer.setString( FIELD_NAME, name );
                layer.setInt( FIELD_TYPE, TYPE_LAYER );
                layer.setInt( FIELD_LAYER_LEVEL, 1 );
                layer.set( FIELD_USED_LAYERS, new ArrayList() );
                layer.set( FIELD_USED_BY_LAYERS, new ArrayList() );
                layerNodes.put( layerModel, layer );
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

        @Override public void visit( ModuleModel moduleModel )
        {
            moduleNode = graph.addNode();
            moduleNode.setString( FIELD_NAME, moduleModel.name() );
            moduleNode.setInt( FIELD_TYPE, TYPE_MODULE );
            Edge edge = graph.addEdge( layerNode, moduleNode );
            edge.setInt( FIELD_TYPE, TYPE_EDGE_HIDDEN );
        }

        public void visit( CompositeModel compositeModel )
        {
            Node node = graph.addNode();
            node.setString( FIELD_NAME, compositeModel.type().getSimpleName() );
            node.setInt( FIELD_TYPE, TYPE_COMPOSITE );
            Edge edge = graph.addEdge( moduleNode, node );
            edge.setInt( FIELD_TYPE, TYPE_EDGE_HIDDEN );
        }

    }

}
