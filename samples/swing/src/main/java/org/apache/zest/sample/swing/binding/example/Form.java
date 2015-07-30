/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.apache.zest.sample.swing.binding.example;

import org.apache.zest.api.property.Property;
import org.apache.zest.sample.swing.binding.StateModel;

import javax.swing.*;
import java.awt.*;

public class Form<T extends BoundPersonComposite> extends JPanel
{

    private final StateModel<T> aModel;

    public Form( StateModel<T> aModel )
    {
        this.aModel = aModel;
        T state = aModel.state();

        JPanel firstNameField = newPersonFirstNameField( aModel, state );
        add( firstNameField );
        JPanel userAddressLine1Field = newUserFirstLineAddressField( aModel, state );
        add( userAddressLine1Field );
        JPanel userAddressLine2Field = newUserSecondLineAddressField( aModel, state );
        add( userAddressLine2Field );
        JPanel userCityField = newUserCityField( aModel, state );
        add( userCityField );
        JPanel userCountryField = newUserCountryField( aModel, state );
        add( userCountryField );
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
    }

    public StateModel<T> getAModel()
    {
        return aModel;
    }

    private JPanel newPersonFirstNameField( StateModel<T> aModel, T aModelState )
    {
        Property<String> property = aModelState.firstName();
        return newTextPanel( aModel, property, "First Name:" );
    }

    private JPanel newUserFirstLineAddressField( StateModel<T> aModel, T aModelState )
    {
        Property<String> property = aModelState.address().get().line1();
        return newTextPanel( aModel, property, "Address:" );
    }

    private JPanel newUserSecondLineAddressField( StateModel<T> aModel, T state )
    {
        Property<String> property = state.address().get().line2();
        return newTextPanel( aModel, property, "" );
    }

    private JPanel newUserCityField( StateModel<T> aModel, T state )
    {
        Property<String> property = state.address().get().city().get().name();
        return newTextPanel( aModel, property, "City:" );
    }

    private JPanel newUserCountryField( StateModel<T> aModel, T state )
    {
        Property<String> property = state.address().get().city().get().country().get().name();
        return newTextPanel( aModel, property, "Country:" );
    }

    private JPanel newTextPanel( StateModel<T> aModel, Property<String> property, String fieldName )
    {
        JPanel panel = new JPanel();
        JLabel label = new JLabel( fieldName );
        label.setPreferredSize( new Dimension( 100, 20 ) );
        panel.add( label );
        JTextField textField = new JTextField();
        panel.add( textField );
        textField.setPreferredSize( new Dimension( 100, 20 ) );
        aModel.bind( property ).to( textField );
        return panel;
    }
}
