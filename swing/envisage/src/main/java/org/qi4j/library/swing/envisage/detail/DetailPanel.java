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
package org.qi4j.library.swing.envisage.detail;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class DetailPanel extends JPanel
{
    public DetailPanel()
    {
        JTabbedPane tabPane = new JTabbedPane( );

        this.setLayout( new BorderLayout() );
        this.add(tabPane, BorderLayout.CENTER);

        tabPane.add( "Commons", new JPanel() );
        tabPane.add( "Methods", new JPanel() );
        tabPane.add( "State", new JPanel() );
        tabPane.add( "Dependency", new JPanel() );

    }
}
