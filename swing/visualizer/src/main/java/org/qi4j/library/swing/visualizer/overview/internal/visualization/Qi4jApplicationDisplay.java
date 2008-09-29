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

import java.awt.event.ActionEvent;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.listener.SelectionListener;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_TYPE;
import org.qi4j.library.swing.visualizer.overview.internal.common.NodeType;
import static org.qi4j.library.swing.visualizer.overview.internal.common.NodeType.APPLICATION;
import prefuse.Display;
import prefuse.controls.ControlAdapter;
import static prefuse.util.display.DisplayLib.fitViewToBounds;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.sort.ItemSorter;

/**
 * TODO: Javadoc
 * TODO: Auto center when the window is resized
 *
 * @author Sonny Gill
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class Qi4jApplicationDisplay extends Display
{
    private static final int DEFAULT_ZOOM_ANIMATION_DURATION = 1000;
    private static final double DEFAULT_ZOOM_SCALE = 0.8;

    private Qi4jApplicationVisualization visualization;

    public Qi4jApplicationDisplay( SelectionListener aListener )
        throws IllegalArgumentException
    {
        validateNotNull( "aListener", aListener );

        visualization = new Qi4jApplicationVisualization();
        setVisualization( visualization );

        setItemSorter( new OverviewItemSorter() );
        addControlListener( new MouseWheelZoomControl() );
        addControlListener( new ItemSelectionControl( aListener ) );
        addKeyboardActions();
    }

    /**
     * Display the specified application descriptor.
     *
     * @param aDescriptor application descriptor.
     * @since 0.5
     */
    public final void display( ApplicationDetailDescriptor aDescriptor )
    {
        visualization.populate( aDescriptor );
        visualization.launch();
    }

    private void addKeyboardActions()
    {
        final int PREVIOUSLY_VISIBLE_AREA_WHEN_SCROLLING = 50;

        InputMap inputMap = getInputMap( WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        ActionMap actionMap = getActionMap();

        inputMap.put( getKeyStroke( '+' ), "zoomIn" );
        actionMap.put( "zoomIn", new AbstractAction( "Zoom In" )
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed( ActionEvent e )
            {
                zoomIn( getDisplayCenter(), null );
            }
        } );

        inputMap.put( getKeyStroke( '-' ), "zoomOut" );
        actionMap.put( "zoomOut", new AbstractAction( "Zoom Out" )
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed( ActionEvent e )
            {
                zoomOut( getDisplayCenter(), null );
            }
        } );

        inputMap.put( getKeyStroke( VK_LEFT, 0 ), "panLeft" );
        actionMap.put( "panLeft", new AbstractAction( "Pan Left" )
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed( ActionEvent e )
            {
                pan( getWidth() - PREVIOUSLY_VISIBLE_AREA_WHEN_SCROLLING, 0, true );
            }
        } );

        inputMap.put( getKeyStroke( VK_RIGHT, 0 ), "panRight" );
        actionMap.put( "panRight", new AbstractAction( "Pan Right" )
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed( ActionEvent e )
            {
                pan( PREVIOUSLY_VISIBLE_AREA_WHEN_SCROLLING - getWidth(), 0, true );
            }
        } );

        inputMap.put( getKeyStroke( VK_UP, 0 ), "panUp" );
        actionMap.put( "panUp", new AbstractAction( "Pan Up" )
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed( ActionEvent e )
            {
                pan( 0, getHeight() - PREVIOUSLY_VISIBLE_AREA_WHEN_SCROLLING, true );
            }
        } );

        inputMap.put( getKeyStroke( VK_DOWN, 0 ), "panDown" );
        actionMap.put( "panDown", new AbstractAction( "Pan Down" )
        {
            private static final long serialVersionUID = 1L;

            public void actionPerformed( ActionEvent e )
            {
                pan( 0, PREVIOUSLY_VISIBLE_AREA_WHEN_SCROLLING - getHeight(), true );
            }
        } );
    }


    @Override
    public final Qi4jApplicationVisualization getVisualization()
    {
        return (Qi4jApplicationVisualization) super.getVisualization();
    }

    public final void zoomToFitContainer()
    {
        Qi4jApplicationVisualization visualization = getVisualization();
        VisualItem applicationNodeItem = visualization.getApplicationNodeItem();
        Rectangle2D applicationNodeBounds = applicationNodeItem.getBounds();
        fitViewToBounds( this, applicationNodeBounds, DEFAULT_ZOOM_ANIMATION_DURATION );
    }

    public final void zoomToActualSize()
    {
        // TODO: Need to calculate boundary
        float scaleToActualSize = (float) ( 1 / getScale() );
        animateZoom( getDisplayCenter(), scaleToActualSize, DEFAULT_ZOOM_ANIMATION_DURATION );
    }

    public final void zoomIn( Point2D p, Double scale )
    {
        if( !isTranformInProgress() )
        {

            double displayScale = getScale();

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
                    zoom( p, zoomScale );
                }
                else
                {
                    animateZoom( p, zoomScale, DEFAULT_ZOOM_ANIMATION_DURATION );
                }
                repaint();
            }

        }
    }

    public final void zoomOut( Point2D p, Double scale )
    {
        if( !isTranformInProgress() )
        {
            Qi4jApplicationVisualization visualization = getVisualization();
            VisualItem applicationNodeItem = visualization.getApplicationNodeItem();

            Rectangle2D bounds = applicationNodeItem.getBounds();
            if( displaySizeFitsScaledBounds( this, bounds ) )
            {
                return;
            }

            double zoomScale = scale == null ? DEFAULT_ZOOM_SCALE : scale;
            double displayScale = getScale();

            int widthAfterZoom = (int) ( bounds.getWidth() * displayScale * zoomScale );
            int heightAfterZoom = (int) ( bounds.getHeight() * displayScale * zoomScale );

            if( widthAfterZoom <= getWidth() && heightAfterZoom <= getHeight() )
            {
                zoomToFitContainer();
            }
            else
            {
                if( scale != null )
                {
                    zoom( p, zoomScale );
                }
                else
                {
                    animateZoom( p, zoomScale, DEFAULT_ZOOM_ANIMATION_DURATION );
                }
                repaint();
            }
        }
    }

    private static boolean displaySizeFitsScaledBounds( Display display, Rectangle2D bounds )
    {
        double scale = display.getScale();
        return ( bounds.getWidth() * scale == display.getWidth() ) &&
               ( bounds.getHeight() * scale == display.getHeight() );
    }

    public final void pan( double dx, double dy, boolean isAnimate )
    {
        if( !isTranformInProgress() )
        {
            Qi4jApplicationVisualization visualization = getVisualization();
            VisualItem applicationNodeItem = visualization.getApplicationNodeItem();
            Rectangle2D bounds = applicationNodeItem.getBounds();
            AffineTransform at = getTransform();

            if( dx > 0 )
            {
                // panning left, mouse movement to right
                double scaledLeftX = bounds.getX() * at.getScaleX();    // Left bound of Bounding Box
                double distanceToLeftEdge = -( at.getTranslateX() ) - scaledLeftX;
                dx = Math.min( dx, distanceToLeftEdge );
            }
            else if( dx < 0 )
            {
                //panning right, mouse movement to left
                int scaledRightX = (int) ( bounds.getMaxX() * at.getScaleX() );    // Right bound of BB
                double distanceToRightEdge = getWidth() - scaledRightX - at.getTranslateX();
                dx = Math.max( dx, distanceToRightEdge );
            }

            if( dy > 0 )
            {
                // panning up, mouse movement towards the bottom of the panel
                int scaledTopY = (int) ( bounds.getY() * at.getScaleY() );    // Top bound of BB
                double distanceToTopEdge = -( at.getTranslateY() ) - scaledTopY;
                dy = Math.min( dy, distanceToTopEdge );

            }
            else if( dy < 0 )
            {
                // panning down, mouse movement towards the top of the panel
                int scaledBottomY = (int) ( bounds.getMaxY() * at.getScaleY() );    // Bottom bound of BB
                double distanceToBottomEdge = getHeight() - scaledBottomY - at.getTranslateY();
                dy = Math.max( dy, distanceToBottomEdge );
            }

            if( isAnimate )
            {
                animatePan( dx, dy, 500 );
            }
            else
            {
                pan( dx, dy );
            }
            repaint();
        }
    }

    public final Point2D getDisplayCenter()
    {
        float midWidth = getWidth() / 2;
        float midHeight = getHeight() / 2;
        return new Point2D.Float( midWidth, midHeight );
    }


    private class MouseWheelZoomControl extends ControlAdapter
    {
        @Override
        public void itemWheelMoved( VisualItem item, MouseWheelEvent e )
        {
            mouseWheelMoved( e );
        }

        @Override
        public void mouseWheelMoved( MouseWheelEvent e )
        {
            if( !isTranformInProgress() )
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

    private static class OverviewItemSorter extends ItemSorter
        implements Serializable
    {
        private static final long serialVersionUID = 1L;

        @Override
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
    }
}
