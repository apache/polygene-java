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
package org.qi4j.library.swing.envisage.graph;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import org.qi4j.library.swing.envisage.event.LinkEvent;
import org.qi4j.library.swing.envisage.event.LinkListener;
import prefuse.Constants;
import prefuse.Display;
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

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class StackedGraphDisplay extends Display
{
    public static final Font FONT = FontLib.getFont("Tahoma",12);

    // create data description of LABELS, setting colors, fonts ahead of time
    static final Schema LABEL_SCHEMA = PrefuseLib.getVisualItemSchema();
    static {
        LABEL_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
        LABEL_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(220));
        LABEL_SCHEMA.setDefault(VisualItem.FONT, FONT);
    }

    static final String USER_OBJECT = "userObject";
    static final String NAME_LABEL = "name";
    static final String LABELS = "labels";

    static final String GRAPH = "graph";
    static final String GRAPH_NODES = "graph.nodes";
    static final String GRAPH_EDGES = "graph.edges";
    static final String USES_EDGES = "uses.edges";

    static final String LAYOUT_ACTION = "layout";
    static final String COLORS_ACTION = "colors";

    protected StackedLayout stackedLayout;

    protected Activity activity;

    public StackedGraphDisplay()
    {
        super(new Visualization());

        LabelRenderer labelRenderer = new LabelRenderer( NAME_LABEL );
        labelRenderer.setVerticalAlignment( Constants.BOTTOM);
        labelRenderer.setHorizontalAlignment( Constants.LEFT );

        EdgeRenderer usesRenderer = new EdgeRenderer( Constants.EDGE_TYPE_CURVE, Constants.EDGE_ARROW_FORWARD );
        usesRenderer.setHorizontalAlignment1( Constants.CENTER );
        usesRenderer.setHorizontalAlignment2( Constants.CENTER );
        usesRenderer.setVerticalAlignment1( Constants.BOTTOM);
        usesRenderer.setVerticalAlignment2( Constants.TOP);

        Predicate usesPredicate = (Predicate) ExpressionParser.parse( "ingroup('graph.edges') AND [" + USES_EDGES + "]==true", true );

        // set up the renderers - one for nodes and one for LABELS
        DefaultRendererFactory rf = new DefaultRendererFactory();
        rf.add(new InGroupPredicate( GRAPH_NODES ), new NodeRenderer());
        rf.add(new InGroupPredicate( LABELS ), labelRenderer);
        rf.add( usesPredicate, usesRenderer );
        m_vis.setRendererFactory(rf);

        // border colors
        final ColorAction borderColor = new BorderColorAction( GRAPH_NODES );
        final ColorAction fillColor = new FillColorAction( GRAPH_NODES );

        // uses edge colors
        ItemAction usesColor = new ColorAction( GRAPH_EDGES, usesPredicate, VisualItem.STROKECOLOR, ColorLib.rgb( 50,50,50 ) );
        ItemAction usesArrow = new ColorAction( GRAPH_EDGES, usesPredicate, VisualItem.FILLCOLOR, ColorLib.rgb( 50, 50, 50 ) );

        // color settings
        ActionList colors = new ActionList();
        colors.add(fillColor);
        colors.add(borderColor);
        colors.add(usesColor);
        colors.add(usesArrow);
        m_vis.putAction(COLORS_ACTION, colors);

        // create the layout action list
        stackedLayout = new StackedLayout( GRAPH );
        ActionList layout = new ActionList();
        layout.add( stackedLayout );
        layout.add(new LabelLayout( LABELS ));
        layout.add(new AutoPanAction());
        layout.add(colors);
        layout.add(new RepaintAction());
        m_vis.putAction(LAYOUT_ACTION, layout);

        // initialize our display
        Dimension size = new Dimension( 400,400);
        setSize( size );
        setPreferredSize( size );
        setItemSorter(new ExtendedTreeDepthItemSorter(true));
        addControlListener(new ControlAdapter() {
            public void itemEntered(VisualItem item, MouseEvent e) {
                item.setStrokeColor(borderColor.getColor(item));
                item.getVisualization().repaint();
            }
            public void itemExited(VisualItem item, MouseEvent e) {
                item.setStrokeColor(item.getEndStrokeColor());
                item.getVisualization().repaint();
            }
        });
        addControlListener( new FocusControl( 1, COLORS_ACTION ) );
        addControlListener( new WheelMouseControl());
        addControlListener( new PanControl(true) );
        addControlListener( new ItemSelectionControl() );

        setDamageRedraw( false );
    }

    public void run (Graph graph)
    {
        // add the GRAPH to the visualization
        m_vis.add( GRAPH, graph);

        // hide edges
        Predicate edgesPredicate = (Predicate) ExpressionParser.parse( "ingroup('graph.edges') AND [" + USES_EDGES + "]==false", true );
        m_vis.setVisible( GRAPH_EDGES, edgesPredicate, false);

        m_vis.setInteractive( GRAPH_EDGES, null, false );

        // make node interactive
        m_vis.setInteractive( GRAPH_NODES, null, true );

        // add LABELS to the visualization
        Predicate labelP = (Predicate)ExpressionParser.parse("VISIBLE()");
        m_vis.addDecorators( LABELS, GRAPH_NODES, labelP, LABEL_SCHEMA);

        run();
    }

    public void run()
    {
        if (isInProgress())
        {
            return;
        }

        // perform layout
        //m_vis.invalidate( GRAPH_NODES );
        activity = m_vis.run(LAYOUT_ACTION);
    }

    public void zoomIn()
    {
        if (isInProgress())
        {
            return;
        }

        stackedLayout.zoomIn();
        run();
    }

    public void zoomOut()
    {
        if (isInProgress())
        {
            return;
        }

        stackedLayout.zoomOut();
        run();
    }

    protected boolean isInProgress()
    {
        if (isTranformInProgress())
        {
            return true;
        }

        if (activity != null)
        {
            if (activity.isRunning())
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Add a listener that's notified each time a change to the selection occurs.
     *
     * @param listener the LinkListener to add
     */
    public void addLinkListener( LinkListener listener )
    {
        listenerList.add( LinkListener.class, listener );
    }

    /**
     * Remove a listener from the list that's notified each time a change to the selection occurs.
     *
     * @param listener the LinkListener to remove
     */
    public void removeLinkListener( LinkListener listener )
    {
        listenerList.remove( LinkListener.class, listener );
    }

    protected void fireLinkActivated( LinkEvent evt )
    {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for( int i = listeners.length - 2; i >= 0; i -= 2 )
        {
            if( listeners[ i ] == LinkListener.class )
            {
                ( (LinkListener) listeners[ i + 1 ] ).activated( evt );
            }
        }
    }

    // ------------------------------------------------------------------------

    /**
     * Set the stroke color for drawing treemap node outlines. A graded
     * grayscale ramp is used, with higer nodes in the tree drawn in
     * lighter shades of gray.
     */
    public class BorderColorAction extends ColorAction {

        public BorderColorAction(String group) {
            super(group, VisualItem.STROKECOLOR);
        }

        public int getColor(VisualItem item) {
            if (!(item instanceof NodeItem))
            {
                return 0;
            }
            NodeItem nitem = (NodeItem)item;
            if ( nitem.isHover() )
                //return ColorLib.rgb(99,130,191);
            return ColorLib.rgb(150,200,200);

            int depth = nitem.getDepth();
            if ( depth < 2 ) {
                return ColorLib.gray(100);
            } else if ( depth < 4 ) {
                return ColorLib.gray(75);
            } else {
                return ColorLib.gray(50);
            }
        }
    }

    /**
     * Set fill colors for treemap nodes. Normal nodes are shaded according to their
     * depth in the tree.
     */
    public class FillColorAction extends ColorAction {
        private ColorMap cmap = new ColorMap(
            ColorLib.getInterpolatedPalette(10,
                ColorLib.rgb(120,152,219), ColorLib.rgb(10,10,10)), 0, 9);

        public FillColorAction(String group) {
            super(group, VisualItem.FILLCOLOR);
        }

        public int getColor( VisualItem item )
        {
            if( item instanceof NodeItem )
            {
                NodeItem nitem = (NodeItem) item;
                return cmap.getColor( nitem.getDepth() );
            }
            else
            {
                return cmap.getColor( 0 );
            }
        }

    } // end of inner class TreeMapColorAction

    /**
     * Set label positions. Labels are assumed to be DecoratorItem instances,
     * decorating their respective nodes. The layout simply gets the bounds
     * of the decorated node and assigns the label coordinates to the center
     * of those bounds.
     */
    public class LabelLayout extends Layout
    {
        public LabelLayout(String group) {
            super(group);
        }
        public void run(double frac) {
            Iterator iter = m_vis.items(m_group);
            while ( iter.hasNext() ) {
                DecoratorItem item = (DecoratorItem)iter.next();
                VisualItem node = item.getDecoratedItem();
                Rectangle2D bounds = node.getBounds();
                setX(item, node, bounds.getX() + StackedLayout.INSET );
                setY(item, node, bounds.getY() + StackedLayout.INSET + 12 );
            }
        }
    } // end of inner class LabelLayout

    /**
     * A renderer for treemap nodes. Draws simple rectangles, but defers
     * the bounds management to the layout.
     */
    public class NodeRenderer extends AbstractShapeRenderer {
        private Rectangle2D m_bounds = new Rectangle2D.Double();

        public NodeRenderer() {
            m_manageBounds = false;
        }
        protected Shape getRawShape(VisualItem item) {
            m_bounds.setRect(item.getBounds());
            return m_bounds;
        }
    } // end of inner class NodeRenderer

    public class WheelMouseControl extends ControlAdapter
    {
        public void itemWheelMoved( VisualItem item, MouseWheelEvent evt )
        {
            zoom(evt.getWheelRotation());
        }

        public void mouseWheelMoved( MouseWheelEvent evt )
        {
            zoom(evt.getWheelRotation());
        }

        private void zoom( final int rotation )
        {
            SwingUtilities.invokeLater( new Runnable()
            {
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

    public class ItemSelectionControl extends ControlAdapter
    {
        public final void itemClicked( VisualItem anItem, MouseEvent anEvent )
        {
            if( !anItem.canGet( USER_OBJECT, Object.class ) )
            {
                return;
            }
            Object object = anItem.get( USER_OBJECT );
            LinkEvent evt = new LinkEvent( this, object );
            fireLinkActivated( evt );
        }
    }

   public class AutoPanAction extends Action
   {
        public void run( double frac )
        {
            //panAbs( 200, 0 );

            Rectangle2D displayBounds = new Rectangle2D.Double(0,0,getWidth(),getHeight());

            Container container = getParent();
            if (container == null)
            {
                return;
            }

            // HACK check the container size
            if (container instanceof JViewport)
            {
                Dimension size = ((JViewport)container).getExtentSize();
                displayBounds.setRect( 0,0, size.getWidth(), size.getHeight());
            }
            else
            {
                Dimension size = ((Component)container).getSize();
                displayBounds.setRect( 0,0, size.getWidth(), size.getHeight());
            }


            Rectangle2D bounds = stackedLayout.getLayoutRoot().getBounds();

            // Pan center
            double x = (displayBounds.getWidth() - bounds.getWidth()) / 2;
            double y = (displayBounds.getHeight() - bounds.getHeight()) / 2;

            // reset the transform
            try
            {
                setTransform( new AffineTransform( ) );
            }
            catch (Exception ex)
            {
                return;    
            }
            if (x < 0)
            {
                x = 0;   
            }

            if (y < 0)
            {
                y = 0;
            }
            pan (x, y);

        }
    }

    /**
     * ExtenedTreeDepthItemSorter to alter the default ordering/sorter,
     * to make sure Edge item is drawn in front. This is for Edge Uses
     *
     * */
    public class ExtendedTreeDepthItemSorter extends TreeDepthItemSorter
    {
        public ExtendedTreeDepthItemSorter()
        {
            this(false);
        }

        public ExtendedTreeDepthItemSorter(boolean childrenAbove)
        {
            super(childrenAbove);            
        }

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