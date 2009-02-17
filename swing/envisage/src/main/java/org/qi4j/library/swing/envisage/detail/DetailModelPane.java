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
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.util.ResourceBundle;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class DetailModelPane extends JPanel
{
    /*protected static final int COMMONS_TAB = 0;
    protected static final int METHODS_TAB = 1;
    protected static final int STATES_TAB = 2;
    protected static final int DEPENDENCIES_TAB = 3;
    */

    protected ResourceBundle bundle = ResourceBundle.getBundle(this.getClass().getName());

    protected GeneralPane generalPane;
    protected StatePane statePane;

    public DetailModelPane()
    {
        JTabbedPane tabPane = new JTabbedPane( );

        this.setLayout( new BorderLayout() );
        this.add(tabPane, BorderLayout.CENTER);

        generalPane = new GeneralPane();
        generalPane.setBorder( BorderFactory.createEmptyBorder(8, 8, 8, 8) );

        statePane = new StatePane();
        statePane.setBorder( BorderFactory.createEmptyBorder(8, 8, 8, 8) );

        tabPane.add( bundle.getString( "CTL_GeneralTab.Text" ), new JScrollPane( generalPane ) );
        tabPane.add( bundle.getString( "CTL_MethodTab.Text" ), new JScrollPane() );
        tabPane.add( bundle.getString( "CTL_StateTab.Text" ), new JScrollPane(statePane) );
        tabPane.add( bundle.getString( "CTL_DependencyTab.Text" ), new JScrollPane() );
    }

    public void setDescriptor(Object objectDescriptor)
    {
        generalPane.setDescriptor( objectDescriptor );
        statePane.setDescriptor( objectDescriptor );
    }
}
