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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.lib.swing.binding.StateModel;
import org.qi4j.lib.swing.binding.SwingBindingAssembler;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;

public class Main
{

    public static void main( String[] args )
    {

        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addEntities(
                    BoundPersonEntityComposite.class,
                    AddressEntityComposite.class,
                    CarEntityComposite.class
                );
                module.addAssembler( new SwingBindingAssembler() );

                module.addAssembler( new InfrastructureAssembler() );
            }
        };

        UnitOfWork unitOfWork = assembler.unitOfWorkFactory().newUnitOfWork();

        //new person 1
        final BoundPersonEntityComposite p1 = newPerson( unitOfWork, "Niclas", true );
        p1.address().set( newAddress( unitOfWork, "Vista Damai", "Jalan Tun Razak" ) );
        p1.cars().add( newCar( unitOfWork, "Proton Wira", "1500cc", "RM45000" ) );
        p1.cars().add( newCar( unitOfWork, "Proton Gen2", "1600cc", "RM53000" ) );

        //new person 2
        final BoundPersonEntityComposite p2 = newPerson( unitOfWork, "Edward", false );
        p2.address().set( newAddress( unitOfWork, "Mutiara", "Jalan Ceylon" ) );
        p2.cars().add( newCar( unitOfWork, "Toyota Vios", "1800cc", "RM76,000" ) );
        p2.cars().add( newCar( unitOfWork, "Honda City", "1600cc", "RM88,000" ) );
        p2.cars().add( newCar( unitOfWork, "BMW 320i", "2200cc", "RM250,000" ) );

        //setting up StateModel
        ObjectBuilderFactory builderFactory = assembler.objectBuilderFactory();
        ObjectBuilder<StateModel> objectBuilder = builderFactory.newObjectBuilder( StateModel.class );
        objectBuilder.use( BoundPersonEntityComposite.class );

        final StateModel<BoundPersonEntityComposite> model = objectBuilder.newInstance();

        Form form = new Form( model );

        JFrame frame = new JFrame( "testing" );

        frame.add( form, CENTER );

        JCheckBox toggleCheckBox = new JCheckBox( "Toggle" );
        toggleCheckBox.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                if( ( (JCheckBox) e.getSource() ).isSelected() )
                {
                    model.use( p1 );
                }
                else
                {
                    model.use( p2 );
                }
            }
        }
        );

        frame.setPreferredSize( new Dimension( 400, 300 ) );
        frame.add( toggleCheckBox, EAST );
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        frame.pack();
        frame.setLocationRelativeTo( null );
        frame.setVisible( true );
        model.use( p2 );
    }

    private static BoundPersonEntityComposite newPerson( UnitOfWork unitOfWork, String firstName,
                                                         boolean expertGroupMember )
    {
        BoundPersonEntityComposite person = unitOfWork.newEntity( BoundPersonEntityComposite.class );

        person.firstName().set( firstName );
        person.expertGroupMember().set( expertGroupMember );

        return person;
    }

    private static Address newAddress( UnitOfWork unitOfWork, String addressLine1, String addressLine2 )
    {
        Address address = unitOfWork.newEntity( Address.class );

        address.line1().set( addressLine1 );

        address.line2().set( addressLine2 );

        return address;
    }

    private static Car newCar( UnitOfWork unitOfWork, String model, String capacity, String price )
    {
        Car car = unitOfWork.newEntity( Car.class );

        car.model().set( model );
        car.capacity().set( capacity );
        car.price().set( price );

        return car;
    }

}
