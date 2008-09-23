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

import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.MOVE_CURSOR;
import static java.awt.Cursor.getDefaultCursor;
import static java.awt.Cursor.getPredefinedCursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.overview.internal.visualization.Qi4jApplicationDisplay;
import prefuse.Display;
import prefuse.controls.PanControl;

/**
 * TODO: Icon
 * TODO: Localization
 *
 * @author edward.yakop@gmail.com
 * @author Sonny Gill
 * @since 0.5
 */
final class TogglePanButton extends JButton
{
    private static final String SELECT_TEXT = "select";
    private static final String PAN_TEXT = "pan";

    private boolean isPan;

    TogglePanButton( Qi4jApplicationDisplay aDisplay )
        throws IllegalArgumentException
    {
        validateNotNull( "aDisplay", aDisplay );

        isPan = false;
        updateText();
        addActionListener( new TogglePanAction( aDisplay ) );
    }

    private void updateText()
    {
        if( !isPan )
        {
            setText( PAN_TEXT );
        }
        else
        {
            setText( SELECT_TEXT );
        }
    }

    private class TogglePanAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        private final PanControl panControl;
        private final Qi4jApplicationDisplay display;

        public TogglePanAction( Qi4jApplicationDisplay aDisplay )
        {
            display = aDisplay;
            panControl = new MousePanControl( aDisplay );
            panControl.setEnabled( false );
            display.addControlListener( panControl );
        }

        public void actionPerformed( ActionEvent anEvent )
        {
            isPan = !isPan;
            updateText();
            if( isPan )
            {
                display.setCursor( getPredefinedCursor( HAND_CURSOR ) );
                panControl.setEnabled( true );
            }
            else
            {
                display.setCursor( getDefaultCursor() );
                panControl.setEnabled( false );
            }
        }
    }

    private static class MousePanControl extends PanControl
    {
        private final Qi4jApplicationDisplay display;

        private int xLoc;
        private int yLoc;

        private MousePanControl( Qi4jApplicationDisplay aDisplay )
        {
            super( true );
            display = aDisplay;
        }

        @Override
        public void mousePressed( MouseEvent anEvent )
        {
            // TODO: Figure out boundary of TOP, LEFT, RIGHT, BOTTOM for valid drag

            Display display = this.display;
            display.setCursor( getPredefinedCursor( MOVE_CURSOR ) );

            xLoc = anEvent.getX();
            yLoc = anEvent.getY();
        }

        @Override
        public void mouseDragged( MouseEvent anEvent )
        {
            int x = anEvent.getX(), y = anEvent.getY();
            int dx = x - xLoc, dy = y - yLoc;

            display.pan( dx, dy );
//            display.pan( dx, dy, false );


            xLoc = x;
            yLoc = y;

            display.repaint();
        }

        @Override
        public void mouseReleased( MouseEvent e )
        {
            display.setCursor( getPredefinedCursor( HAND_CURSOR ) );
            xLoc = -1;
            yLoc = -1;
        }
    }
}
