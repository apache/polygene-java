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
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants;
import org.qi4j.library.swing.visualizer.overview.internal.common.NodeType;
import prefuse.visual.VisualItem;

final class ObjectRenderer
    extends AbstractRenderer
{
    private static final Color FILL_COLOR_SERVICE = new Color( 0x524E10 );
    private static final Color FILL_COLOR_ENTITY = new Color( 0x52382A );
    private static final Color FILL_COLOR_COMPOSITE = new Color( 0x3C4A1E );
    private static final Color FILL_COLOR_OBJECT = new Color( 0x254147 );

    private static final Color COLOR_TEXT = WHITE;

    public final void render( Graphics2D g, VisualItem item )
    {
        /*if (!isRenderable( g, item )) {
            return;
        }*/

        Rectangle2D rect = item.getBounds();
        int x = (int) rect.getX();
        int y = (int) rect.getY();
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();

        if (width == 0 || height == 0) {
            return; 
        }

        g.setPaint( fillColor( item ) );
        g.fillRoundRect( x, y, width, height, 3, 3 );

        g.setPaint( COLOR_TEXT );
        drawName( g, item, x, y );
    }

    private Paint fillColor( VisualItem item )
    {
        // Focus color: TODO
//        item.isInGroup( Visualization.FOCUS_ITEMS );

        NodeType nodeType = (NodeType) item.get( GraphConstants.FIELD_TYPE );
        switch( nodeType )
        {
        case SERVICE:
            return FILL_COLOR_SERVICE;
        case ENTITY:
            return FILL_COLOR_ENTITY;
        case COMPOSITE:
            return FILL_COLOR_COMPOSITE;
        case OBJECT:
            return FILL_COLOR_OBJECT;
        }

        return BLACK;
    }
}
