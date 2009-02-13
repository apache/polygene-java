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
import java.awt.geom.AffineTransform;
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
    // the zoom scale value
    protected double scale = 1;

    public void zoom(double scale) {
        this.scale = scale;
    }

    protected boolean isRenderable(Graphics2D graphics, VisualItem item) {
        boolean b = true;

        String name = (String) item.get( FIELD_NAME );
        if (name == null) {
            return b;
        }

        if (scale >= 1) { return b; }

        Rectangle2D rect = item.getBounds();

        double w = (rect.getWidth()  * scale) - (PADDING_LEFT + 5) ;
        double h = (rect.getHeight() *  scale) - 2 ;

        Font headerFont = headerFont( item );
        FontMetrics fm = graphics.getFontMetrics( headerFont );
        //String name = (String) item.get( FIELD_NAME );

        int sw = fm.stringWidth( name );

        if (w < sw || h < fm.getHeight()) {
            b = false;
        }

        return b;
    }

    protected final void drawName( Graphics2D graphics, VisualItem item, int x, int y )
    {
        /*Font font = headerFont( item );

        Point headerLocation = headerLocation( item, x, y );

        String name = (String) item.get( FIELD_NAME );
        graphics.drawString( name, headerLocation.x, headerLocation.y );
        */

        String name = (String) item.get( FIELD_NAME );
        if (name == null) { return; }

        AffineTransform oldAt =  graphics.getTransform();


        //////////////////////
        // HACK, default 50px for reasonable text display ???
        Rectangle2D rect = item.getBounds();
        rect = oldAt.createTransformedShape( rect ).getBounds2D();

        double width =  rect.getWidth() * item.getVisualization().getDisplay( 0 ).getScale();
        if (width < 50) {
            return;
        }
        /*width += 10;
        
        Font headerFont = headerFont( item );
        FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics( headerFont );
        int sw = fm.stringWidth( name );

        if (width < sw) {
            return; 
        }*/
        //////////////////////////



        Point2D headerLocation = headerLocation( graphics, item, x, y );

        headerLocation = oldAt.transform( headerLocation, null );

        graphics.setTransform( new AffineTransform( ) );
        graphics.drawString( name, Math.round( headerLocation.getX() + 0.5), Math.round(headerLocation.getY() + 0.5 ));
        graphics.setTransform( oldAt );
        
    }

    protected Point headerLocation( VisualItem item, int x, int y )
    {
        Font headerFont = headerFont( item );
        FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics( headerFont );

        x = x + PADDING_LEFT;
        y = y + ( PADDING_TOP / 2 ) + fm.getHeight();

        return new Point( x, y );
    }

    protected Point2D headerLocation( Graphics2D graphics, VisualItem item, int x, int y )
    {
        Font headerFont = headerFont( item );
        FontMetrics fm = graphics.getFontMetrics( headerFont );

        double dx = x + PADDING_LEFT;
        double dy = y + ( PADDING_TOP ) + fm.getHeight();

        return new Point2D.Double( dx, dy );
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