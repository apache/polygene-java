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
package org.apache.zest.index.rdf.qi173;

import org.junit.Assert;
import org.junit.Test;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.api.query.QueryExpressions;
import org.apache.zest.api.unitofwork.ConcurrentEntityModificationException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import java.util.Iterator;

import static org.apache.zest.api.query.QueryExpressions.orderBy;

public class Qi173IssueTest
    extends AbstractZestTest
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
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            createCar( "Volvo", "S80", 2007 );
            createCar( "Volvo", "C70", 2006 );
            createCar( "Ford", "Transit", 2007 );
            createCar( "Ford", "Mustang", 2007 );
            createCar( "Ford", "Mustang", 2006 );
            createCar( "Ford", "Mustang", 2005 );
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

        uow = module.newUnitOfWork();
        QueryBuilder<Car> qb = module.newQueryBuilder( Car.class );
        Car template = QueryExpressions.templateFor( Car.class );
        qb = qb.where( QueryExpressions.eq( template.year(), 2007 ) );

        Query<Car> query = uow.newQuery( qb );
        query.orderBy( orderBy( template.manufacturer() ), orderBy( template.model() ) );
        Iterator<Car> cars = query.iterator();
        Assert.assertTrue( cars.hasNext() );
        Car car1 = cars.next();
        Assert.assertEquals( car1.manufacturer().get(), "Ford" );
        Assert.assertEquals( car1.model().get(), "Mustang" );
        Assert.assertEquals( (int) car1.year().get(), 2007 );
        Car car2 = cars.next();
        Assert.assertEquals( car2.manufacturer().get(), "Ford" );
        Assert.assertEquals( car2.model().get(), "Transit" );
        Assert.assertEquals( (int) car2.year().get(), 2007 );
        Car car3 = cars.next();
        Assert.assertEquals( car3.manufacturer().get(), "Volvo" );
        Assert.assertEquals( car3.model().get(), "S80" );
        Assert.assertEquals( (int) car3.year().get(), 2007 );
        for( Car car : query )
        {
            System.out.println( car.manufacturer().get() + " " + car.model().get() + ", " + car.year().get() );
        }

        uow.discard();
    }

    private String createCar( String manufacturer, String model, int year )
    {
        UnitOfWork uow = module.currentUnitOfWork();
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

    public static interface Car
    {
        Property<String> manufacturer();

        Property<String> model();

        Property<Integer> year();
    }
}