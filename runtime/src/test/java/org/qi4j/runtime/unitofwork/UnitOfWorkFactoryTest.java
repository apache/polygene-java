/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.runtime.unitofwork;

import java.util.concurrent.Callable;
import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

public class UnitOfWorkFactoryTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addEntities( AccountComposite.class,
                            OrderComposite.class,
                            ProductEntity.class,
                            CustomerComposite.class );

        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void testUnitOfWork()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();

        // Create product
        EntityBuilder<ProductEntity> cb = unitOfWork.newEntityBuilder( ProductEntity.class );
        cb.instance().name().set( "Chair" );
        cb.instance().price().set( 57 );
        Product chair = cb.newInstance();

        String actual = chair.name().get();
        org.junit.Assert.assertThat( "Chair.name()", actual, org.hamcrest.CoreMatchers.equalTo( "Chair" ) );
        org.junit.Assert.assertThat( "Chair.price()", chair.price().get(), org.hamcrest.CoreMatchers.equalTo( 57 ) );

        unitOfWork.complete();
    }

    @Mixins( { AccountMixin.class } )
    public interface AccountComposite
        extends Account, EntityComposite
    {
    }

    public interface Account
    {
        Property<Integer> balance();

        void add( int amount );

        void remove( int amount );
    }

    public static abstract class AccountMixin
        implements Account
    {
        public void add( int amount )
        {
            balance().set( balance().get() + amount );
        }

        public void remove( int amount )
        {
            balance().set( balance().get() - amount );
        }
    }

    public interface Customer
    {
        Association<Account> account();

        Property<String> name();
    }

    public interface CustomerComposite
        extends Customer, EntityComposite
    {
    }

    public interface LineItem
    {
        Association<Product> product();
    }

    public interface LineItemComposite
        extends LineItem, EntityComposite
    {
    }

    public interface Name
        extends Property<String>
    {
    }

    public interface Order
    {
        Association<Customer> customer();

        ManyAssociation<LineItem> lineItems();
    }

    public interface OrderComposite
        extends Order, EntityComposite
    {
    }

    public interface Product
    {
        @UseDefaults
        Property<String> name();

        @UseDefaults
        Property<Integer> price();
    }

    public interface ProductEntity
        extends Product, EntityComposite
    {
    }
}

class UnitOfWorkTemplate
    implements Callable, Runnable
{
    private UnitOfWorkFactory factory;
    private Callable callable;

    UnitOfWorkTemplate( UnitOfWorkFactory factory, Callable callable )
    {
        this.factory = factory;
        this.callable = callable;
    }

    UnitOfWorkTemplate( UnitOfWorkFactory factory, final Runnable runnable )
    {

        this.factory = factory;
        callable = new Callable()
        {
            public Object call()
                throws Exception
            {
                runnable.run();
                return null;
            }
        };
    }

    public Object call()
        throws Exception
    {
        UnitOfWork unitOfWork = factory.newUnitOfWork();
        try
        {
            Object result = callable.call();
            unitOfWork.complete();
            return result;
        }
        catch( Exception e )
        {
            unitOfWork.discard();
            throw e;
        }
    }

    public void run()
    {
        try
        {
            call();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}