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
package org.qi4j.library.swing.visualizer.detailPanel.internal.common.form;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.qi4j.library.swing.visualizer.model.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;
import org.qi4j.service.ServiceDescriptor;

/**
 * TODO: Localization
 *
 * @author edward.yakop@gmail.com
 * @see ServiceDetailDescriptor
 * @since 0.5
 */
public final class ServiceForm extends JPanel
{
    // Location
    private JTextField layerField;
    private JTextField moduleField;

    // Service
    private JTextField serviceId;
    private JTextField serviceType;
    private JCheckBox serviceIsInstOnStartup;
    private JTextField serviceVisibility;
    private JTextField serviceFactory;

    public ServiceForm()
    {
        FormLayout layout = new FormLayout(
            // Columns
            "5dlu, pref, 5dlu, left:80dlu:grow(0.5), 20dlu, pref, 5dlu, left:150dlu:grow, 5dlu:grow",
            // Rows
            "5dlu, " +                                      // Gap: 1
            "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, " +  // Service: 2 -> 8
            "10dlu, " +                                      // Gap: 9
            "pref, 3dlu, pref, " +                          // Location: 10 -> 12
            "5dlu:grow"                                     // Gap: 13
        );
        layout.setRowGroups( new int[][]{
            { 2, 10 } // Separators
        } );
        layout.setColumnGroups( new int[][]{ { 1, 9 }, { 2, 6 } } );
        setLayout( layout );
        CellConstraints cc = new CellConstraints();

        addService( cc );
        addLocation( cc );
    }

    private void addService( CellConstraints cc )
    {
        DefaultComponentFactory cmpFactory = DefaultComponentFactory.getInstance();

        // Separator:
        JComponent serviceSeparator = cmpFactory.createSeparator( "Service" );
        add( serviceSeparator, cc.xyw( 2, 2, 7 ) );

        // *** Row 1

        // Identity
        JLabel idLabel = cmpFactory.createLabel( "Identity" );
        add( idLabel, cc.xy( 2, 4 ) );

        serviceId = new JTextField();
        add( serviceId, cc.xy( 4, 4, "fill, center" ) );
        serviceId.setEditable( false );

        // Type
        JLabel typeLabel = cmpFactory.createLabel( "Type" );
        add( typeLabel, cc.xy( 6, 4 ) );

        serviceType = new JTextField();
        add( serviceType, cc.xy( 8, 4, "fill, center" ) );
        serviceType.setEditable( false );

        // *** Row 2

        // Is Instantiation on startup
        JLabel isInstOnStartLabel = cmpFactory.createLabel( "Is instantiate on startup?" );
        add( isInstOnStartLabel, cc.xy( 2, 6 ) );

        serviceIsInstOnStartup = new JCheckBox();
        add( serviceIsInstOnStartup, cc.xy( 4, 6, "center, center" ) );
        serviceIsInstOnStartup.setEnabled( false );

        // Visiblity
        JLabel visibilityLabel = cmpFactory.createLabel( "Visibility" );
        add( visibilityLabel, cc.xy( 6, 6 ) );

        serviceVisibility = new JTextField();
        add( serviceVisibility, cc.xy( 8, 6, "fill, center" ) );
        serviceVisibility.setEditable( false );

        // *** Row 3

        // Service Factory
        JLabel serviceFactoryLabel = cmpFactory.createLabel( "Service factory" );
        add( serviceFactoryLabel, cc.xy( 2, 8 ) );

        serviceFactory = new JTextField();
        add( serviceFactory, cc.xyw( 4, 8, 5, "fill, center" ) );
        serviceFactory.setEditable( false );
    }

    private void addLocation( CellConstraints cc )
    {
        DefaultComponentFactory cmpFactory = DefaultComponentFactory.getInstance();

        // Separator: Locations
        JComponent locSeparator = cmpFactory.createSeparator( "Location" );
        add( locSeparator, cc.xyw( 2, 10, 7 ) );

        // Layer
        JLabel layerLabel = cmpFactory.createLabel( "Layer" );
        add( layerLabel, cc.xy( 2, 12 ) );

        layerField = new JTextField();
        add( layerField, cc.xy( 4, 12, "fill, center" ) );
        layerField.setEditable( false );

        // Module
        JLabel moduleLabel = cmpFactory.createLabel( "Module" );
        add( moduleLabel, cc.xy( 6, 12 ) );

        moduleField = new JTextField();
        add( moduleField, cc.xy( 8, 12, "fill, center" ) );
        moduleField.setEditable( false );
    }

    public final void updateModel( ServiceDetailDescriptor aDescriptor )
    {
        populateLocationFields( aDescriptor );
        populateServiceFields( aDescriptor );
    }

    private void populateLocationFields( ServiceDetailDescriptor aDescriptor )
    {
        String moduleName = "";
        String layerName = "";

        if( aDescriptor != null )
        {
            ModuleDetailDescriptor module = aDescriptor.module();
            moduleName = module.descriptor().name();

            LayerDetailDescriptor layer = module.layer();
            layerName = layer.descriptor().name();
        }

        moduleField.setText( moduleName );
        layerField.setText( layerName );
    }

    private void populateServiceFields( ServiceDetailDescriptor aDescriptor )
    {
        String serviceIdValue = "";
        String serviceClassName = "";
        boolean isInstantiateOnStartup = false;
        String visibility = "";
        String serviceFactoryVal = "";

        if( aDescriptor != null )
        {
            ServiceDescriptor descriptor = aDescriptor.descriptor();

            serviceIdValue = descriptor.identity();
            serviceClassName = descriptor.type().getName();
            isInstantiateOnStartup = descriptor.isInstantiateOnStartup();
            visibility = descriptor.visibility().toString();                // TODO: localization
            serviceFactoryVal = descriptor.serviceFactory().getName();
        }

        serviceId.setText( serviceIdValue );
        serviceType.setText( serviceClassName );
        serviceIsInstOnStartup.setSelected( isInstantiateOnStartup );
        serviceVisibility.setText( visibility );
        serviceFactory.setText( serviceFactoryVal );
    }
}
