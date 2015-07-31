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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.lib.swing.binding.StateModel;
import org.qi4j.lib.swing.binding.SwingBindingAssembler;

public class Main
{

    private static Module module;

    public static void main( String[] args )
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( BoundPersonComposite.class );
                module.transients( AddressTransient.class );
                module.values( CityValue.class, CountryValue.class );
                new SwingBindingAssembler().assemble( module );
            }
        };
        module = assembler.module();
        Address address1 = createAddress( "Vista Damai", "Jalan Tun Razak" );
        Address address2 = createAddress( "Mutiara", "Jalan Ceylon" );

        TransientBuilder<BoundPersonComposite> builder = module.newTransientBuilder( BoundPersonComposite.class );
        PersonComposite prototype = builder.prototype();
        prototype.address().set( address1 );
        prototype.firstName().set( "Niclas" );
        final BoundPersonComposite p1 = builder.newInstance();

        prototype.address().set( address2 );
        prototype.firstName().set( "Edward" );
        final BoundPersonComposite p2 = builder.newInstance();

        final StateModel<BoundPersonComposite> model = module.newObject( StateModel.class, BoundPersonComposite.class );
        Form form = new Form<BoundPersonComposite>( model );

        JFrame frame = new JFrame( "testing" );

        frame.add( form, BorderLayout.CENTER );
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setFocusable( true );
        checkBoxPanel.addFocusListener( new FocusListener()
        {

            public void focusGained( FocusEvent e )
            {
                System.out.println( "Gained!" );
            }

            public void focusLost( FocusEvent e )
            {
                System.out.println( "LOst!" );
            }
        } );
        JCheckBox sw = new JCheckBox( "Toggle" );
        sw.addActionListener( new ActionListener()
        {

            public void actionPerformed( ActionEvent e )
            {
                if( ( (JCheckBox) e.getSource() ).isSelected() )
                {
                    model.use( p1 );
                    System.out.println( p1.firstName().get() );
                }
                else
                {
                    model.use( p2 );
                    System.out.println( p2.firstName().get() );
                }
            }
        }
        );
        checkBoxPanel.add( sw );
        frame.add( checkBoxPanel, BorderLayout.EAST );
        frame.pack();
        System.out.println( model );
        frame.setVisible( true );
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    }

    private static Address createAddress( String line1, String line2 )
    {
        TransientBuilder<Address> addressBuilder = module.newTransientBuilder( Address.class );
        addressBuilder.prototype().line1().set( line1 );
        addressBuilder.prototype().line2().set( line2 );
        addressBuilder.prototype().city().set( createCity("Kuala Lumpur") );
        return addressBuilder.newInstance();
    }

    private static City createCity( String cityName )
    {
        ValueBuilder<City> builder = module.newValueBuilder( City.class );
        builder.prototype().name().set( cityName );
        builder.prototype().country().set( createCountry( "Malaysia" ) );
        return builder.newInstance();
    }

    private static Country createCountry( String countryName )
    {
        ValueBuilder<Country> builder = module.newValueBuilder( Country.class );
        builder.prototype().name().set( countryName );
        return builder.newInstance();
    }
}
