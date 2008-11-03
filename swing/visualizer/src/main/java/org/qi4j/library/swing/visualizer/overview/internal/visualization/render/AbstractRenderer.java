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
package org.qi4j.library.swing.visualizer.overview.internal.visualization.render;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_NAME;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_LEFT;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_TOP;
import prefuse.render.Renderer;
import prefuse.visual.VisualItem;

/**
 * @author Sonny Gill
 */
abstract class AbstractRenderer
    implements Renderer
{
    protected final void drawName( Graphics2D graphics, VisualItem item, int x, int y )
    {
        Font font = headerFont( item );

        Point headerLocation = headerLocation( item, x, y );

        String name = (String) item.get( FIELD_NAME );
        graphics.drawString( name, headerLocation.x, headerLocation.y );
    }

    protected Point headerLocation( VisualItem item, int x, int y )
    {
        Font headerFont = headerFont( item );
        FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics( headerFont );

        x = x + PADDING_LEFT;
        y = y + ( PADDING_TOP / 2 ) + fm.getHeight();

        return new Point( x, y );
    }

    protected Font headerFont( VisualItem item )
    {
        return item.getFont();
    }

    public final boolean locatePoint( Point2D p, VisualItem item )
    {
        Rectangle2D bounds = item.getBounds();
        return bounds.contains( p );
    }

    public final void setBounds( VisualItem item )
    {
        // no management of the bounds by default
    }
}