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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.overview.internal.visualization.PrefuseJScrollPane;
import org.qi4j.library.swing.visualizer.overview.internal.visualization.Qi4jApplicationDisplay;
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
    private static final ImageIcon SELECT_TEXT = new ImageIcon( TogglePanButton.class.getResource( "pointer.png" ) );
    private static final ImageIcon PAN_TEXT = new ImageIcon( TogglePanButton.class.getResource( "hand.png" ) );

    private boolean isPan;

    TogglePanButton( PrefuseJScrollPane scrollPane )
        throws IllegalArgumentException
    {
        validateNotNull( "scrollPane", scrollPane );

        isPan = false;
        updateText();
        addActionListener( new TogglePanAction( scrollPane ) );
    }

    private void updateText()
    {
        if( !isPan )
        {
            setIcon( PAN_TEXT );
        }
        else
        {
            setIcon( SELECT_TEXT );
        }
    }

    private class TogglePanAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        private final PanControl panControl;
        private final Qi4jApplicationDisplay display;

        public TogglePanAction( PrefuseJScrollPane aScrollPane )
        {
            display = aScrollPane.getDisplay();
            panControl = new MousePanControl( aScrollPane );
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
        private final PrefuseJScrollPane scrollPane;

        private int xLoc;
        private int yLoc;

        private MousePanControl( PrefuseJScrollPane aDisplay )
        {
            super( true );
            scrollPane = aDisplay;
        }

        @Override
        public void mousePressed( MouseEvent anEvent )
        {
            scrollPane.setCursor( getPredefinedCursor( MOVE_CURSOR ) );

            xLoc = anEvent.getX();
            yLoc = anEvent.getY();
        }

        @Override
        public void mouseDragged( MouseEvent anEvent )
        {
            int x = anEvent.getX(), y = anEvent.getY();
            int dx = x - xLoc, dy = y - yLoc;

            scrollPane.scroll( dx, dy );

            xLoc = x;
            yLoc = y;

            scrollPane.repaint();
        }

        @Override
        public void mouseReleased( MouseEvent e )
        {
            scrollPane.setCursor( getPredefinedCursor( HAND_CURSOR ) );
            xLoc = -1;
            yLoc = -1;
        }
    }
}
