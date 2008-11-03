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
import prefuse.visual.VisualItem;

final class LayerRenderer
    extends AbstractRenderer
{
    private static final Color FILL_COLOR = new Color( 0xFFFFFF );
    private static final Color TEXT_COLOR = new Color( 0x000000 );

    // todo drop shadow

    public final void render( Graphics2D g, VisualItem item )
    {
        Rectangle2D rect = item.getBounds();

        int x = (int) rect.getX();
        int y = (int) rect.getY();
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();

        g.setPaint( FILL_COLOR );
        g.fillRect( x, y, width, height );

        g.setPaint( TEXT_COLOR );
        drawName( g, item, x, y );
    }
}
