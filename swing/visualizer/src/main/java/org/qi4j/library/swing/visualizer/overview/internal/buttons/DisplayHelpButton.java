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
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

/**
 * TODO: Icon
 * TODO: Localization
 * TODO: HTML
 *
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class DisplayHelpButton extends JButton
{
    public DisplayHelpButton()
    {
        setText( "Help" );

        addActionListener( new DisplayHelpAction() );
    }

    private static final class DisplayHelpAction extends AbstractAction
    {
        public final void actionPerformed( ActionEvent anEvent )
        {
            JFrame.getFrames();
            String message = createHelpMessage();
            showMessageDialog( null, message, "Help", INFORMATION_MESSAGE );
        }

        private String createHelpMessage()
        {
            // TODO: This should be an html doc
            StringBuilder buf = new StringBuilder();
            buf.append( "Controls:\n" );
            buf.append( "1. Zoom with mouse scroll wheel\n" );
            buf.append( "2. + key to zoom in, - key to zoom out\n" );

            return buf.toString();
        }
    }

}
