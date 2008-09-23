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
package org.qi4j.library.swing.visualizer.overview;

import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.PAGE_START;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.overview.internal.ApplicationGraphBuilder;
import org.qi4j.library.swing.visualizer.overview.internal.ApplicationLayout;
import org.qi4j.library.swing.visualizer.overview.internal.ItemSelectionControl;
import org.qi4j.library.swing.visualizer.overview.internal.PrefuseJScrollPane;
import org.qi4j.library.swing.visualizer.overview.internal.buttons.DisplayHelpButton;
import org.qi4j.library.swing.visualizer.overview.internal.buttons.TogglePanButton;
import org.qi4j.library.swing.visualizer.overview.internal.buttons.ZoomToActualSizeButton;
import org.qi4j.library.swing.visualizer.overview.internal.buttons.ZoomToFitButton;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_NAME;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_TYPE;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.APPLICATION;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.EDGE_HIDDEN;
import org.qi4j.library.swing.visualizer.overview.internal.render.RendererFactory;
import prefuse.Display;
import prefuse.Visualization;
import static prefuse.Visualization.FOCUS_ITEMS;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import static prefuse.data.expression.ComparisonPredicate.EQ;
import prefuse.data.expression.ObjectLiteral;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;
import static prefuse.util.display.DisplayLib.fitViewToBounds;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import static prefuse.visual.VisualItem.FILLCOLOR;
import static prefuse.visual.VisualItem.STROKECOLOR;
import prefuse.visual.sort.ItemSorter;

/**
 * TODO
 */
public class OverviewPanel extends JPanel
{
    private int animatedZoomDuration = 1000;

    private Visualization visualization;
    private Display display;
    private VisualItem applicationNodeItem;

    private Control selectionControl;

    public OverviewPanel( ApplicationDetailDescriptor anAppDescriptor, SelectionListener aListener )
    {
        super( new BorderLayout() );

        setPreferredSize( new Dimension( 800, 600 ) );

        selectionControl = new ItemSelectionControl( aListener );


        Graph graph = new Graph( true );
        visualization = createVisualization( graph, anAppDescriptor );

        display = createDisplay( visualization );
        PrefuseJScrollPane displayScrollPane = new PrefuseJScrollPane( display );
        add( displayScrollPane, CENTER );
        createPanningAndZoomingActions( display );

        launchDisplay( visualization );

        Node applicationNode = graph.getNode( 0 );
        applicationNodeItem = visualization.getVisualItem( "graph.nodes", applicationNode );

        // Toolbar
        JToolBar toolbar = createToolbar( display, applicationNodeItem );
        add( toolbar, PAGE_START );
    }

    private Visualization createVisualization( Graph graph, ApplicationDetailDescriptor anAppDescriptor )
    {
        ApplicationGraphBuilder builder = new ApplicationGraphBuilder( anAppDescriptor );
        builder.populate( graph );

        // add the graph to the visualization as the data group "graph"
        // nodes and edges are accessible as "graph.nodes" and "graph.edges"
        Visualization visualization = new Visualization();
        visualization.add( "graph", graph );

        RendererFactory rendererFactory = new RendererFactory();
        visualization.setRendererFactory( rendererFactory );

        createProcessingActions( visualization );

        return visualization;
    }

    private JToolBar createToolbar( Display aDisplay, VisualItem anApplicationNodeItem )
        throws IllegalArgumentException
    {
        validateNotNull( "aDisplay", aDisplay );
        validateNotNull( "anApplicationNodeItem", anApplicationNodeItem );

        JToolBar toolBar = new JToolBar();

        TogglePanButton togglePanButton = new TogglePanButton( aDisplay );
        toolBar.add( togglePanButton );

        ZoomToFitButton zoomToFitBtn = new ZoomToFitButton( aDisplay, anApplicationNodeItem );
        toolBar.add( zoomToFitBtn );

        ZoomToActualSizeButton actualSizeButton = new ZoomToActualSizeButton( aDisplay );
        toolBar.add( actualSizeButton );

        DisplayHelpButton helpButton = new DisplayHelpButton();
        toolBar.add( helpButton );

        return toolBar;
    }

    private void createPanningAndZoomingActions( Display component )
    {
        final int PREVIOUSLY_VISIBLE_AREA_WHEN_SCROLLING = 50;

        InputMap inputMap = component.getInputMap( WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        ActionMap actionMap = component.getActionMap();

        inputMap.put( KeyStroke.getKeyStroke( '+' ), "zoomIn" );
        actionMap.put( "zoomIn", new AbstractAction( "Zoom In" )
        {
            public void actionPerformed( ActionEvent e )
            {
                zoomIn( getDisplayCenter(), null );
            }
        } );

        inputMap.put( KeyStroke.getKeyStroke( '-' ), "zoomOut" );
        actionMap.put( "zoomOut", new AbstractAction( "Zoom Out" )
        {
            public void actionPerformed( ActionEvent e )
            {
                zoomOut( getDisplayCenter(), null );
            }
        } );

        inputMap.put( KeyStroke.getKeyStroke( VK_LEFT, 0 ), "panLeft" );
        actionMap.put( "panLeft", new AbstractAction( "Pan Left" )
        {
            public void actionPerformed( ActionEvent e )
            {
                pan( display.getWidth() - PREVIOUSLY_VISIBLE_AREA_WHEN_SCROLLING, 0, true );
            }
        } );

        inputMap.put( KeyStroke.getKeyStroke( VK_RIGHT, 0 ), "panRight" );
        actionMap.put( "panRight", new AbstractAction( "Pan Right" )
        {
            public void actionPerformed( ActionEvent e )
            {
                pan( PREVIOUSLY_VISIBLE_AREA_WHEN_SCROLLING - display.getWidth(), 0, true );
            }
        } );

        inputMap.put( KeyStroke.getKeyStroke( VK_UP, 0 ), "panUp" );
        actionMap.put( "panUp", new AbstractAction( "Pan Up" )
        {
            public void actionPerformed( ActionEvent e )
            {
                pan( 0, display.getHeight() - PREVIOUSLY_VISIBLE_AREA_WHEN_SCROLLING, true );
            }
        } );

        inputMap.put( KeyStroke.getKeyStroke( VK_DOWN, 0 ), "panDown" );
        actionMap.put( "panDown", new AbstractAction( "Pan Down" )
        {
            public void actionPerformed( ActionEvent e )
            {
                pan( 0, PREVIOUSLY_VISIBLE_AREA_WHEN_SCROLLING - display.getHeight(), true );
            }
        } );
    }

    private void launchDisplay( Visualization visualization )
    {
        visualization.run( "color" );
        visualization.run( "layout" );
        visualization.run( "hideEdges" );
        visualization.run( "repaint" );
    }

    private Display createDisplay( Visualization visualization )
    {
        Display display = new Display( visualization );

        display.setItemSorter( new ItemSorter()
        {
            public int score( VisualItem item )
            {
                // First draw the Application box, then the edges, then other nodes
                NodeType type = (NodeType) item.get( FIELD_TYPE );
                if( APPLICATION.equals( type ) )
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

        display.addControlListener( new MouseWheelZoomControl() );
        display.addControlListener( selectionControl );

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
                ComparisonPredicate edgePredicate = new ComparisonPredicate(
                    EQ, new ColumnExpression( FIELD_TYPE ), new ObjectLiteral( EDGE_HIDDEN )
                );

                Iterator itr = m_vis.items( "graph.edges", edgePredicate );
                while( itr.hasNext() )
                {
                    VisualItem item = (VisualItem) itr.next();
                    item.setVisible( false );
                }
            }
        } );
    }

    private ActionList establishColors()
    {
        // color for edges
        ColorAction edgesStroke = new ColorAction( "graph.edges", STROKECOLOR, ColorLib.gray( 100 ) );
        ColorAction edgesFill = new ColorAction( "graph.edges", FILLCOLOR, ColorLib.gray( 100 ) );

        // an action list containing all color assignments
        ActionList color = new ActionList();
        color.add( edgesStroke );
        color.add( edgesFill );
        return color;
    }

    public void showGraph()
    {
        zoomToFit();
    }

    private Point2D getDisplayCenter()
    {
        return new Point2D.Float( display.getWidth() / 2, display.getHeight() / 2 );
    }

    private void zoomToFit()
    {
        fitViewToBounds( display, applicationNodeItem.getBounds(), 0 );
    }

    private void zoomToActualSize()
    {
        display.animateZoom( getDisplayCenter(), 1 / display.getScale(), 0 );
    }

    private void zoomIn( Point2D p, Double scale )
    {
        if( !display.isTranformInProgress() )
        {

            double displayScale = display.getScale();

            if( displayScale == 1 )
            {
                return;
            }

            double zoomScale = scale == null ? 1.2 : scale;
            if( displayScale * zoomScale > 1 )
            {
                zoomToActualSize();
            }
            else
            {
                if( scale != null )
                {
                    display.zoom( p, zoomScale );
                }
                else
                {
                    display.animateZoom( p, zoomScale, animatedZoomDuration );
                }
                display.repaint();
            }

        }
    }

    private void zoomOut( Point2D p, Double scale )
    {
        if( !display.isTranformInProgress() )
        {

            Rectangle2D bounds = applicationNodeItem.getBounds();
            if( displaySizeFitsScaledBounds( display, bounds ) )
            {
                return;
            }

            double zoomScale = scale == null ? 0.8 : scale;
            double displayScale = display.getScale();

            int widthAfterZoom = (int) ( bounds.getWidth() * displayScale * zoomScale );
            int heightAfterZoom = (int) ( bounds.getHeight() * displayScale * zoomScale );

            if( widthAfterZoom <= display.getWidth() && heightAfterZoom <= display.getHeight() )
            {
                zoomToFit();
            }
            else
            {
                if( scale != null )
                {
                    display.zoom( p, zoomScale );
                }
                else
                {
                    display.animateZoom( p, zoomScale, animatedZoomDuration );
                }
                display.repaint();
            }
        }
    }

    private void pan( double x, double y, boolean animate )
    {
        if( !display.isTranformInProgress() )
        {
            Rectangle2D bounds = applicationNodeItem.getBounds();
            AffineTransform at = display.getTransform();

            if( x > 0 )
            {
                // panning left, mouse movement to right
                double scaledLeftX = bounds.getX() * at.getScaleX();    // Left bound of Bounding Box
                double distanceToLeftEdge = -( at.getTranslateX() ) - scaledLeftX;
                x = Math.min( x, distanceToLeftEdge );
            }
            else if( x < 0 )
            {
                //panning right, mouse movement to left
                int scaledRightX = (int) ( bounds.getMaxX() * at.getScaleX() );    // Right bound of BB
                double distanceToRightEdge = display.getWidth() - scaledRightX - at.getTranslateX();
                x = Math.max( x, distanceToRightEdge );
            }

            if( y > 0 )
            {
                // panning up, mouse movement towards the bottom of the panel
                int scaledTopY = (int) ( bounds.getY() * at.getScaleY() );    // Top bound of BB
                double distanceToTopEdge = -( at.getTranslateY() ) - scaledTopY;
                y = Math.min( y, distanceToTopEdge );

            }
            else if( y < 0 )
            {
                // panning down, mouse movement towards the top of the panel
                int scaledBottomY = (int) ( bounds.getMaxY() * at.getScaleY() );    // Bottom bound of BB
                double distanceToBottomEdge = display.getHeight() - scaledBottomY - at.getTranslateY();
                y = Math.max( y, distanceToBottomEdge );
            }

            if( animate )
            {
                display.animatePan( x, y, 500 );
            }
            else
            {
                display.pan( x, y );
            }
            display.repaint();
        }
    }

    public void clearCompositeSelection()
    {
        TupleSet focusGroup = visualization.getGroup( FOCUS_ITEMS );
        focusGroup.clear();
        repaint();
    }

    public void selectComposite( String name )
    {
        String query = FIELD_NAME + " = '" + name + "'";
        Predicate predicate = (Predicate) ExpressionParser.parse( query );

        TupleSet focusGroup = visualization.getGroup( FOCUS_ITEMS );
        focusGroup.clear();
        Iterator iterator = visualization.items( predicate );
        while( iterator.hasNext() )
        {
            Object o = iterator.next();
            focusGroup.addTuple( (Tuple) o );
        }

        System.out.println( visualization.run( "color" ) );
    }

    private class ActualSizeAction extends AbstractAction
    {
        private ActualSizeAction()
        {
            super( "Actual Size" );
        }

        public void actionPerformed( ActionEvent e )
        {
            zoomToActualSize();
        }

    }

    private class MouseWheelZoomControl extends ControlAdapter
    {
        public void itemWheelMoved( VisualItem item, MouseWheelEvent e )
        {
            mouseWheelMoved( e );
        }

        public void mouseWheelMoved( MouseWheelEvent e )
        {
            if( !display.isTranformInProgress() )
            {
                int clicks = e.getWheelRotation();
                Point2D p = getDisplayCenter();
                double zoom = Math.pow( 1.1, clicks );
                if( clicks < 0 )
                {
                    zoomOut( p, zoom );
                }
                else
                {
                    zoomIn( p, zoom );
                }
            }
        }
    }

    public static boolean displaySizeFitsScaledBounds( Display display, Rectangle2D bounds )
    {
        double scale = display.getScale();
        return ( bounds.getWidth() * scale == display.getWidth() ) && ( bounds.getHeight() * scale == display.getHeight() );
    }

    public static boolean displaySizeContainsScaledBounds( Display display, Rectangle2D bounds )
    {
        double scale = display.getScale();
        return ( display.getWidth() > bounds.getWidth() * scale ) && ( display.getHeight() > bounds.getHeight() * scale );
    }
}