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
package org.qi4j.library.framework.swing;

import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import prefuse.visual.VisualItem;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;

/**
 * @author Sonny Gill
 */
abstract class AbstractRenderer
    implements Renderer
{

    private boolean debug = false;

    protected void drawName( Graphics2D g, VisualItem item, int x, int y )
    {
        Font font = item.getFont();
        FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics( font );
        x = x + GraphConstants.paddingLeft;
        y = y + GraphConstants.paddingTop + fm.getHeight();
        String name = (String) item.get( GraphConstants.NAME );

        int color = item.getTextColor();
        g.setPaint( ColorLib.getColor( color ) );
        g.drawString( name, x, y );

        if( debug )
        {
            Rectangle2D rect = item.getBounds();
            String s = ( (int) rect.getX() ) + ", " + ( (int) rect.getY() ) + "," +
                       ( (int) rect.getWidth() ) + "," + ( (int) rect.getHeight() );

            g.drawString( s, x, y - GraphConstants.paddingTop );
        }
    }

    public boolean locatePoint( Point2D p, VisualItem item )
    {
        return item.getBounds().contains( p );
    }

    public void setBounds( VisualItem item )
    {
        //todo ??
        System.out.println( "setBounds: " + item );
    }

}
