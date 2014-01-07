/*
 * Copyright (c) 2009, Tony Kohar. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.envisage.graph;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import prefuse.Display;
import prefuse.action.layout.graph.TreeLayout;
import prefuse.render.Renderer;
import prefuse.visual.NodeItem;

/* package */ final class StackedLayout
    extends TreeLayout
{
    /* package */ static int INSET = 10;

    private int zoom = 2;

    /* package */ StackedLayout( String group )
    {
        super( group );
    }

    @Override
    public void run( double frac )
    {
        // setup
        NodeItem root = getLayoutRoot();
        layout( root, 0, 0 );

        Rectangle2D bounds = root.getBounds();
        Display display = this.getVisualization().getDisplay( 0 );
        Dimension size = new Dimension( (int) bounds.getWidth(), (int) bounds.getHeight() );
        display.setSize( size );

        if( !display.isValid() )
        {
            display.validate();
        }
    }

    /* package */ void zoomOut()
    {
        zoom--;
        if( zoom < 1 )
        {
            zoom = 1;
        }
    }

    /* package */ void zoomIn()
    {
        zoom++;
        if( zoom > 4 )
        {
            zoom = 4;
        }
    }

    /* package */ void zoom( int zoom )
    {
        this.zoom = zoom;
    }

    /* package */ int getZoom()
    {
        return zoom;
    }

    private Dimension getItemMinSize( NodeItem node, Dimension minSize )
    {
        if( minSize == null )
        {
            minSize = new Dimension( 0, 0 );
        }

        String label = node.getString( "name" );
        FontMetrics fm = Renderer.DEFAULT_GRAPHICS.getFontMetrics( StackedGraphDisplay.FONT );
        int width = fm.stringWidth( label );
        int height = fm.getHeight();
        minSize.setSize( width + INSET + INSET, height + INSET + INSET );

        //System.out.println(fm.getAscent());
        return minSize;
    }

    private void layout( NodeItem node, double x, double y )
    {
        Dimension minSize = getItemMinSize( node, null );
        node.setBounds( x, y, minSize.width, minSize.height );

        int depth = node.getDepth();

        if( depth > zoom )
        {
            //System.out.println("depth: " +  depth + "  zoom: " + zoom);
            node.setBounds( x, y, 0, 0 );
            node.setVisible( false );
        }
        else
        {
            node.setVisible( true );
        }

        double cx = x + INSET;
        double cy = y + minSize.height;

        Area area = new Area( node.getBounds() );

        boolean hasChild = false;
        for( int i = 0; i < node.getChildCount(); i++ )
        {
            hasChild = true;
            NodeItem child = (NodeItem) node.getChild( i );

            layout( child, cx, cy );
            area.add( new Area( child.getBounds() ) );

            // shifting location calculation
            Rectangle2D nodeRect = child.getBounds();
            if( depth == 0 )
            {
                // layer
                cy = cy + ( INSET * 2 ) + nodeRect.getHeight();
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
                cy = cy + INSET + nodeRect.getHeight();
            }
        }

        Rectangle2D bounds = area.getBounds2D();
        if( hasChild && depth <= zoom )
        {
            bounds.setRect( x, y, bounds.getWidth() + INSET, bounds.getHeight() + INSET );
        }

        node.setBounds( x, y, bounds.getWidth(), bounds.getHeight() );

        // relayout the child so it have consistent width or height
        //int depth = parent.getDepth();
        if( depth == 0 )
        {
            arrangeChildVertically( node );
        }
        else if( depth == 1 )
        {
            arrangeChildHorizontally( node );
        }
        else if( depth == 2 )
        {
            arrangeChildHorizontally( node );
        }
        else if( depth == 3 )
        {
            arrangeChildVertically( node );
        }
    }

    private void arrangeChildVertically( NodeItem parent )
    {
        double maxW = 0;
        for( int i = 0; i < parent.getChildCount(); i++ )
        {
            NodeItem node = (NodeItem) parent.getChild( i );
            Rectangle2D bounds = node.getBounds();
            maxW = Math.max( maxW, bounds.getWidth() );
        }

        for( int i = 0; i < parent.getChildCount(); i++ )
        {
            NodeItem node = (NodeItem) parent.getChild( i );
            Rectangle2D bounds = node.getBounds();
            node.setBounds( bounds.getX(), bounds.getY(), maxW, bounds.getHeight() );
        }
    }

    private void arrangeChildHorizontally( NodeItem parent )
    {
        double maxH = 0;
        for( int i = 0; i < parent.getChildCount(); i++ )
        {
            NodeItem node = (NodeItem) parent.getChild( i );
            Rectangle2D bounds = node.getBounds();
            maxH = Math.max( maxH, bounds.getHeight() );
        }

        for( int i = 0; i < parent.getChildCount(); i++ )
        {
            NodeItem node = (NodeItem) parent.getChild( i );
            Rectangle2D bounds = node.getBounds();
            node.setBounds( bounds.getX(), bounds.getY(), bounds.getWidth(), maxH );
        }
    }
}
