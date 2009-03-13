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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.Layout;
import prefuse.controls.ControlAdapter;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.ColorMap;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class BoxedGraphDisplay extends Display
{
    public static final Font FONT = FontLib.getFont("Tahoma",12);

    // create data description of labels, setting colors, fonts ahead of time
    private static final Schema LABEL_SCHEMA = PrefuseLib.getVisualItemSchema();
    static {
        LABEL_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
        LABEL_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(200));
        LABEL_SCHEMA.setDefault(VisualItem.FONT, FONT);
    }

    private static final String tree = "graph";
    private static final String treeNodes = "graph.nodes";
    private static final String treeEdges = "graph.edges";
    private static final String labels = "labels";
    private static final String labelName = "name";

    protected BoxedLayout boxedLayout;

    public BoxedGraphDisplay()
    {
        super(new Visualization());

        LabelRenderer labelRenderer = new LabelRenderer( labelName );
        //labelRenderer.setRenderType( AbstractShapeRenderer.RENDER_TYPE_FILL );
        labelRenderer.setVerticalAlignment( Constants.BOTTOM);
        labelRenderer.setHorizontalAlignment( Constants.LEFT );
        //labelRenderer.setRoundedCorner( 8, 8 );

        // set up the renderers - one for nodes and one for labels
        DefaultRendererFactory rf = new DefaultRendererFactory();
        rf.add(new InGroupPredicate(treeNodes), new NodeRenderer());
        rf.add(new InGroupPredicate(labels), labelRenderer);
        m_vis.setRendererFactory(rf);

        // border colors
        final ColorAction borderColor = new BorderColorAction(treeNodes);
        final ColorAction fillColor = new FillColorAction(treeNodes);

        // color settings
        ActionList colors = new ActionList();
        colors.add(fillColor);
        colors.add(borderColor);
        m_vis.putAction("colors", colors);

        // animate paint change
        //ActionList animatePaint = new ActionList(400);
        //animatePaint.add(new ColorAnimator(treeNodes));
        //animatePaint.add(new RepaintAction());
        //m_vis.putAction("animatePaint", animatePaint);

        // create the single filtering and layout action list
        boxedLayout = new BoxedLayout( tree );
        ActionList layout = new ActionList();
        layout.add(boxedLayout);
        layout.add(new LabelLayout(labels));
        layout.add(colors);
        layout.add(new RepaintAction());
        m_vis.putAction("layout", layout);

        // initialize our display
        Dimension size = new Dimension( 400,400);
        setPreferredSize( size );
        setSize( size );
        setItemSorter(new TreeDepthItemSorter(true));
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
        addControlListener( new WheelMouseControl());

        setDamageRedraw( false );
    }

    public void run (Graph graph)
    {
        // add the tree to the visualization
        m_vis.add(tree, graph);
        m_vis.setVisible(treeEdges, null, false);

        // make node interactive
        m_vis.setInteractive( treeNodes, null, true );

        // add labels to the visualization
        Predicate labelP = (Predicate)ExpressionParser.parse("VISIBLE()");
        m_vis.addDecorators(labels, treeNodes, labelP, LABEL_SCHEMA);

        run();
    }

    public void run()
    {
        // perform layout
        m_vis.run("layout");
    }

    public void zoomIn()
    {
        if (isTranformInProgress())
        {
            return;
        }
        boxedLayout.zoomIn();
        //m_vis.invalidateAll();
        m_vis.run( "layout" );
    }

    public void zoomOut()
    {
        if (isTranformInProgress())
        {
            return;
        }
        boxedLayout.zoomOut();
        //m_vis.invalidateAll();
        m_vis.run( "layout" );
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
     * Set fill colors for treemap nodes. Search items are colored
     * in pink, while normal nodes are shaded according to their
     * depth in the tree.
     */
    public class FillColorAction extends ColorAction {
        private ColorMap cmap = new ColorMap(
            ColorLib.getInterpolatedPalette(10,
                ColorLib.rgb(120,152,219), ColorLib.rgb(0,0,0)), 0, 9);

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
    public static class LabelLayout extends Layout
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
                setX(item, node, bounds.getX() + BoxedLayout.INSET );
                setY(item, node, bounds.getY() + BoxedLayout.INSET + 12 );
            }
        }
    } // end of inner class LabelLayout

    /**
     * A renderer for treemap nodes. Draws simple rectangles, but defers
     * the bounds management to the layout.
     */
    public static class NodeRenderer extends AbstractShapeRenderer {
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
        public void itemWheelMoved( VisualItem item, java.awt.event.MouseWheelEvent evt )
        {
            zoom(evt.getWheelRotation());
        }

        public void mouseWheelMoved( VisualItem item, java.awt.event.MouseWheelEvent evt )
        {
            zoom(evt.getWheelRotation());
        }

        private void zoom (int rotation)
        {
            if (rotation == 0)
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
    }

}