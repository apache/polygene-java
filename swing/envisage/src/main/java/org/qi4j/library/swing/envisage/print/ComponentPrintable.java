/*  Copyright 2009 Tonny Kohar.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.swing.envisage.print;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

/**
 * A Printable wrapper for component
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class ComponentPrintable implements Printable
{
    protected Component component;
    public ComponentPrintable( Component component) {
        this.component = component;
    }

    public int print( Graphics g, PageFormat pf, int pageIndex )
    {
        if (pageIndex > 0)
        {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;

        double x = pf.getImageableX();
        double y = pf.getImageableY();
        double w = pf.getImageableWidth();
        double h = pf.getImageableHeight();

        Dimension destSize = new Dimension( (int)w, (int)h);
        Dimension srcSize = component.getSize();
        double scale = scaleToFit( srcSize, destSize );

        g2d.translate( pf.getImageableX(), pf.getImageableY() );
        g2d.scale( scale, scale );
        component.print( g2d );
        return PAGE_EXISTS;
    }

    public static double scaleToFit(Dimension src, Dimension dest) {
        //System.err.println("------------------------------");
        //System.err.println(src.width + " " + src.height);
        //System.err.println(dest.width + " " + dest.height);
        double srcW = src.getWidth();
        double srcH = src.getHeight();
        double destW = dest.getWidth();
        double destH = dest.getHeight();
        double scale = 1;
        if (srcW > srcH) {
            if (srcW > destW) {
                scale = destW / srcW;
            }
            srcH = srcH * scale;
            if (srcH > destH) {
                scale = scale * (destH / srcH);
            }
        } else {
            if (srcH > destH) {
                scale = destH / srcH;
            }
            srcW = srcW * scale;
            if (srcW > destW ) {
                scale = scale * (destW / srcW);
            }
        }

        return scale;
    }
}
