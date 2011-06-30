/*
 * Copyright 2009 Niclas Hedhman.
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
package org.qi4j.tests.regression.niclas1;

import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.qi4j.api.query.QueryExpressions.orderBy;

public class PieroTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( CarEntity.class );
        new RdfMemoryStoreAssembler().assemble( module );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void testPersistence()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            String carId1 = createCar( "Volvo", "S80", 2007 );
            String carId2 = createCar( "Volvo", "C70", 2006 );
            String carId3 = createCar( "Ford", "Transit", 2007 );
            String carId4 = createCar( "Ford", "Mustang", 2007 );
            String carId5 = createCar( "Ford", "Mustang", 2006 );
            String carId6 = createCar( "Ford", "Mustang", 2005 );
            uow.complete();
            uow = unitOfWorkFactory.newUnitOfWork();
            System.out.println( uow.get( Car.class, carId1 ) );
            System.out.println( uow.get( Car.class, carId2 ) );
            System.out.println( uow.get( Car.class, carId3 ) );
            System.out.println( uow.get( Car.class, carId4 ) );
            System.out.println( uow.get( Car.class, carId5 ) );
            System.out.println( uow.get( Car.class, carId6 ) );
            uow.discard();
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

        uow = unitOfWorkFactory.newUnitOfWork();
        QueryBuilder<Car> qb = queryBuilderFactory.newQueryBuilder( Car.class );
        Car template = QueryExpressions.templateFor( Car.class );
        qb.where( QueryExpressions.eq( template.year(), 2007 ) );

        Query<Car> query = uow.newQuery( qb );
        query.orderBy( orderBy( template.manufacturer() ), orderBy( template.model() ) );
        System.out.println( "Cars from 2007" );
        for( Car car : query )
        {
            System.out.println( car.manufacturer().get() + " " + car.model().get() + ", " + car.year().get() );
        }

        uow.discard();
    }

    private String createCar( String manufacturer, String model, int year )
    {
        UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
        EntityBuilder<Car> builder = uow.newEntityBuilder( Car.class );
        Car prototype = builder.instanceFor( Car.class );
        prototype.manufacturer().set( manufacturer );
        prototype.model().set( model );
        prototype.year().set( year );
        CarEntity entity = (CarEntity) builder.newInstance();
        return entity.identity().get();
    }

    public interface CarEntity
        extends Car, EntityComposite
    {
    }
}
