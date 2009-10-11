/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.unitofwork;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

/**
 * JAVADOC
 */
public class NestedUnitOfWorkTest
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
    @Ignore( "Nested UoW has to be fixed" )
    public void whenNestedUnitOfWorkThenReturnCorrectPropertyValues()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();

        try
        {
            // Create product
            EntityBuilder<ProductEntity> cb = unitOfWork.newEntityBuilder( ProductEntity.class );
            cb.instance().name().set( "Chair" );
            cb.instance().price().set( 57 );
            Product chair = cb.newInstance();

            assertThat( "Initial property is not correct", chair.price().get(), equalTo( 57 ) );

            // Create nested unitOfWork
            UnitOfWork nestedUnitOfWork = unitOfWorkFactory.nestedUnitOfWork();
            Property<Integer> originalPrice;
            try
            {
                Product nestedChair = nestedUnitOfWork.get( chair );
                assertThat( "Nested property is correct", nestedChair.price().get(), equalTo( 57 ) );

                nestedChair.price().set( 60 );

                originalPrice = chair.price();
                assertThat( "Initial property has not changed", originalPrice.get(), equalTo( 57 ) );

                assertThat( "Nested property has changed", nestedChair.price().get(), equalTo( 60 ) );
            }
            finally
            {
                nestedUnitOfWork.complete();
            }

            assertThat( "Initial property has been updated", originalPrice.get(), equalTo( 60 ) );
        }
        finally
        {
            unitOfWork.complete();
        }

        assertThat( "Current UnitOfWork is not reset.", null, equalTo( unitOfWorkFactory.currentUnitOfWork() ) );
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