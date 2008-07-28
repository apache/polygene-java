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
import javax.swing.JFrame;
import org.qi4j.runtime.composite.CompositeModel;
import org.qi4j.runtime.structure.ApplicationModel;
import org.qi4j.runtime.structure.LayerModel;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleModel;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Edge;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

import static org.qi4j.library.framework.swing.GraphConstants.*;

/**
 * TODO
 */
public class ApplicationGraph
{
    private static final int TYPE_APPLICATION = 0;
    private static final int TYPE_LAYER = 1;
    private static final int TYPE_MODULE = 2;
    private static final int TYPE_COMPOSITE = 3;

    private static final int TYPE_HIDDEN = 100;

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
//        visualization.run( "hideEdges" );
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
        return display;
    }

    private void createProcessingActions( Visualization visualization )
    {
        ActionList color = establishColors();
        ApplicationLayout layout = new ApplicationLayout( "graph" );

        visualization.putAction( "color", color );
        visualization.putAction( "layout", layout );
        visualization.putAction( "repaint", new RepaintAction() );
    }

    private void createRenderers( Visualization visualization )
    {

        DefaultRendererFactory rendererFactory = new DefaultRendererFactory();

        rendererFactory.add( "type = 0", new ApplicationRenderer() );
        rendererFactory.add( "type = 1", new LayerRenderer() );
        rendererFactory.add( "type = 2", new ModuleRenderer() );
        rendererFactory.add( "type = 3", new CompositeRenderer() );

        visualization.setRendererFactory( rendererFactory );
    }

    private Graph createData( ApplicationModel applicationModel )
    {
        final Graph graph = new Graph( true );
        graph.addColumn( FIELD_NAME, String.class );
        graph.addColumn( FIELD_TYPE, int.class );
        graph.addColumn( FIELD_LAYER_LEVEL, int.class );

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
        // create our nominal color palette
        int[] palette = new int[]{
            ColorLib.rgb( 100, 200, 100 ), // Application node
            ColorLib.rgb( 255, 60, 60 ), // layers
            ColorLib.rgb( 230, 230, 255 ), // modules
            ColorLib.rgb( 230, 180, 180 ),  // composites
        };
        // map nominal data values to colors using our provided palette
        DataColorAction fill = new DataColorAction( "graph.nodes", FIELD_TYPE, Constants.ORDINAL,
                                                    VisualItem.FILLCOLOR, palette );

        // color for node text
        ColorAction text = new ColorAction( "graph.nodes", VisualItem.TEXTCOLOR, ColorLib.gray( 0 ) );
        // color for edges
//        ColorAction edgesStroke = new ColorAction( "graph.edges", VisualItem.STROKECOLOR, ColorLib.gray( 100 ) );
//        ColorAction edgesFill = new ColorAction( "graph.edges", VisualItem.FILLCOLOR, ColorLib.gray( 100 ) );

        // an action list containing all color assignments
        ActionList color = new ActionList();
        color.add( fill );
        color.add( text );
//        color.add( edgesStroke );
//        color.add( edgesFill );
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
                graph.addEdge( layerNode, usedLayerNode );
                incrementLayerLevel( usedLayerNode );
            }

            graph.addEdge( root, layerNode );
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

                layerNodes.put( layerModel, layer );
            }

            return layer;
        }

        private void incrementLayerLevel( Node layer )
        {
            int level = layer.getInt( FIELD_LAYER_LEVEL );
            layer.setInt( FIELD_LAYER_LEVEL, ++level );
        }

        @Override public void visit( ModuleModel moduleModel )
        {
            moduleNode = graph.addNode();
            moduleNode.setString( FIELD_NAME, moduleModel.name() );
            moduleNode.setInt( FIELD_TYPE, TYPE_MODULE );
            Edge edge = graph.addEdge( layerNode, moduleNode );
            edge.setInt( FIELD_TYPE, TYPE_HIDDEN );
        }

        public void visit( CompositeModel compositeModel )
        {
            Node node = graph.addNode();
            node.setString( FIELD_NAME, compositeModel.type().getSimpleName() );
            node.setInt( FIELD_TYPE, TYPE_COMPOSITE );
            Edge edge = graph.addEdge( moduleNode, node );
            edge.setInt( FIELD_TYPE, TYPE_HIDDEN );
        }

    }

}
