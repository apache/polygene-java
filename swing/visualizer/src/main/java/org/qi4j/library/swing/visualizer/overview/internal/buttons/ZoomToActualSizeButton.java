/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.library.swing.visualizer.overview.internal.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import javax.swing.JButton;
import prefuse.Display;
import static org.qi4j.composite.NullArgumentException.validateNotNull;

/**
 * TODO: Localization
 * TODO: Icon
 *
 * @author edward.yakop@gmail.com
 * @author Sonny Gill
 * @since 0.5
 */
public final class ZoomToActualSizeButton extends JButton
{
    public ZoomToActualSizeButton( Display aDisplay )
        throws IllegalArgumentException
    {
        validateNotNull( "aDisplay", aDisplay );
        setText( "Zoom to actual" );
        addActionListener( new ZoomToActualSizeAction( aDisplay ) );
    }

    private static class ZoomToActualSizeAction implements ActionListener
    {
        private final Display display;

        public ZoomToActualSizeAction( Display aDisplay )
        {
            display = aDisplay;
        }

        public void actionPerformed( ActionEvent e )
        {
            Point2D center = new Point2D.Float( display.getWidth() / 2, display.getHeight() / 2 );
            display.animateZoom( center, 1 / display.getScale(), 2000 );
            display.repaint();
        }
    }
}
