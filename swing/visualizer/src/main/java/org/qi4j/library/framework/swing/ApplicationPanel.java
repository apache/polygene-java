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

import java.util.Collection;
import java.util.Iterator;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import static org.qi4j.library.framework.swing.GraphConstants.FIELD_LAYER_LEVEL;
import static org.qi4j.library.framework.swing.GraphConstants.FIELD_NAME;
import static org.qi4j.library.framework.swing.GraphConstants.FIELD_TYPE;
import static org.qi4j.library.framework.swing.GraphConstants.FIELD_USED_BY_LAYERS;
import static org.qi4j.library.framework.swing.GraphConstants.FIELD_USED_LAYERS;
import org.qi4j.library.framework.swing.render.ApplicationRenderer;
import org.qi4j.library.framework.swing.render.CompositeRenderer;
import org.qi4j.library.framework.swing.render.LayerRenderer;
import org.qi4j.library.framework.swing.render.ModuleRenderer;
import org.qi4j.library.framework.swing.render.VerticalEdgeRenderer;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.structure.Application;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.sort.ItemSorter;

/**
 * TODO
 */
public class ApplicationPanel extends JPanel
{
    static final int TYPE_APPLICATION = 0;
    static final int TYPE_LAYER = 1;
    static final int TYPE_MODULE = 2;
    static final int TYPE_COMPOSITE = 3;

    static final int TYPE_EDGE_HIDDEN = 100;

    private Display display;

    public ApplicationPanel( Application application )
    {
        super( new BorderLayout() );

        Graph graph = createData( application );
        Visualization visualization = createVisualization( graph );
        createRenderers( visualization );
        createProcessingActions( visualization );
        display = createDisplay( visualization );
        launchDisplay( visualization, display );

        ZoomInAction zoomIn = new ZoomInAction( display );
        ZoomOutAction zoomOut = new ZoomOutAction( display );

        JPanel controlsPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        JButton zoomInBtn = new JButton( zoomIn );
        JButton zoomOutBtn = new JButton( zoomOut );

        controlsPanel.setBackground( Color.white );
        zoomInBtn.setBackground( Color.white );
        zoomOutBtn.setBackground( Color.white );

        controlsPanel.add( zoomInBtn );
        controlsPanel.add( zoomOutBtn );

        add( controlsPanel, BorderLayout.NORTH );
        add( display, BorderLayout.CENTER );

        InputMap inputMap = getInputMap( WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        inputMap.put( KeyStroke.getKeyStroke( '+' ), "zoomIn" );
        inputMap.put( KeyStroke.getKeyStroke( '-' ), "zoomOut" );

        ActionMap actionMap = getActionMap();
        actionMap.put( "zoomIn", zoomIn );
        actionMap.put( "zoomOut", zoomOut );
    }

    private void launchDisplay( Visualization visualization, Display display )
    {
        visualization.run( "color" );
        visualization.run( "layout" );
        visualization.run( "hideEdges" );
        visualization.run( "repaint" );
//        Rectangle2D bounds = visualization.getBounds( Visualization.ALL_ITEMS );
//        System.out.println( bounds );
//        DisplayLib.fitViewToBounds( display, bounds, 0);
//        display.setSize( (int) bounds.getWidth(), (int) bounds.getHeight() );
    }

    private Display createDisplay( Visualization visualization )
    {
        Display display = new Display( visualization );
        display.setSize( 800, 600 );

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

    private Graph createData( Application applicationModel )
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

        ( (ApplicationSPI) applicationModel ).visitDescriptor( new ApplicationModelVisitor( graph, root ) );
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

    public Display getDisplay()
    {
        return display;
    }

    private class ZoomInAction extends AbstractAction
    {
        private Display display;

        private ZoomInAction( Display display )
        {
            super( "Zoom In" );
            this.display = display;
        }

        public void actionPerformed( ActionEvent e )
        {
            Point2D p = new Point2D.Float( display.getWidth() / 2, display.getHeight() / 2 );
            display.zoom( p, 1.1 );
            display.repaint();
        }
    }

    private class ZoomOutAction extends AbstractAction
    {
        private Display display;

        private ZoomOutAction( Display display )
        {
            super( "Zoom Out" );
            this.display = display;
        }

        public void actionPerformed( ActionEvent e )
        {
            Point2D p = new Point2D.Float( display.getWidth() / 2, display.getHeight() / 2 );
            display.zoom( p, 0.9 );
            display.repaint();
        }
    }

}