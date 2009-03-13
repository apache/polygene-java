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
import java.awt.FontMetrics;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import prefuse.action.layout.graph.TreeLayout;
import prefuse.render.Renderer;
import prefuse.visual.NodeItem;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class BoxedLayout extends TreeLayout
{
    public static int INSET = 10;

    public BoxedLayout(String group) {
        super(group);
    }

    public void run(double frac) {
        // setup
        NodeItem root = getLayoutRoot();
        //setItemMinSize(root);

        setItemLocation(root,0,0);

        Rectangle2D bounds = root.getBounds();
        this.getVisualization().getDisplay( 0 ).setSize( (int)bounds.getX(), (int)bounds.getY() );

        //Rectangle2D b = getLayoutBounds();
        /*m_r.setRect(b.getX(), b.getY(), b.getWidth()-1, b.getHeight()-1);

        // process size values
        computeAreas(root);

        // layout root node
        setX(root, null, 0);
        setY(root, null, 0);
        root.setBounds(0, 0, m_r.getWidth(), m_r.getHeight());

        // layout the tree
        updateArea(root, m_r);
        layout(root, m_r);
        */
    }

    protected void setItemMinSize(NodeItem parent)
    {
        Dimension minSize = new Dimension(0,0);
        Area area = new Area();
        for (int i=0; i<parent.getChildCount(); i++)
        {
            NodeItem node = (NodeItem)parent.getChild( i );
            setItemMinSize (node);
            area.add( new Area(node.getBounds()) );
        }

        // update parent Size
        if (!area.isEmpty())
        {
            Rectangle2D bounds = area.getBounds2D();
            parent.setBounds( 0,0, INSET + bounds.getWidth() + INSET, INSET + bounds.getHeight() + INSET  );
        } else {
            minSize = getItemMinSize( parent, minSize );
            parent.setBounds( 0,0, INSET + minSize.width + INSET, INSET + minSize.height + INSET );            
        }

    }

    protected Dimension getItemMinSize(NodeItem node, Dimension minSize)
    {
        if (minSize == null)
        {
            minSize = new Dimension(0,0);
        }

        String label = node.getString( "name" );
        FontMetrics fm = Renderer.DEFAULT_GRAPHICS.getFontMetrics( BoxedGraphDisplay.FONT );
        int width = fm.stringWidth( label);
        int height = fm.getHeight();
        minSize.setSize( width + INSET + INSET,height + INSET + INSET );

        //System.out.println(fm.getAscent());

        return minSize; 
    }

    protected void setItemLocation(NodeItem parent, double x, double y)
    {
        Dimension minSize = getItemMinSize( parent, null );
        parent.setBounds( x,y, minSize.width , minSize.height);

        double cx = x + INSET;
        double cy = y + minSize.height;

        int depth = parent.getDepth();
        Area area = new Area(parent.getBounds());

        boolean hasChild = false;
        for (int i=0; i<parent.getChildCount(); i++)
        {
            hasChild = true;
            NodeItem node = (NodeItem)parent.getChild( i );
            setItemLocation (node,cx,cy);
            area.add( new Area(node.getBounds()) );

            // shifting the location calculation
            Rectangle2D nodeRect = node.getBounds();
            //int depth = node.getDepth();
            if( depth == 0 )
            {
                // layer
                cy = cy + (INSET * 2) + nodeRect.getHeight();
            }
            if( depth == 1 )
            {
                // module
                cx = cx + INSET + nodeRect.getWidth();
            }
            else if( depth == 2 )
            {
                // type container
                cx = cx + INSET + nodeRect.getWidth();
            }
            else if( depth == 3 )
            {
                // type
                cy = cy +  INSET + nodeRect.getHeight();
            }
            /*else if( depth == 4 )
            {

            }*/
        }

        Rectangle2D bounds = area.getBounds2D();
        if (hasChild)
        {
            bounds.setRect( x,y, bounds.getWidth() + INSET, bounds.getHeight() + INSET );
        }

        parent.setBounds( x,y, bounds.getWidth(), bounds.getHeight() );

        // relayout the child so it have consistent width or height
        //int depth = parent.getDepth();
        if( depth == 0 )
        {
            arrangeChildVertically( parent );
        }
        else if( depth == 1 )
        {
            arrangeChildHorizontally( parent );
        }
        else if( depth == 2 )
        {
            arrangeChildHorizontally( parent );
        }
        else if( depth == 3 )
        {
            arrangeChildVertically( parent );
        }
        /*else if( depth == 4 )
        {
            arrangeChildVertically( parent );
        }*/
    }

    private void arrangeChildVertically(NodeItem parent)
    {
        double max = 0;
        for (int i=0; i<parent.getChildCount(); i++)
        {
            NodeItem node = (NodeItem)parent.getChild( i );
            Rectangle2D bounds = node.getBounds();
            max = Math.max( max, bounds.getWidth() );
        }

        for (int i=0; i<parent.getChildCount(); i++)
        {
            NodeItem node = (NodeItem)parent.getChild( i );
            Rectangle2D bounds = node.getBounds();
            node.setBounds( bounds.getX(), bounds.getY(), max, bounds.getHeight()  );
        }
    }

    private void arrangeChildHorizontally(NodeItem parent)
    {
        double max = 0;
        for (int i=0; i<parent.getChildCount(); i++)
        {
            NodeItem node = (NodeItem)parent.getChild( i );
            Rectangle2D bounds = node.getBounds();
            max = Math.max( max, bounds.getHeight() );
        }

        for (int i=0; i<parent.getChildCount(); i++)
        {
            NodeItem node = (NodeItem)parent.getChild( i );
            Rectangle2D bounds = node.getBounds();
            node.setBounds( bounds.getX(), bounds.getY(), bounds.getWidth(), max );
        }
    }
}
