/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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

import java.awt.Color;
import static java.awt.Color.WHITE;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_LEFT;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.PADDING_TOP;
import prefuse.visual.VisualItem;

/**
 * This renderer only draws the Application box.
 *
 * @since 0.4
 */
final class ApplicationRenderer
    extends AbstractRenderer
{
    // Header
    private static final Color COLOR_HEADER_FILL = new Color( 0x71B910 );
    private static final Color COLOR_HEADER_TEXT = WHITE;
    private static final Color COLOR_HEADER_LINE1_SEPARATOR = new Color( 0x22580D );
    private static final Color COLOR_HEADER_LINE2_SEPARATOR = WHITE;
    private static final int HEADER_HEIGHT = 30;

    // BODY
    private static final Color COLOR_BODY_FILL = new Color( 0xD9D4C5 );

    public final void render( Graphics2D graphic, VisualItem item )
    {
        Rectangle2D rect = item.getBounds();

        int x = (int) rect.getX();
        int y = (int) rect.getY();
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();

        // Draw body
        graphic.setPaint( COLOR_BODY_FILL );
        graphic.fillRoundRect( x, y, width, height, 5, 5 );

        drawHeader( graphic, item );
    }

    private void drawHeader( Graphics2D graphic, VisualItem item )
    {
        Rectangle2D rect = item.getBounds();

        int x = (int) rect.getX();
        int y = (int) rect.getY();
        int width = (int) rect.getWidth();

        graphic.setPaint( COLOR_HEADER_FILL );
        graphic.fillRoundRect( x, y, width, HEADER_HEIGHT, 5, 5 );

        graphic.setPaint( COLOR_HEADER_TEXT );
        drawName( graphic, item, x, y );

        graphic.setPaint( COLOR_HEADER_LINE1_SEPARATOR );
        int lineY = y + HEADER_HEIGHT - 2;
        graphic.fillRect( x, lineY, width, 1 );

        graphic.setPaint( COLOR_HEADER_LINE2_SEPARATOR );
        lineY = lineY + 1;
        graphic.fillRect( x, lineY, width, 1 );
    }

    @Override
    protected final Point headerLocation( VisualItem item, int x, int y )
    {
        Font headerFont = headerFont( item );
        FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics( headerFont );

        x = x + PADDING_LEFT;
        y = y + ( PADDING_TOP / 2 ) + fm.getHeight();

        return new Point( x, y );
    }
}