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

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.qi4j.library.swing.envisage.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ServiceDetailDescriptor;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class DetailModelPane extends JPanel
{
    protected static final int GENERAL_TAB = 0;
    protected static final int METHOD_TAB = 1;
    protected static final int DEPENDENCIE_TAB = 2;
    protected static final int STATE_TAB = 3;


    protected ResourceBundle bundle = ResourceBundle.getBundle(this.getClass().getName());

    protected JTabbedPane tabPane;
    protected GeneralPane generalPane;
    protected MethodPane methodPane;
    protected StatePane statePane;
    protected DependencyPane dependencyPane;
    protected ServiceConfigurationPane serviceConfigurationPane;

    protected int commonTabCount = 0;

    public DetailModelPane()
    {
        tabPane = new JTabbedPane( );

        this.setLayout( new BorderLayout() );
        this.add(tabPane, BorderLayout.CENTER);

        createDetailPane( );

        tabPane.add( bundle.getString( "CTL_GeneralTab.Text" ),  generalPane );
        tabPane.add( bundle.getString( "CTL_MethodTab.Text" ), methodPane );
        tabPane.add( bundle.getString( "CTL_DependencyTab.Text" ), dependencyPane );
    }

    protected void createDetailPane()
    {
        generalPane = new GeneralPane();
        generalPane.setBorder( BorderFactory.createEmptyBorder(8, 8, 8, 8) );
        commonTabCount++;

        statePane = new StatePane();
        statePane.setBorder( BorderFactory.createEmptyBorder(8, 8, 8, 8) );
        commonTabCount++;

        methodPane = new MethodPane();
        methodPane.setBorder( BorderFactory.createEmptyBorder(8, 8, 8, 8) );
        commonTabCount++;

        dependencyPane = new DependencyPane();
        dependencyPane.setBorder( BorderFactory.createEmptyBorder(8, 8, 8, 8) );
        commonTabCount++;

        serviceConfigurationPane = new ServiceConfigurationPane();
        serviceConfigurationPane.setBorder( BorderFactory.createEmptyBorder(8, 8, 8, 8) );
    }

    public void setDescriptor(Object objectDescriptor)
    {
        generalPane.setDescriptor( objectDescriptor );
        methodPane.setDescriptor( objectDescriptor );
        dependencyPane.setDescriptor( objectDescriptor );
        statePane.setDescriptor( objectDescriptor );
        serviceConfigurationPane.setDescriptor( objectDescriptor );

        if (objectDescriptor instanceof ObjectDetailDescriptor )
        {
            int index = tabPane.indexOfComponent( statePane );
            if (index != -1)
            {
                tabPane.removeTabAt( index );
            }
        } else {
            int index = tabPane.indexOfComponent( statePane );
            if (index == -1)
            {
                tabPane.add( bundle.getString( "CTL_StateTab.Text" ), statePane );
            }
        }

        if (objectDescriptor instanceof ServiceDetailDescriptor)
        {
            int index = tabPane.indexOfComponent( serviceConfigurationPane );
            if (index == -1)
            {
                tabPane.add( bundle.getString( "CTL_ServiceConfiguration.Text" ), serviceConfigurationPane );
            }
        } else {
            int index = tabPane.indexOfComponent( serviceConfigurationPane );
            if (index != -1)
            {
                tabPane.removeTabAt( index );
            }
        }

    }
}
