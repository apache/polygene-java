/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.envisage.graph;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import org.qi4j.envisage.event.LinkEvent;
import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.Layout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.FocusControl;
import prefuse.controls.PanControl;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.ColorMap;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;

/* package */ class StackedGraphDisplay
    extends GraphDisplay
{
    /* package */ static final Font FONT = FontLib.getFont( "Tahoma", 12 );

    // create data description of LABELS, setting colors, fonts ahead of time
    private static final Schema LABEL_SCHEMA = PrefuseLib.getVisualItemSchema();

    static
    {
        LABEL_SCHEMA.setDefault( VisualItem.INTERACTIVE, false );
        LABEL_SCHEMA.setDefault( VisualItem.TEXTCOLOR, ColorLib.rgb( 255, 255, 255 ) );
        LABEL_SCHEMA.setDefault( VisualItem.FONT, FONT );
    }

    private static final String LABELS = "labels";

    private static final String LAYOUT_ACTION = "layout";
    private static final String COLORS_ACTION = "colors";
    private static final String AUTO_PAN_ACTION = "autoPan";

    private static int OUTLINE_COLOR = ColorLib.rgb( 33, 115, 170 );
    private static int OUTLINE_FOCUS_COLOR = ColorLib.rgb( 255, 255, 255 );  // alternative color ColorLib.rgb(150,200,200);

    private StackedLayout stackedLayout;

    private Activity activity;

    /* package */ StackedGraphDisplay()
    {
        super( new Visualization() );

        setBackground( ColorLib.getColor( 0, 51, 88 ) );

        LabelRenderer labelRenderer = new LabelRenderer( NAME_LABEL );
        labelRenderer.setVerticalAlignment( Constants.BOTTOM );
        labelRenderer.setHorizontalAlignment( Constants.LEFT );

        EdgeRenderer usesRenderer = new EdgeRenderer( Constants.EDGE_TYPE_CURVE, Constants.EDGE_ARROW_FORWARD );
        usesRenderer.setHorizontalAlignment1( Constants.CENTER );
        usesRenderer.setHorizontalAlignment2( Constants.CENTER );
        usesRenderer.setVerticalAlignment1( Constants.BOTTOM );
        usesRenderer.setVerticalAlignment2( Constants.TOP );

        Predicate usesPredicate = (Predicate) ExpressionParser.parse( "ingroup('graph.edges') AND [" + USES_EDGES + "]==true", true );

        // set up the renderers - one for nodes and one for LABELS
        DefaultRendererFactory rf = new DefaultRendererFactory();
        rf.add( new InGroupPredicate( GRAPH_NODES ), new NodeRenderer() );
        rf.add( new InGroupPredicate( LABELS ), labelRenderer );
        rf.add( usesPredicate, usesRenderer );
        m_vis.setRendererFactory( rf );

        // border colors
        ColorAction borderColor = new BorderColorAction( GRAPH_NODES );
        ColorAction fillColor = new FillColorAction( GRAPH_NODES );

        // uses edge colors
        ItemAction usesColor = new ColorAction( GRAPH_EDGES, usesPredicate, VisualItem.STROKECOLOR, ColorLib.rgb( 50, 50, 50 ) );
        ItemAction usesArrow = new ColorAction( GRAPH_EDGES, usesPredicate, VisualItem.FILLCOLOR, ColorLib.rgb( 50, 50, 50 ) );

        // color settings
        ActionList colors = new ActionList();
        colors.add( fillColor );
        colors.add( borderColor );
        colors.add( usesColor );
        colors.add( usesArrow );
        m_vis.putAction( COLORS_ACTION, colors );

        ActionList autoPan = new ActionList();
        autoPan.add( colors );
        autoPan.add( new AutoPanAction() );
        autoPan.add( new RepaintAction() );
        m_vis.putAction( AUTO_PAN_ACTION, autoPan );

        // create the layout action list
        stackedLayout = new StackedLayout( GRAPH );
        ActionList layout = new ActionList();
        layout.add( stackedLayout );
        layout.add( new LabelLayout( LABELS ) );
        layout.add( autoPan );
        m_vis.putAction( LAYOUT_ACTION, layout );

        // initialize our display
        Dimension size = new Dimension( 400, 400 );
        setSize( size );
        setPreferredSize( size );
        setItemSorter( new ExtendedTreeDepthItemSorter( true ) );
        addControlListener( new HoverControl() );
        addControlListener( new FocusControl( 1, COLORS_ACTION ) );
        addControlListener( new WheelMouseControl() );
        addControlListener( new PanControl( true ) );
        addControlListener( new ItemSelectionControl() );

        setDamageRedraw( true );
    }

    @Override
    public void run( Graph graph )
    {
        // add the GRAPH to the visualization
        m_vis.add( GRAPH, graph );

        // hide edges
        Predicate edgesPredicate = (Predicate) ExpressionParser.parse( "ingroup('graph.edges') AND [" + USES_EDGES + "]==false", true );
        m_vis.setVisible( GRAPH_EDGES, edgesPredicate, false );

        m_vis.setInteractive( GRAPH_EDGES, null, false );

        // make node interactive
        m_vis.setInteractive( GRAPH_NODES, null, true );

        // add LABELS to the visualization
        Predicate labelP = (Predicate) ExpressionParser.parse( "VISIBLE()" );
        m_vis.addDecorators( LABELS, GRAPH_NODES, labelP, LABEL_SCHEMA );

        run();
    }

    @Override
    public void run()
    {
        if( isInProgress() )
        {
            return;
        }

        // perform layout
        m_vis.invalidate( GRAPH_NODES );
        activity = m_vis.run( LAYOUT_ACTION );
    }

    @Override
    public void setSelectedValue( Object object )
    {
        if( object == null )
        {
            return;
        }

        NodeItem item = null;

        Iterator iter = m_vis.items( GRAPH_NODES );
        while( iter.hasNext() )
        {
            NodeItem tItem = (NodeItem) iter.next();
            Object tObj = tItem.get( USER_OBJECT );
            if( tObj.equals( object ) )
            {
                item = tItem;
                break;
            }
        }

        if( item != null )
        {
            int depth = item.getDepth();
            boolean relayout = false;
            if( depth > stackedLayout.getZoom() )
            {
                stackedLayout.zoom( depth );
                relayout = true;
            }

            TupleSet ts = m_vis.getFocusGroup( Visualization.FOCUS_ITEMS );
            ts.setTuple( item );
            if( relayout )
            {
                run();
            }
            else
            {
                m_vis.run( AUTO_PAN_ACTION );
            }
        }
    }

    private void zoomIn()
    {
        if( isInProgress() )
        {
            return;
        }

        stackedLayout.zoomIn();
        run();
    }

    private void zoomOut()
    {
        if( isInProgress() )
        {
            return;
        }

        stackedLayout.zoomOut();
        run();
    }

    private boolean isInProgress()
    {
        if( isTranformInProgress() )
        {
            return true;
        }

        if( activity != null )
        {
            if( activity.isRunning() )
            {
                return true;
            }
        }

        return false;
    }

    // ------------------------------------------------------------------------
    /**
     * Set the stroke color for drawing border node outlines.
     */
    private static class BorderColorAction
        extends ColorAction
    {

        private BorderColorAction( String group )
        {
            super( group, VisualItem.STROKECOLOR );
        }

        @Override
        public int getColor( VisualItem item )
        {
            if( !( item instanceof NodeItem ) )
            {
                return 0;
            }
            NodeItem nItem = (NodeItem) item;
            if( m_vis.isInGroup( nItem, Visualization.FOCUS_ITEMS ) )
            {
                return OUTLINE_FOCUS_COLOR;
            }

            return OUTLINE_COLOR;
        }
    }

    /**
     * Set fill colors for treemap nodes. Normal nodes are shaded according to their
     * depth in the tree.
     */
    private static class FillColorAction
        extends ColorAction
    {
        private static final ColorMap CMAP = new ColorMap( new int[]
        {
            ColorLib.rgb( 11, 117, 188 ),
            ColorLib.rgb( 8, 99, 160 ),
            ColorLib.rgb( 5, 77, 126 ),
            ColorLib.rgb( 2, 61, 100 ),
            ColorLib.rgb( 148, 55, 87 )
        }, 0, 4 );

        private FillColorAction( String group )
        {
            super( group, VisualItem.FILLCOLOR );
        }

        @Override
        public int getColor( VisualItem item )
        {
            if( item instanceof NodeItem )
            {
                NodeItem nItem = (NodeItem) item;
                if( m_vis.isInGroup( nItem, Visualization.FOCUS_ITEMS ) )
                {
                    int c = CMAP.getColor( nItem.getDepth() );
                    return ColorLib.darker( c );
                }
                return CMAP.getColor( nItem.getDepth() );
            }
            else
            {
                return CMAP.getColor( 0 );
            }
        }
    } // end of inner class FillColorAction

    private static class HoverControl
        extends ControlAdapter
    {
        @Override
        public void itemEntered( VisualItem item, MouseEvent evt )
        {
            item.setStrokeColor( OUTLINE_FOCUS_COLOR );
            item.getVisualization().repaint();
        }

        @Override
        public void itemExited( VisualItem item, MouseEvent evt )
        {
            item.setStrokeColor( item.getEndStrokeColor() );
            item.getVisualization().repaint();
        }
    }

    /**
     * Set label positions. Labels are assumed to be DecoratorItem instances,
     * decorating their respective nodes. The layout simply gets the bounds
     * of the decorated node and assigns the label coordinates to the center
     * of those bounds.
     */
    private static class LabelLayout
        extends Layout
    {
        private LabelLayout( String group )
        {
            super( group );
        }

        @Override
        public void run( double frac )
        {
            Iterator iter = m_vis.items( m_group );
            while( iter.hasNext() )
            {
                DecoratorItem item = (DecoratorItem) iter.next();
                VisualItem node = item.getDecoratedItem();
                Rectangle2D bounds = node.getBounds();
                setX( item, node, bounds.getX() + StackedLayout.INSET );
                setY( item, node, bounds.getY() + StackedLayout.INSET + 12 );
            }
        }
    } // end of inner class LabelLayout

    /**
     * A renderer for treemap nodes. Draws simple rectangles, but defers
     * the bounds management to the layout.
     */
    private static class NodeRenderer
        extends AbstractShapeRenderer
    {
        private Rectangle2D m_bounds = new Rectangle2D.Double();

        private NodeRenderer()
        {
            m_manageBounds = false;
        }

        @Override
        protected Shape getRawShape( VisualItem item )
        {
            m_bounds.setRect( item.getBounds() );
            return m_bounds;
        }
    } // end of inner class NodeRenderer

    private class WheelMouseControl
        extends ControlAdapter
    {
        @Override
        public void itemWheelMoved( VisualItem item, MouseWheelEvent evt )
        {
            zoom( evt.getWheelRotation() );
        }

        @Override
        public void mouseWheelMoved( MouseWheelEvent evt )
        {
            zoom( evt.getWheelRotation() );
        }

        private void zoom( final int rotation )
        {
            SwingUtilities.invokeLater( new Runnable()
            {
                @Override
                public void run()
                {
                    if( rotation == 0 )
                    {
                        return;
                    }
                    if( rotation < 0 )
                    {
                        zoomOut();
                    }
                    else
                    {
                        zoomIn();
                    }
                }
            } );
        }
    }

    private class ItemSelectionControl
        extends ControlAdapter
    {
        @Override
        public final void itemClicked( VisualItem anItem, MouseEvent anEvent )
        {
            // update the display
            anItem.getVisualization().repaint();

            if( !anItem.canGet( USER_OBJECT, Object.class ) )
            {
                return;
            }

            Object object = anItem.get( USER_OBJECT );
            LinkEvent evt = new LinkEvent( StackedGraphDisplay.this, object );
            fireLinkActivated( evt );
        }
    }

    private class AutoPanAction
        extends Action
    {
        @Override
        public void run( double frac )
        {
            Rectangle2D displayBounds = new Rectangle2D.Double( 0, 0, getWidth(), getHeight() );

            Container container = getParent();
            if( container == null )
            {
                return;
            }

            // HACK check the container size
            if( container instanceof JViewport )
            {
                Dimension size = ( (JViewport) container ).getExtentSize();
                displayBounds.setRect( 0, 0, size.getWidth(), size.getHeight() );
            }
            else
            {
                Dimension size = ( (Component) container ).getSize();
                displayBounds.setRect( 0, 0, size.getWidth(), size.getHeight() );
            }

            Rectangle2D bounds = stackedLayout.getLayoutRoot().getBounds();

            // Pan center
            double x = ( displayBounds.getWidth() - bounds.getWidth() ) / 2;
            double y = ( displayBounds.getHeight() - bounds.getHeight() ) / 2;

            // reset the transform
            try
            {
                setTransform( new AffineTransform() );
            }
            catch( NoninvertibleTransformException ex )
            {
                return;
            }
            if( x < 0 )
            {
                x = 0;
            }

            if( y < 0 )
            {
                y = 0;
            }
            pan( x, y );

            TupleSet ts = m_vis.getFocusGroup( Visualization.FOCUS_ITEMS );
            if( ts.getTupleCount() != 0 )
            {
                // get the first selected item and pan center it
                VisualItem vi = (VisualItem) ts.tuples().next();

                //update scrollbar position
                if( container instanceof JViewport )
                {
                    // TODO there is a bug on Swing scrollRectToVisible
                    ( (JViewport) container ).scrollRectToVisible( vi.getBounds().getBounds() );
                }
            }
        }
    }

    /**
     * ExtenedTreeDepthItemSorter to alter the default ordering/sorter,
     * to make sure Edge item is drawn in front. This is for Edge Uses
     */
    private static class ExtendedTreeDepthItemSorter
        extends TreeDepthItemSorter
    {
        private ExtendedTreeDepthItemSorter()
        {
            this( false );
        }

        private ExtendedTreeDepthItemSorter( boolean childrenAbove )
        {
            super( childrenAbove );
        }

        @Override
        public int score( VisualItem item )
        {
            int score = super.score( item );
            if( item instanceof EdgeItem )
            {
                // make it drawn in front of NODE
                score = ( 1 << ( 25 + EDGE + NODE ) );
            }

            return score;
        }
    }

}
