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
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import prefuse.Visualization;
import prefuse.visual.VisualItem;

final class ObjectRenderer
    extends AbstractRenderer
{

    private Color fillColor = new Color( 129, 127, 121 );
    private Color textColor = Color.white;
    private Color focusColor = new Color( 129, 200, 121 );

    public final void render( Graphics2D g, VisualItem item )
    {
        Rectangle2D rect = item.getBounds();
        int x = (int) rect.getX();
        int y = (int) rect.getY();
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();

        if( item.isInGroup( Visualization.FOCUS_ITEMS ) )
        {
            g.setPaint( focusColor );
        }
        else
        {
            g.setPaint( fillColor );
        }
        g.fillRoundRect( x, y, width, height, 3, 3 );

        g.setPaint( textColor );
        drawName( g, item, x, y );
    }
}
