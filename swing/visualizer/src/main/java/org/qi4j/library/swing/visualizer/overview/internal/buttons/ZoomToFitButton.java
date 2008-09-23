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
import java.awt.geom.Rectangle2D;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import prefuse.Display;
import static prefuse.util.display.DisplayLib.fitViewToBounds;
import prefuse.visual.VisualItem;

/**
 * TODO: Localization
 * TODO: icon
 *
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class ZoomToFitButton extends JButton
{
    public ZoomToFitButton( Display aDisplay, VisualItem anAppVisualItem )
        throws IllegalArgumentException
    {
        validateNotNull( "aDisplay", aDisplay );
        validateNotNull( "anAppVisualItem", anAppVisualItem );

        addActionListener( new ZoomToFitAction( aDisplay, anAppVisualItem ) );
        setText( "Zoom to fit" );
    }

    private final class ZoomToFitAction extends AbstractAction
    {
        private final Display display;
        private final VisualItem appVisualItem;

        private ZoomToFitAction( Display aDisplay, VisualItem anAppVisualItem )
        {
            display = aDisplay;
            appVisualItem = anAppVisualItem;
        }

        public final void actionPerformed( ActionEvent e )
        {
            Rectangle2D bounds = appVisualItem.getBounds();
            fitViewToBounds( display, bounds, 2000 );
            display.repaint();
        }
    }
}
