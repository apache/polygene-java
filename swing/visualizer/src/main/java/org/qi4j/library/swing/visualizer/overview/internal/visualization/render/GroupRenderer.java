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

import java.awt.BasicStroke;
import java.awt.Color;
import static java.awt.Color.BLACK;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_NAME;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.GROUP_NAME_COMPOSITES;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.GROUP_NAME_ENTITIES;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.GROUP_NAME_OBJECTS;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.GROUP_NAME_SERVICES;
import prefuse.visual.VisualItem;

/**
 * @author Sonny Gill
 * @author edward.yakop@gmail.com
 * @since 0.4
 */
final class GroupRenderer extends AbstractRenderer
{
    private Color BORDER_COLOR = new Color( 0x615E5E );
    private BasicStroke BORDER_STROKE = new BasicStroke( 1 );

    private static final Color FILL_COLOR_SERVICES = new Color( 0xEBE12E );
    private static final Color FILL_COLOR_COMPOSITES = new Color( 0xB7E25B );
    private static final Color FILL_COLOR_OBJECTS = new Color( 0x76C9E1 );
    private static final Color FILL_COLOR_ENTITIES = new Color( 0xEBA378 );

    private Color COLOR_HEADER_TEXT = BLACK;

    public final void render( Graphics2D g, VisualItem item )
    {
        Rectangle2D rect = item.getBounds();
        int x = (int) rect.getX();
        int y = (int) rect.getY();
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();

        // Draw border
        g.setPaint( BORDER_COLOR );
        g.setStroke( BORDER_STROKE );
        g.drawRoundRect( x, y, width, height, 3, 3 );

        // Draw fill
        Color fillColor = colorFill( item );
        g.setPaint( fillColor );
        g.fillRoundRect( x + 1, y + 1, width - 1, height - 1, 3, 3 );

        // Draw header text
        g.setPaint( COLOR_HEADER_TEXT );
        drawName( g, item, x, y );
    }

    private Color colorFill( VisualItem item )
    {
        String groupName = item.getString( FIELD_NAME );

        if( GROUP_NAME_SERVICES.equals( groupName ) )
        {
            return FILL_COLOR_SERVICES;
        }
        else if( GROUP_NAME_COMPOSITES.equals( groupName ) )
        {
            return FILL_COLOR_COMPOSITES;
        }
        else if( GROUP_NAME_OBJECTS.equals( groupName ) )
        {
            return FILL_COLOR_OBJECTS;
        }
        else if( GROUP_NAME_ENTITIES.equals( groupName ) )
        {
            return FILL_COLOR_ENTITIES;
        }

        return BLACK;
    }
}
