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
package org.qi4j.library.swing.binding.example;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.library.swing.binding.StateModel;
import org.qi4j.library.swing.binding.SwingBindingAssembler;

public class Main
{
    private static City kualaLumpur;

    public static void main( String[] args ) throws UnitOfWorkCompletionException
    {

        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addEntities(
                    BoundPersonEntityComposite.class,
                    AddressEntityComposite.class,
                    CarEntityComposite.class,
                    CityEntity.class,
                    CountryEntity.class
                );
                module.addAssembler( new SwingBindingAssembler() );

                module.addAssembler( new InfrastructureAssembler() );
            }
        };

        UnitOfWork unitOfWork = assembler.unitOfWorkFactory().newUnitOfWork();
        Country malaysia = createMalaysia( unitOfWork );
        kualaLumpur = createKualaLumpur( unitOfWork, malaysia );
        unitOfWork.apply();

        //new person 1
        Address address1 = newAddress( unitOfWork, "Vista Damai", "Jalan Tun Razak" );
        final BoundPersonEntityComposite p1 = newPerson( unitOfWork, "Niclas", "Hedhman", true, Gender.male, address1 );
        p1.cars().add( newCar( unitOfWork, "Proton Wira", "1500cc", "RM45000" ) );
        p1.cars().add( newCar( unitOfWork, "Proton Gen2", "1600cc", "RM53000" ) );

        //new person 2
        Address address2 = newAddress( unitOfWork, "Mutiara", "Jalan Ceylon" );
        final BoundPersonEntityComposite p2 = newPerson( unitOfWork, "Edward", "Yakop", false, Gender.male, address2 );
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

    private static Country createMalaysia( UnitOfWork unitOfWork )
    {
        EntityBuilder<Country> builder = unitOfWork.newEntityBuilder( Country.class );
        Country prototype = builder.stateFor( Country.class );
        prototype.name().set( "Malaysia" );
        return builder.newInstance();
    }

    private static City createKualaLumpur( UnitOfWork unitOfWork, Country malaysia )
    {
        EntityBuilder<City> builder = unitOfWork.newEntityBuilder( City.class );
        City prototype = builder.stateFor( City.class );
        prototype.name().set( "Kuala Lumpur" );
        prototype.country().set( malaysia );
        return builder.newInstance();
    }

    private static BoundPersonEntityComposite newPerson( UnitOfWork unitOfWork, String firstName, String lastName,
                                                         boolean expertGroupMember, Gender gender, Address address )
    {
        EntityBuilder<BoundPersonEntityComposite> builder = unitOfWork.newEntityBuilder( BoundPersonEntityComposite.class );
        Person prototype = builder.stateFor( Person.class );
        prototype.firstName().set( firstName );
        prototype.lastName().set( lastName );
        prototype.address().set( address );
        prototype.expertGroupMember().set( expertGroupMember );
        prototype.gender().set( gender );
        BoundPersonEntityComposite person = builder.newInstance();
        return person;
    }

    private static Address newAddress( UnitOfWork unitOfWork, String addressLine1, String addressLine2 )
    {
        EntityBuilder<Address> builder = unitOfWork.newEntityBuilder( Address.class );
        Address prototype = builder.stateFor( Address.class );
        prototype.line1().set( addressLine1 );
        prototype.line2().set( addressLine2 );
        prototype.city().set( kualaLumpur );
        return builder.newInstance();
    }

    private static Car newCar( UnitOfWork unitOfWork, String model, String capacity, String price )
    {
        EntityBuilder<Car> builder = unitOfWork.newEntityBuilder( Car.class );
        Car prototype = builder.stateFor( Car.class );
        prototype.model().set( model );
        prototype.capacity().set( capacity );
        prototype.price().set( price );
        return builder.newInstance();
    }

}
