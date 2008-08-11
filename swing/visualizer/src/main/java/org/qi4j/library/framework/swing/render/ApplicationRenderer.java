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

package org.qi4j.library.framework.swing.render;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.geom.Rectangle2D;
import prefuse.visual.VisualItem;

/**
 * This renderer only draws the Application box.
 */
public class ApplicationRenderer
    extends AbstractRenderer
{

    private Color borderColor = Color.black;
    private BasicStroke borderStroke = new BasicStroke( 6 );

    private Color fillColor = new Color( 233, 227, 211 );
    private Color textColor = new Color( 129, 127, 121 );

    public void render( Graphics2D g, VisualItem item )
    {
        Rectangle2D rect = item.getBounds();

        int x = (int) rect.getX();
        int y = (int) rect.getY();
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();

        // todo draw drop shadow
//        g.setPaint( borderColor );
//        g.setStroke( borderStroke );
//        g.drawRect( x, y, width, height );

        g.setPaint( fillColor );
        g.fillRoundRect( x + 3, y + 3, width - 6, height - 6, 5, 5 );

        g.setPaint( textColor );
        drawName( g, item, x, y );
    }

}
