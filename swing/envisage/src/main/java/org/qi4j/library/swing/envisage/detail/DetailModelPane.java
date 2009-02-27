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
import org.qi4j.library.swing.envisage.event.LinkEvent;
import org.qi4j.library.swing.envisage.event.LinkListener;
import org.qi4j.library.swing.envisage.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ServiceDetailDescriptor;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class DetailModelPane extends JPanel
{

    protected ResourceBundle bundle = ResourceBundle.getBundle(this.getClass().getName());

    protected JTabbedPane tabPane;
    protected GeneralPane generalPane;
    protected MethodPane methodPane;
    protected StatePane statePane;
    protected DependencyPane dependencyPane;
    protected ServiceConfigurationPane serviceConfigurationPane;
    protected ServiceUsagePane serviceUsagePane;

    public DetailModelPane()
    {
        tabPane = new JTabbedPane( );

        this.setLayout( new BorderLayout() );
        this.add(tabPane, BorderLayout.CENTER);

        createDetailPane( );

        tabPane.add( bundle.getString( "CTL_GeneralTab.Text" ),  generalPane );
        tabPane.add( bundle.getString( "CTL_DependencyTab.Text" ), dependencyPane );
        tabPane.add( bundle.getString( "CTL_MethodTab.Text" ), methodPane );
    }

    protected void createDetailPane()
    {
        generalPane = new GeneralPane(this);
        generalPane.setBorder( BorderFactory.createEmptyBorder(8, 8, 8, 8) );

        statePane = new StatePane(this);
        statePane.setBorder( BorderFactory.createEmptyBorder(8, 8, 8, 8) );

        methodPane = new MethodPane(this);
        methodPane.setBorder( BorderFactory.createEmptyBorder(8, 8, 8, 8) );

        dependencyPane = new DependencyPane(this);
        dependencyPane.setBorder( BorderFactory.createEmptyBorder(8, 8, 8, 8) );

        serviceConfigurationPane = new ServiceConfigurationPane(this);
        serviceConfigurationPane.setBorder( BorderFactory.createEmptyBorder(8, 8, 8, 8) );

        serviceUsagePane = new ServiceUsagePane(this);
        serviceUsagePane.setBorder( BorderFactory.createEmptyBorder(8, 8, 8, 8) );

    }

    public void setDescriptor(Object objectDescriptor)
    {
        generalPane.setDescriptor( objectDescriptor );
        dependencyPane.setDescriptor( objectDescriptor );
        methodPane.setDescriptor( objectDescriptor );
        statePane.setDescriptor( objectDescriptor );
        serviceConfigurationPane.setDescriptor( objectDescriptor );
        serviceUsagePane.setDescriptor( objectDescriptor );

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
                tabPane.add( bundle.getString( "CTL_ServiceUsage.Text" ), serviceUsagePane );                
            }
        } else {
            int index = tabPane.indexOfComponent( serviceConfigurationPane );
            if (index != -1)
            {
                tabPane.removeTabAt( index );
                tabPane.removeTabAt( index );
            }
        }

        tabPane.setSelectedIndex( 0 );
    }

    /**
     * Add a listener that's notified each time a LinkEvent occurs.
     *
     * @param listener the LinkListener to add
     */
    public void addLinkListener( LinkListener listener )
    {
        listenerList.add( LinkListener.class, listener );
    }

    /**
     * Remove a listener that's notified each time a LinkEvent occurs.
     *
     * @param listener the LinkListener to remove
     */
    public void removeLinkListener( LinkListener listener )
    {
        listenerList.remove( LinkListener.class, listener );
    }
    

    void fireLinkActivated( LinkEvent evt)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for( int i = listeners.length - 2; i >= 0; i -= 2 )
        {
            if( listeners[ i ] == LinkListener.class )
            {
                ( (LinkListener) listeners[ i + 1 ] ).activated( evt );
            }
        }
    }
}
