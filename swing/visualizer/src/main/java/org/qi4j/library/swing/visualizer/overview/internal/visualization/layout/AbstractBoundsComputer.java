/*
 * Copyright 2008 Edward Yakop. All Rights Reserved.
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
package org.qi4j.library.swing.visualizer.overview.internal.visualization.layout;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_NAME;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_BOTTOM;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_LEFT;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_TOP;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.hSpace;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.vSpace;
import prefuse.render.Renderer;
import prefuse.visual.NodeItem;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
abstract class AbstractBoundsComputer
{
    abstract Rectangle computeBounds( NodeItem node, Point location );

    Rectangle arrangeChildrenHorizontallyAndComputeBounds(
        NodeItem nodeItem, Point location, AbstractBoundsComputer childBoundsComputer )
    {
        Dimension dimension = getNodeLabelSize( nodeItem );
        int x = location.x + PADDING_LEFT;
        int y = location.y + PADDING_TOP + dimension.height + vSpace;

        Iterator children = nodeItem.children();
        int maxChildHeight = 0;
        while( children.hasNext() )
        {
            NodeItem child = (NodeItem) children.next();
            Point moduleLocation = new Point( x, y );
            Rectangle bounds = childBoundsComputer.computeBounds( child, moduleLocation );
            child.setBounds( bounds.x, bounds.y, bounds.width, bounds.height );

            x += bounds.width + hSpace;
            if( bounds.height > maxChildHeight )
            {
                maxChildHeight = bounds.height;
            }

        }

        if( x < location.x + dimension.width )
        {
            x = location.x + dimension.width;
        }

        int width = x - location.x;
        int height = ( y + maxChildHeight + PADDING_BOTTOM + vSpace ) - location.y;

        return new Rectangle( location.x, location.y, width, height );
    }


    Dimension getNodeLabelSize( NodeItem node )
    {
        Font font = node.getFont();
        FontMetrics fm = Renderer.DEFAULT_GRAPHICS.getFontMetrics( font );
        // 40 is arbitrarily selected, drawString takes more space than calculated here
        // this may be because the Graphics object is different from the one that is used to draw it
        int width = fm.stringWidth( getName( node ) ) + 40;
        int height = fm.getHeight() + 2;

        return new Dimension( width, height );
    }

    String getName( NodeItem node )
    {
        return (String) node.get( FIELD_NAME );
    }
}
