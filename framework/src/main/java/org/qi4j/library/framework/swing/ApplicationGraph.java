/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Sonny Gill. All Rights Reserved.
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

import org.qi4j.runtime.structure.ApplicationModel;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.LayerModel;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.composite.CompositeModel;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.Visualization;
import prefuse.Constants;
import prefuse.Display;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.SubtreeDragControl;
import prefuse.visual.VisualItem;
import prefuse.util.ColorLib;
import prefuse.render.LabelRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import javax.swing.JFrame;
import java.util.Map;
import java.util.HashMap;

/**
 * TODO
 */
public class ApplicationGraph
{
    private static final int APPLICATION = 0;
    private static final int LAYER = 1;
    private static final int MODULE = 2;
    private static final int COMPOSITE = 3;

    private static final String NAME = "name";
    private static final String TYPE = "type";

//    private static final int ENTITY_COMPOSITE = 4;
//    private static final int SERVICE_COMPOSITE = 5;

    public void show( ApplicationModel applicationModel )
    {

        final Graph graph = new Graph( true );
        graph.addColumn( NAME, String.class );
        graph.addColumn( TYPE, int.class );

        final Node root = graph.addNode();
        root.setString( NAME, "Application" );
        root.setInt( TYPE, APPLICATION );

        final Map<LayerModel, Node> layerNodes = new HashMap<LayerModel, Node>();
        applicationModel.visitModel( new ModelVisitor()
        {

            Node layerNode;
            Node moduleNode;

            @Override public void visit( LayerModel layerModel )
            {
                layerNode = layerNodes.get( layerModel );
                if( layerNode == null )
                {
                    layerNode = graph.addNode();
                    layerNodes.put( layerModel, layerNode );
                }

                Iterable<LayerModel> layers = layerModel.usedLayers().layers();
                for( LayerModel layer : layers )
                {
                    Node usedLayerNode = layerNodes.get( layer );
                    if( usedLayerNode == null )
                    {
                        usedLayerNode = graph.addNode();
                        layerNodes.put( layer, usedLayerNode );
                    }
                    graph.addEdge( layerNode, usedLayerNode );
                }

                layerNode.setString( NAME, layerModel.name() );
                layerNode.setInt( TYPE, LAYER );
                graph.addEdge( root, layerNode );
            }

            @Override
            public void visit( ModuleModel moduleModel )
            {
                moduleNode = graph.addNode();
                moduleNode.setString( NAME, moduleModel.name() );
                moduleNode.setInt( TYPE, MODULE );
                graph.addEdge( layerNode, moduleNode );
            }

            @Override public void visit( CompositeModel compositeModel )
            {
                Node node = graph.addNode();
                node.setString( NAME, compositeModel.type().getSimpleName() );
                node.setInt( TYPE, COMPOSITE );
                graph.addEdge( moduleNode, node );
            }

        } );

        // add the graph to the visualization as the data group "graph"
        // nodes and edges are accessible as "graph.nodes" and "graph.edges"
        Visualization vis = new Visualization();
        vis.add( "graph", graph );
        vis.setInteractive( "graph.edges", null, false );

        // draw the "name" label for NodeItems
        LabelRenderer labelRenderer = new LabelRenderer( NAME );
        labelRenderer.setRoundedCorner( 8, 8 );
        labelRenderer.setHorizontalPadding( 5 );
        labelRenderer.setVerticalPadding( 5 );

        // create a new default renderer factory
        // return our name label renderer as the default for all non-EdgeItems
        // includes straight line edges for EdgeItems by default
        vis.setRendererFactory( new DefaultRendererFactory( labelRenderer ) );

        // create our nominal color palette
        int[] palette = new int[]{
            ColorLib.rgb( 240, 240, 240 ), // Application node
            ColorLib.rgb( 255, 230, 230 ), // layers
            ColorLib.rgb( 220, 220, 255 ), // modules
            ColorLib.rgb( 255, 250, 205 )  // composites
        };
        // map nominal data values to colors using our provided palette
        DataColorAction fill = new DataColorAction( "graph.nodes", TYPE, Constants.NOMINAL,
                                                    VisualItem.FILLCOLOR, palette );

        // color for node text
        ColorAction text = new ColorAction( "graph.nodes", VisualItem.TEXTCOLOR, ColorLib.gray( 0 ) );
        // color for edges
        ColorAction edgesStroke = new ColorAction( "graph.edges", VisualItem.STROKECOLOR, ColorLib.gray( 200 ) );
        ColorAction edgesFill = new ColorAction( "graph.edges", VisualItem.FILLCOLOR, ColorLib.gray( 200 ) );

        // an action list containing all color assignments
        ActionList color = new ActionList();
        color.add( fill );
        color.add( text );
        color.add( edgesStroke );
        color.add( edgesFill );

        // an action list with the layout
        ActionList layout = new ActionList();
        layout.add( new NodeLinkTreeLayout( "graph", Constants.ORIENT_TOP_BOTTOM, 20, 20, 20 ) );
        layout.add( new RepaintAction() );

        // add the actions to the visualization
        vis.putAction( "color", color );
        vis.putAction( "layout", layout );

        Display display = new Display( vis );
        display.setSize( 640, 480 );

        // drag, pan and zoom controls
        display.addControlListener( new SubtreeDragControl() );
        display.addControlListener( new PanControl() );
        display.addControlListener( new ZoomControl() );

        JFrame frame = new JFrame( "Qi4j Application Graph - " + applicationModel.name() );
        frame.add( display );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.pack();
        frame.setVisible( true );

        // assign the colors
        vis.run( "color" );
        // start up the layout
        vis.run( "layout" );
    }
}
