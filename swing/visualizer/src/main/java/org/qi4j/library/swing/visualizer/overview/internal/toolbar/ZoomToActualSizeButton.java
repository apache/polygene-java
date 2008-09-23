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
package org.qi4j.library.swing.visualizer.overview.internal.toolbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.overview.internal.visualization.Qi4jApplicationDisplay;

/**
 * TODO: Localization
 * TODO: Icon
 *
 * @author edward.yakop@gmail.com
 * @author Sonny Gill
 * @since 0.5
 */
final class ZoomToActualSizeButton extends JButton
{
    ZoomToActualSizeButton( Qi4jApplicationDisplay aDisplay )
        throws IllegalArgumentException
    {
        validateNotNull( "aDisplay", aDisplay );
        setText( "Zoom to actual" );
        addActionListener( new ZoomToActualSizeAction( aDisplay ) );
    }

    private static class ZoomToActualSizeAction
        implements ActionListener
    {
        private final Qi4jApplicationDisplay display;

        public ZoomToActualSizeAction( Qi4jApplicationDisplay aDisplay )
        {
            display = aDisplay;
        }

        public void actionPerformed( ActionEvent anEvent )
        {
            display.zoomToActualSize();
        }
    }
}
