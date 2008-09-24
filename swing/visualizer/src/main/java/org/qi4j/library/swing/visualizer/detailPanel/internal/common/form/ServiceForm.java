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

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.qi4j.library.swing.visualizer.model.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;

/**
 * TODO: Localization
 * TODO: JGoodies!!!
 *
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class ServiceForm extends JPanel
{
    private final JTextField layerField;
    private final JTextField moduleField;

    ServiceForm()
    {
        super( new GridLayout( 2, 2 ) );

        // Layer
        JLabel layerLabel = new JLabel( "Layer" );
        add( layerLabel );
        layerField = new JTextField();
        add( layerField );

        // Module
        JLabel moduleLabel = new JLabel( "Module" );
        add( moduleLabel );
        moduleField = new JTextField();
        add( moduleField );
    }

    void display( ServiceDetailDescriptor aDescriptor )
    {
        if( aDescriptor != null )
        {
            // Module
            ModuleDetailDescriptor module = aDescriptor.module();
            String moduleName = module.descriptor().name();
            moduleField.setText( moduleName );

            // Layer
            LayerDetailDescriptor layer = module.layer();
            String layerName = layer.descriptor().name();
            layerField.setText( layerName );
        }
        else
        {
            layerField.setText( null );
            moduleField.setText( null );
        }
    }
}
