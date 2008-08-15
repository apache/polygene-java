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
package org.qi4j.library.swing.visualizer;

import java.util.Iterator;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.Cursor;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import org.qi4j.library.swing.visualizer.render.ApplicationRenderer;
import org.qi4j.library.swing.visualizer.render.CompositeRenderer;
import org.qi4j.library.swing.visualizer.render.LayerRenderer;
import org.qi4j.library.swing.visualizer.render.ModuleRenderer;
import org.qi4j.library.swing.visualizer.render.VerticalEdgeRenderer;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.controls.Control;
import prefuse.controls.PanControl;
import prefuse.controls.ControlAdapter;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.util.ui.UILib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.PaintListener;
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

    private int animatedZoomDuration = 1000;

    private Visualization visualization;
    private Display display;
    private VisualItem applicationNodeItem;

    private Control compositeSelectionControl;

    public ApplicationPanel( Graph graph, Control compositeSelectionControl )
    {
        super( new BorderLayout() );
        this.compositeSelectionControl = compositeSelectionControl;

        visualization = createVisualization( graph );
        createRenderers( visualization );
        createProcessingActions( visualization );
        display = createDisplay( visualization );
        launchDisplay( visualization, display );
        createDisplayActions( display );

        Node applicationNode = graph.getNode( 0 );
        applicationNodeItem = visualization.getVisualItem( "graph.nodes", applicationNode );

        JPanel controlsPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        JButton zoomToFitBtn = new JButton( new ZoomToFitAction() );
        JButton actualSizeButton = new JButton( new ActualSizeAction() );

        controlsPanel.setBackground( Color.white );
        zoomToFitBtn.setBackground( Color.white );
        actualSizeButton.setBackground( Color.white );

        controlsPanel.add( zoomToFitBtn );
        controlsPanel.add( actualSizeButton );

        add( controlsPanel, BorderLayout.NORTH );
        add( new PrefuseJScrollPane( display ), BorderLayout.CENTER );

    }

    private void createDisplayActions( Display display )
    {
        InputMap inputMap = getInputMap( WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        ActionMap actionMap = getActionMap();

        inputMap.put( KeyStroke.getKeyStroke( '+' ), "zoomIn" );
        actionMap.put( "zoomIn", new ZoomInAction() );

        inputMap.put( KeyStroke.getKeyStroke( '-' ), "zoomOut" );
        actionMap.put( "zoomOut", new ZoomOutAction() );

/*
        Action panLeft = new PanLeftAction( display );
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0 ), "panLeft" );
        actionMap.put( "panLeft", panLeft );

        Action panRight = new PanRightAction( display );
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0 ), "panRight" );
        actionMap.put( "panRight", panRight );

        Action panUp = new PanUpAction( display );
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0 ), "panUp" );
        actionMap.put( "panUp", panUp );

        Action panDown = new PanDownAction( display );
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0 ), "panDown" );
        actionMap.put( "panDown", panDown );
*/
    }

    private void launchDisplay( Visualization visualization, Display display )
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
                if( item.getInt( GraphConstants.FIELD_TYPE ) == TYPE_APPLICATION )
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

        display.addControlListener( new MousePanControl( true ) );
        display.addControlListener( new MouseWheelZoomControl() );
        display.addControlListener( new DoubleClickZoomControl() );
        display.addControlListener( compositeSelectionControl );
        display.addPaintListener( new DisplayExpansionListener() );

        return display;
    }

    private void createProcessingActions( Visualization visualization )
    {
        ActionList color = establishColors();
        ApplicationLayout layout = new ApplicationLayout( "graph" );

        visualization.putAction( "color", color );
        visualization.putAction( "layout", layout );
        visualization.putAction( "repaint", new RepaintAction() );
        visualization.putAction( "hideEdges", new prefuse.action.Action()
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

    public void graphShown()
    {
        zoomToFit();
    }

    private Point2D getDisplayCenter()
    {
        return new Point2D.Float( display.getWidth() / 2, display.getHeight() / 2 );
    }

    private void zoomToFit()
    {
        DisplayLib.fitViewToBounds( display, applicationNodeItem.getBounds(), 2000 );
        display.repaint();
    }

    private void zoomToActualSize()
    {
        display.animateZoom( getDisplayCenter(), 1 / display.getScale(), 2000 );
        display.repaint();
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
            if( GraphUtils.displaySizeFitsScaledBounds( display, bounds ) )
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

    private void pan( double x, double y )
    {
        Rectangle2D bounds = applicationNodeItem.getBounds();
        AffineTransform at = display.getTransform();

        double scaledLeftX = bounds.getX() * at.getScaleX();    // Left bound of Bounding Box
        int scaledRightX = (int) ( bounds.getMaxX() * at.getScaleX() );    // Right bound of BB

        int scaledTopY = (int) ( bounds.getY() * at.getScaleY() );    // Top bound of BB
        int scaledBottom = (int) ( bounds.getMaxY() * at.getScaleY() );    // Bottom bound of BB

        // Instead of setting values to 0, we can calculate the new value to satisify both conditions,
        // but this works for now
        boolean canPanLeft = scaledLeftX + at.getTranslateX() + x <= 0;
        if( !canPanLeft )
        {
            x = Math.min( x, 0 );
        }
        boolean canPanRight = scaledRightX + at.getTranslateX() + x >= display.getWidth();
        if( !canPanRight )
        {
            x = Math.max( x, 0 );
        }
        boolean canPanUp = scaledTopY + at.getTranslateY() + y <= 0;
        if( !canPanUp )
        {
            y = Math.min( y, 0 );
        }
        boolean canPanDown = scaledBottom + at.getTranslateY() + y >= display.getHeight();
        if( !canPanDown )
        {
            y = Math.max( y, 0 );
        }

        display.pan( x, y );
        display.repaint();

    }

    private class ZoomToFitAction extends AbstractAction
    {
        private ZoomToFitAction()
        {
            super( "Zoom To Fit" );
        }

        public void actionPerformed( ActionEvent e )
        {
            zoomToFit();
        }
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

    private class ZoomInAction extends AbstractAction
    {
        private ZoomInAction()
        {
            super( "Zoom In" );
        }

        public void actionPerformed( ActionEvent e )
        {
            zoomIn( getDisplayCenter(), null );
        }

    }

    private class ZoomOutAction extends AbstractAction
    {
        private ZoomOutAction()
        {
            super( "Zoom Out" );
        }

        public void actionPerformed( ActionEvent e )
        {
            zoomOut( getDisplayCenter(), null );
        }
    }

    private int panMovement = 10;

    private class PanLeftAction extends AbstractAction
    {
        private Display display;

        private PanLeftAction( Display display )
        {
            super( "Pan Left" );
            this.display = display;
        }

        public void actionPerformed( ActionEvent e )
        {
            AffineTransform at = display.getTransform();
            display.pan( at.getShearX() - panMovement, at.getShearY() );
            display.repaint();
        }
    }

    private class PanRightAction extends AbstractAction
    {
        private Display display;

        private PanRightAction( Display display )
        {
            super( "Pan Right" );
            this.display = display;
        }

        public void actionPerformed( ActionEvent e )
        {
            AffineTransform at = display.getTransform();
            display.pan( at.getShearX() + panMovement, at.getShearY() );
            display.repaint();
        }
    }

    private class PanUpAction extends AbstractAction
    {
        private Display display;

        private PanUpAction( Display display )
        {
            super( "Pan Up" );
            this.display = display;
        }

        public void actionPerformed( ActionEvent e )
        {
            AffineTransform at = display.getTransform();
            display.pan( at.getShearX(), at.getShearY() - panMovement );
            display.repaint();
        }
    }

    private class PanDownAction extends AbstractAction
    {
        private Display display;

        private PanDownAction( Display display )
        {
            super( "Pan Down" );
            this.display = display;
        }

        public void actionPerformed( ActionEvent e )
        {
            AffineTransform at = display.getTransform();
            display.pan( at.getShearX(), at.getShearY() + panMovement );
            display.repaint();
        }
    }

    private class DoubleClickZoomControl extends ControlAdapter
    {
        public void itemClicked( VisualItem item, MouseEvent e )
        {
            zoom( e, new Point( e.getX(), e.getY() ) );
        }

        public void mouseClicked( MouseEvent e )
        {
            zoom( e, getDisplayCenter() );
        }

        private void zoom( MouseEvent e, Point2D p )
        {
            if( !display.isTranformInProgress() )
            {

                int count = e.getClickCount();
                if( count == 2 )
                {
                    if( e.isShiftDown() )
                    {
                        zoomOut( p, null );
                    }
                    else
                    {
                        zoomIn( p, null );
                    }
                }

            }
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

    /**
     * Listens for increase in the size of the display, and fits the graph to the new size if needed
     */
    private class DisplayExpansionListener implements PaintListener
    {
        int lastWidth = 0;
        int lastHeight = 0;

        public void prePaint( Display d, Graphics2D g )
        {
            // nothing doing
        }

        public void postPaint( Display d, Graphics2D g )
        {
            if( !d.isTranformInProgress() )
            {
                int width = d.getWidth();
                int height = d.getHeight();
                if( width > lastWidth || height > lastHeight )
                {
                    if( GraphUtils.displaySizeContainsScaledBounds( d, applicationNodeItem.getBounds() ) )
                    {
                        zoomToFit();
                    }
                }
                lastWidth = width;
                lastHeight = height;
            }
        }
    }

    private class MousePanControl extends PanControl
    {

        private int m_xDown, m_yDown;

        public MousePanControl( boolean b )
        {
            super( b );
        }

        public void mousePressed( MouseEvent e )
        {
            if( UILib.isButtonPressed( e, LEFT_MOUSE_BUTTON ) )
            {
                e.getComponent().setCursor( Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR ) );
                m_xDown = e.getX();
                m_yDown = e.getY();
            }
        }

        public void mouseDragged( MouseEvent e )
        {
            if( UILib.isButtonPressed( e, LEFT_MOUSE_BUTTON ) )
            {
                int x = e.getX(), y = e.getY();
                int dx = x - m_xDown, dy = y - m_yDown;

                pan( dx, dy );

                m_xDown = x;
                m_yDown = y;
            }
        }

        public void mouseReleased( MouseEvent e )
        {
            if( UILib.isButtonPressed( e, LEFT_MOUSE_BUTTON ) )
            {
                e.getComponent().setCursor( Cursor.getDefaultCursor() );
                m_xDown = -1;
                m_yDown = -1;
            }
        }
    }
}