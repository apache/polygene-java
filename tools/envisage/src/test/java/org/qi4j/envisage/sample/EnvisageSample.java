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

package org.qi4j.envisage.sample;

import java.awt.GraphicsEnvironment;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.envisage.Envisage;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assume.*;

public class EnvisageSample
    extends AbstractQi4jTest
{

    public static void main( String[] args )
        throws Exception
    {
        EnvisageSample sample = new EnvisageSample();
        sample.runSample();
    }

    @BeforeClass
    public static void assumeDisplay()
    {
        assumeFalse( GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance() );
        String display = System.getenv( "DISPLAY" );
        assumeThat( display, is( notNullValue() ) );
        assumeTrue( display.length() > 0 );
    }

    @Test
    public void runSample()
        throws Exception
    {
        setUp();
        createTestData();
        //createTestData2();
        //createTestData3();

        new Envisage().run( applicationModel );
//        Thread.sleep( 1113000 );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( CarEntity.class );
        module.entities( AnimalEntity.class );
        new RdfMemoryStoreAssembler().assemble( module );
        new EntityTestAssembler().assemble( module );
    }

    public void createTestData()
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            createCar( "Volvo", "S80", 2007 );
            createCar( "Volvo", "C70", 2006 );
            createCar( "Ford", "Transit", 2007 );
            createCar( "Ford", "Mustang", 2007 );
            createCar( "Ford", "Mustang", 2006 );
            createCar( "Ford", "Mustang", 2005 );

            createAnimal( "Cat", "Miaow" );
            createAnimal( "Duck", "Kwek Kwek" );
            createAnimal( "Dog", "Guk Guk" );
            createAnimal( "Cow", "Moooo" );

            uow.complete();
        }
        catch( ConcurrentEntityModificationException e )
        {
            // Can not happen.
            e.printStackTrace();
        }
        catch( UnitOfWorkCompletionException e )
        {
            e.printStackTrace();
        }
    }

    private String createCar( String manufacturer, String model, int year )
    {
        UnitOfWork uow = module.currentUnitOfWork();
        EntityBuilder<Car> builder = uow.newEntityBuilder( Car.class );
        Car prototype = builder.instanceFor( CarEntity.class );
        prototype.manufacturer().set( manufacturer );
        prototype.model().set( model );
        prototype.year().set( year );
        CarEntity entity = (CarEntity) builder.newInstance();
        return entity.identity().get();
    }

    private String createAnimal( String name, String sound )
    {
        UnitOfWork uow = module.currentUnitOfWork();
        EntityBuilder<Animal> builder = uow.newEntityBuilder( Animal.class );
        Animal prototype = builder.instanceFor( AnimalEntity.class );
        prototype.name().set( name );
        prototype.sound().set( sound );
        AnimalEntity entity = (AnimalEntity) builder.newInstance();
        return entity.identity().get();
    }

    public interface Car
    {
        Property<String> manufacturer();

        Property<String> model();

        Property<Integer> year();
    }

    public interface CarEntity
        extends Car, EntityComposite
    {
    }

    public interface Animal
    {
        Property<String> name();

        Property<String> sound();
    }

    public interface AnimalEntity
        extends Animal, EntityComposite
    {
    }
}
