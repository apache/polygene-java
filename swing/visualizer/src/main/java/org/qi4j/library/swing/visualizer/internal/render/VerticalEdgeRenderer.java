/*
 * Copyright 2008 Sonny Gill. All Rights Reserved.
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
package org.qi4j.library.swing.visualizer.internal.render;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import prefuse.render.EdgeRenderer;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

/**
 * @author Sonny Gill
 */
public class VerticalEdgeRenderer
    extends EdgeRenderer
{

    private Map<EdgeItem, Rectangle2D> drawnEdges = new HashMap<EdgeItem, Rectangle2D>();

    // horizontal space for the rectangle that will contain each edge
    private static final int edgeRectWidth = 10;
    private static final int halfWidth = edgeRectWidth / 2;

    protected Shape getRawShape( VisualItem item )
    {
        Line2D line = getConnectingLine( (EdgeItem) item );
        if( line != null )
        {
            // create the arrow head
            // get starting and ending edge endpoints
            Point2D start = line.getP1();
            Point2D end = line.getP2();

            // create the arrow head shape
            AffineTransform at = getArrowTrans( start, end, m_curWidth );
            m_curArrow = at.createTransformedShape( m_arrowHead );

            return line;
        }

        return super.getRawShape( item );
    }

    private Line2D getConnectingLine( EdgeItem edge )
    {
        VisualItem sourceNode = edge.getSourceItem();
        VisualItem targetNode = edge.getTargetItem();

        Rectangle2D bounds1 = sourceNode.getBounds();
        Rectangle2D bounds2 = targetNode.getBounds();

        int left1 = (int) bounds1.getX();
        int right1 = (int) ( left1 + bounds1.getWidth() );
        int left2 = (int) bounds2.getX();
        int right2 = (int) ( left2 + bounds2.getWidth() );

        double x = -1;
        double minX = -1;
        double maxX = -1;
        double y1 = bounds1.getY() + bounds1.getHeight();
        double y2 = bounds2.getY();

        if( left1 >= left2 && right1 <= right2 )
        {
            // the top rectangle falls within the bottom rectangle, if projected vertically
            x = left1 + bounds1.getWidth() / 2;
            minX = left1;
            maxX = right1;
        }
        else if( left1 <= left2 && right1 >= right2 )
        {
            // the bottom rectangle falls within the top rectangle, if projected vertically
            x = left2 + bounds2.getWidth() / 2;
            minX = left2;
            maxX = right2;
        }
        else if( right1 > left2 && right2 > left1 )
        {
            // overlap between the left side of the top rectangle, and right side of the bottom rectangle
            x = left2 + ( ( right1 - left2 ) / 2 );
            minX = left2;
            maxX = right1;
        }
        else if( right2 > left1 && right1 > left2 )
        {
            // overlap between the right side of the top rectangle, and the left side of the bottom rectangle
            x = left1 + ( ( right2 - left1 ) / 2 );
            minX = left1;
            maxX = right2;
        }

        if( x != -1 )
        {
            return getLine( edge, x, y1, y2, minX, maxX );
        }

        return null;
    }

    private Rectangle2D getRectangleAround( Line2D line )
    {
        double x = line.getX1();
        double y1 = line.getY1();
        double y2 = line.getY2();
        return new Rectangle2D.Double( x - halfWidth, y1, edgeRectWidth, y2 - y1 );
    }

    private boolean edgeIntersectsAlreadyDrawnEdges( EdgeItem edge, Rectangle2D rect )
    {
        for( Map.Entry<EdgeItem, Rectangle2D> entry : drawnEdges.entrySet() )
        {
            if( !( entry.getKey().equals( edge ) ) && rect.intersects( entry.getValue() ) )
            {
                return true;
            }
        }
        return false;
    }

    private Line2D getLine( EdgeItem edge, final double x, final double y1, final double y2, double minX, double maxX )
    {
        Line2D.Double line = new Line2D.Double( x, y1, x, y2 );
        Rectangle2D rect = getRectangleAround( line );
        boolean goLeft = true;
        int tries = 0;
        double acceptedX = x;
        while( edgeIntersectsAlreadyDrawnEdges( edge, rect ) )
        {
            ++tries;
            double nextLeft = x - tries * edgeRectWidth;
            double nextRight = x + tries * edgeRectWidth;
            if( nextLeft < minX && nextRight > maxX )
            {
                return null;
            }

            acceptedX = goLeft ? nextLeft : nextRight;
            goLeft = !goLeft;

            rect.setRect( acceptedX, rect.getY(), rect.getWidth(), rect.getHeight() );
        }

        drawnEdges.put( edge, rect );
        line.setLine( acceptedX, y1, acceptedX, y2 );
        return line;
    }
}
