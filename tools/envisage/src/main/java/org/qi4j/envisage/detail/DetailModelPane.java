/*
 * Copyright (c) 2009, Tony Kohar. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.envisage.detail;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.qi4j.envisage.event.LinkEvent;
import org.qi4j.envisage.event.LinkListener;
import org.qi4j.tools.model.descriptor.CompositeDetailDescriptor;
import org.qi4j.tools.model.descriptor.EntityDetailDescriptor;
import org.qi4j.tools.model.descriptor.ImportedServiceDetailDescriptor;
import org.qi4j.tools.model.descriptor.LayerDetailDescriptor;
import org.qi4j.tools.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.tools.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.tools.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.tools.model.descriptor.ValueDetailDescriptor;

public final class DetailModelPane
    extends JPanel
{

    protected ResourceBundle bundle = ResourceBundle.getBundle( this.getClass().getName() );

    protected JTabbedPane tabPane;
    protected GeneralPane generalPane;
    protected MethodPane methodPane;
    protected StatePane statePane;
    protected DependencyPane dependencyPane;
    protected ServiceConfigurationPane serviceConfigurationPane;
    protected ServiceUsagePane serviceUsagePane;
    protected ImportedByPane importedByPane;
    protected APIPane apiPane;
    protected SPIPane spiPane;

    protected boolean linkActivatedInProgress;

    public DetailModelPane()
    {
        tabPane = new JTabbedPane();

        this.setLayout( new BorderLayout() );
        this.add( tabPane, BorderLayout.CENTER );

        createDetailPane();
    }

    private void createDetailPane()
    {
        generalPane = new GeneralPane( this );
        generalPane.setBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ) );

        statePane = new StatePane( this );
        statePane.setBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ) );

        methodPane = new MethodPane( this );
        methodPane.setBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ) );

        dependencyPane = new DependencyPane( this );
        dependencyPane.setBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ) );

        serviceConfigurationPane = new ServiceConfigurationPane( this );
        serviceConfigurationPane.setBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ) );

        serviceUsagePane = new ServiceUsagePane( this );
        serviceUsagePane.setBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ) );

        importedByPane = new ImportedByPane( this );
        importedByPane.setBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ) );

        apiPane = new APIPane( this );
        apiPane.setBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ) );

        spiPane = new SPIPane( this );
        spiPane.setBorder( BorderFactory.createEmptyBorder( 8, 8, 8, 8 ) );
    }

    public void setDescriptor( final Object objectDescriptor )
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            @Override
            public void run()
            {
                setDescriptorImpl( objectDescriptor );
            }
        } );
    }

    private void setDescriptorImpl( Object objectDescriptor )
    {
        Component curSelectedComp = tabPane.getSelectedComponent();
        tabPane.removeAll();

        generalPane.setDescriptor( objectDescriptor );
        dependencyPane.setDescriptor( objectDescriptor );
        methodPane.setDescriptor( objectDescriptor );
        statePane.setDescriptor( objectDescriptor );
        serviceConfigurationPane.setDescriptor( objectDescriptor );
        serviceUsagePane.setDescriptor( objectDescriptor );
        importedByPane.setDescriptor( objectDescriptor );
        apiPane.setDescriptor( objectDescriptor );
        spiPane.setDescriptor( objectDescriptor );

        if( objectDescriptor instanceof LayerDetailDescriptor
            || objectDescriptor instanceof ModuleDetailDescriptor )
        {
            tabPane.add( bundle.getString( "CTL_APITab.Text" ), apiPane );
            tabPane.add( bundle.getString( "CTL_SPITab.Text" ), spiPane );
        }
        else if( objectDescriptor instanceof ServiceDetailDescriptor )
        {
            tabPane.add( bundle.getString( "CTL_GeneralTab.Text" ), generalPane );
            tabPane.add( bundle.getString( "CTL_DependencyTab.Text" ), dependencyPane );
            tabPane.add( bundle.getString( "CTL_MethodTab.Text" ), methodPane );
            tabPane.add( bundle.getString( "CTL_StateTab.Text" ), statePane );
            tabPane.add( bundle.getString( "CTL_ServiceConfiguration.Text" ), serviceConfigurationPane );
            tabPane.add( bundle.getString( "CTL_ServiceUsage.Text" ), serviceUsagePane );
        }
        else if( objectDescriptor instanceof ImportedServiceDetailDescriptor )
        {
            tabPane.add( bundle.getString( "CTL_GeneralTab.Text" ), generalPane );
            tabPane.add( bundle.getString( "CTL_MethodTab.Text" ), methodPane );
            tabPane.add( bundle.getString( "CTL_ServiceUsage.Text" ), serviceUsagePane );
            tabPane.add( bundle.getString( "CTL_ImportedBy.Text" ), importedByPane );
        }
        else if( objectDescriptor instanceof EntityDetailDescriptor )
        {
            tabPane.add( bundle.getString( "CTL_GeneralTab.Text" ), generalPane );
            tabPane.add( bundle.getString( "CTL_DependencyTab.Text" ), dependencyPane );
            tabPane.add( bundle.getString( "CTL_MethodTab.Text" ), methodPane );
            tabPane.add( bundle.getString( "CTL_StateTab.Text" ), statePane );
        }
        else if( objectDescriptor instanceof ValueDetailDescriptor )
        {
            tabPane.add( bundle.getString( "CTL_GeneralTab.Text" ), generalPane );
            tabPane.add( bundle.getString( "CTL_DependencyTab.Text" ), dependencyPane );
            tabPane.add( bundle.getString( "CTL_MethodTab.Text" ), methodPane );
            tabPane.add( bundle.getString( "CTL_StateTab.Text" ), statePane );
        }
        else if( objectDescriptor instanceof ObjectDetailDescriptor )
        {
            tabPane.add( bundle.getString( "CTL_GeneralTab.Text" ), generalPane );
            tabPane.add( bundle.getString( "CTL_DependencyTab.Text" ), dependencyPane );
            tabPane.add( bundle.getString( "CTL_MethodTab.Text" ), methodPane );
        }
        else if( objectDescriptor instanceof CompositeDetailDescriptor ) // this is transient
        {
            tabPane.add( bundle.getString( "CTL_GeneralTab.Text" ), generalPane );
            tabPane.add( bundle.getString( "CTL_DependencyTab.Text" ), dependencyPane );
            tabPane.add( bundle.getString( "CTL_MethodTab.Text" ), methodPane );
            tabPane.add( bundle.getString( "CTL_StateTab.Text" ), statePane );
        }

        if( linkActivatedInProgress )
        {
            // for linking always display the first tab (General)
            linkActivatedInProgress = false;
            tabPane.setSelectedIndex( 0 );
        }
        else
        {
            // if not linking, then maintain the current selected tab
            if( curSelectedComp != null )
            {
                int index = tabPane.indexOfComponent( curSelectedComp );
                if( index != -1 )
                {
                    tabPane.setSelectedIndex( index );
                }
            }
        }
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

    /* package */ void fireLinkActivated( LinkEvent evt )
    {
        linkActivatedInProgress = true;
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for( int i = listeners.length - 2; i >= 0; i -= 2 )
        {
            if( listeners[i] == LinkListener.class )
            {
                ( (LinkListener) listeners[i + 1] ).activated( evt );
            }
        }
    }
}
