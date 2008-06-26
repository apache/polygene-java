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
package org.qi4j.lib.swing.binding.example;

import java.awt.Dimension;
import java.util.Vector;
import javax.swing.BoxLayout;
import static javax.swing.BoxLayout.Y_AXIS;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.qi4j.lib.swing.binding.StateModel;
import org.qi4j.lib.swing.binding.TableBinding;
import org.qi4j.property.Property;

public class Form<T extends BoundPersonEntityComposite> extends JPanel
{

    private final StateModel<T> aModel;

    public Form( StateModel<T> aModel )
    {
        this.aModel = aModel;
        T state = aModel.state();
        state.address().get().city();

        JPanel firstNameField = newPersonFirstNameField( aModel, state );
        add( firstNameField );
        JPanel userAddressLine1Field = newUserFirstLineAddressField( aModel, state );
        add( userAddressLine1Field );
        JPanel userAddressLine2Field = newUserSecondLineAddressField( aModel, state );
        add( userAddressLine2Field );

        JPanel genderField = newGenderField( aModel, state );
        add( genderField );

        JPanel userCityField = newUserCityField( aModel, state );
        add( userCityField );
        JPanel userCountryField = newUserCountryField( aModel, state );
        add( userCountryField );

        JTable carTable = newCarTable( aModel, state );
        add( new JScrollPane( carTable ) );

        setLayout( new BoxLayout( this, Y_AXIS ) );
    }

    private JPanel newGenderField( StateModel<T> aModel, T state )
    {
        JPanel panel = new JPanel();

        JLabel label = new JLabel( "Gender:" );
        label.setPreferredSize( new Dimension( 100, 20 ) );
        panel.add( label );

        JRadioButton maleRadio = new JRadioButton( "Male" );
        JRadioButton femaleRadio = new JRadioButton( "Female" );

        ButtonGroup buttonGroup = new ButtonGroup();

        buttonGroup.add( maleRadio );
        buttonGroup.add( femaleRadio );

        panel.add( maleRadio );
        panel.add( femaleRadio );

//        aModel.bindToggleButton( state.gender() ).to( maleRadio ).withValue( Gender.female );
//        aModel.bindToggleButton( state.gender() ).to( femaleRadio ).withValue( Gender.male );

        //TODO do binding

        return panel;
    }

    @SuppressWarnings( "unchecked" )
    private JTable newCarTable( StateModel<T> stateModel, T state )
    {
        Vector<String> columnNames = new Vector<String>();

        columnNames.add( "Model" );
        columnNames.add( "Capacity" );
        columnNames.add( "Price" );

        JTable table = new JTable( null, columnNames );

        TableBinding binding = stateModel.bindTable( state.cars() );
        TableBinding<Car> tableBinding = binding.to( table );

        Car carTemplate = tableBinding.stateModel().state();

        tableBinding.bindColumn( carTemplate.model(), 0 );
        tableBinding.bindColumn( carTemplate.capacity(), 1 );
        tableBinding.bindColumn( carTemplate.price(), 2 );

        table.setPreferredScrollableViewportSize( new Dimension( 200, 70 ) );

        return table;
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
