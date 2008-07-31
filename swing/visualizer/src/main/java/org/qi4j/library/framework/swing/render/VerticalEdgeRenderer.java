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
package org.qi4j.library.framework.swing.render;

import prefuse.render.EdgeRenderer;
import prefuse.visual.VisualItem;
import prefuse.visual.EdgeItem;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.Set;
import java.util.HashSet;
import org.qi4j.library.framework.swing.GraphConstants;

/**
 * @author Sonny Gill
 */
public class VerticalEdgeRenderer
    extends EdgeRenderer
{

    private Set<Rectangle2D> drawnEdges = new HashSet<Rectangle2D>();

    protected Shape getRawShape( VisualItem item )
    {

        EdgeItem edge = (EdgeItem) item;
        VisualItem sourceNode = edge.getSourceItem();
        VisualItem targetNode = edge.getTargetItem();

        String edgeName = sourceNode.get( GraphConstants.FIELD_NAME ) + " - " + targetNode.get( GraphConstants.FIELD_NAME );

        Rectangle2D bounds1 = sourceNode.getBounds();
        Rectangle2D bounds2 = targetNode.getBounds();
        Line2D line = getConnectingLine( bounds1, bounds2 );
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

    private Line2D getConnectingLine( Rectangle2D bounds1, Rectangle2D bounds2 )
    {
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
        else if( right1 > left2 )
        {
            x = left2 + ( ( right1 - left2 ) / 2 );
            minX = left2;
            maxX = right1;
        }
        else if( right2 > left1 )
        {
            x = left1 + ( ( right2 - left1 ) / 2 );
            minX = left1;
            maxX = right2;
        }

        if( x != -1 )
        {
            return getLine( x, y1, y2, minX, maxX );
        }

        return null;
    }

    private boolean edgeIntersectsAlreadyDrawnEdges( Rectangle2D rect )
    {
        for( Rectangle2D edge : drawnEdges )
        {
            if( rect.intersects( edge ) )
            {
                return true;
            }
        }
        return false;
    }

    private Rectangle2D getRectangleAround( Line2D line )
    {
        double x = line.getX1();
        double y1 = line.getY1();
        double y2 = line.getY2();
        return new Rectangle2D.Double( x - 5, y1, 10, y2 - y1 );
    }

    private Line2D getLine( final double x, final double y1, final double y2, double minX, double maxX )
    {
        Line2D.Double line = new Line2D.Double( x, y1, x, y2 );
        Rectangle2D rect = getRectangleAround( line );
        boolean goLeft = true;
        int tries = 0;
        double acceptedX = x;
        while( edgeIntersectsAlreadyDrawnEdges( rect ) )
        {
            ++tries;
            double nextLeft = x - tries * 10;
            double nextRight = x + tries * 10;
            if( nextLeft < minX && nextRight > maxX )
            {
                return null;
            }

            acceptedX = goLeft ? nextLeft : nextRight;
            goLeft = !goLeft;

            rect.setRect( acceptedX, rect.getY(), rect.getWidth(), rect.getHeight() );
        }

        drawnEdges.add( rect );
        line.setLine( acceptedX, y1, acceptedX, y2 );
        return line;
    }
}
