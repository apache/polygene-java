/* Copyright 2008 Neo Technology, http://neotechnology.com.
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
package org.qi4j.entity.neo4j.test;

import java.util.List;
import org.junit.Test;
import org.neo4j.api.core.Transaction;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.neo4j.NeoCoreService;
import org.qi4j.entity.neo4j.NeoEntityStoreService;
import org.qi4j.entity.neo4j.NeoIdentityService;
import org.qi4j.entity.neo4j.NeoTransactionService;
import org.qi4j.entity.neo4j.state.direct.DirectEntityStateFactory;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.entity.Account;
import org.qi4j.test.entity.AccountComposite;
import org.qi4j.test.entity.Customer;
import org.qi4j.test.entity.CustomerComposite;
import org.qi4j.test.entity.LineItem;
import org.qi4j.test.entity.LineItemComposite;
import org.qi4j.test.entity.Order;
import org.qi4j.test.entity.OrderComposite;
import org.qi4j.test.entity.Product;
import org.qi4j.test.entity.ProductComposite;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class NeoEntityStoreTest extends AbstractQi4jTest
{
    private UnitOfWork uow;
    private NeoTransactionService txFactory;
    private static String storedId;

    @Test
    public void testOrderSystem() throws Exception
    {
        System.out.println( "TESTCASE: testOrderSystem" );
        System.out.println( "= Testing creation =" );
        Transaction tx = txFactory.beginTx();
        System.out.println( "Neo transaction started." );
        String orderId;
        try
        {
            Order order = newOrder();
            orderId = entityIdOf( order );
            System.out.println( "id: " + orderId );
            storedId = orderId;
            Customer rickard = newCustomer( "Rickard" );
            Account account = newAccount( 0 );
            rickard.account().set( account );
            order.customer().set( rickard );
            List<LineItem> items = order.lineItems();
            LineItem item = newLineItem( newProduct( "IKEA Benny", 189 ) );
            items.add( item );
            items.add( newLineItem( newProduct( "IKEA Luddvig", 138 ) ) );
            //*
            items.add( newLineItem( newProduct( "IKEA Satan", 666 ) ) );
            /*/
            items.add(item);
            //*/
            tx.success();
            System.out.println( "Neo transaction completed successfully." );
        }
        finally
        {
            tx.finish();
            System.out.println( "Neo transaction closed." );
        }
        System.out.println( "= Testing read back =" );
        tx = txFactory.beginTx();
        System.out.println( "Neo transaction started." );
        try
        {
            Order order = getOrder( orderId );
            Customer customer = order.customer().get();
            System.out.println( "* Customer name: " + customer.name().get() );
            System.out.println( "* Account balance: " + customer.account().get().balance().get() );
            System.out.println( "* Number of items: " + order.lineItems().size() );
            for( LineItem item : order.lineItems() )
            {
                Product product = item.product().get();
                System.out.println( "* Order item: " + product.name().get() + " ‡ " + product.price().get() );
            }
            tx.success();
            System.out.println( "Neo transaction completed successfully." );
        }
        finally
        {
            tx.finish();
            System.out.println( "Neo transaction closed." );
        }
    }

    @Test
    public void testReadBack() throws Exception
    {
        System.out.println( "TESTCASE: testReadBack" );
        System.out.println( "= Testing read back =" );
        Transaction tx = txFactory.beginTx();
        System.out.println( "Neo transaction started." );
        try
        {
            System.out.println( "* id: " + storedId );
            Order order = getOrder( storedId );
            Customer customer = order.customer().get();
            System.out.println( "* Customer name: " + customer.name().get() );
            System.out.println( "* Account balance: " + customer.account().get().balance().get() );
            System.out.println( "* Number of items: " + order.lineItems().size() );
            for( LineItem item : order.lineItems() )
            {
                Product product = item.product().get();
                System.out.println( "* Order item: " + product.name().get() + " ‡ " + product.price().get() );
            }
            tx.success();
            System.out.println( "Neo transaction completed successfully." );
        }
        catch( Exception ex )
        {
            System.out.println( "SOME ERROR" );
            ex.printStackTrace();
            throw ex;
        }
        finally
        {
            tx.finish();
            System.out.println( "Neo transaction closed." );
        }
    }

    private Account newAccount( int inBalance )
    {
        System.out.println( "Creating new Account" );
        CompositeBuilder<AccountComposite> accountBuilder = uow
            .newEntityBuilder( null, AccountComposite.class );
        Account prototype = accountBuilder.stateOfComposite();
        prototype.balance().set( inBalance );
        return accountBuilder.newInstance();
    }

    private Customer newCustomer( String name )
    {
        System.out.println( "Creating new Cosumer" );
        CompositeBuilder<CustomerComposite> customerBuilder = uow
            .newEntityBuilder( null, CustomerComposite.class );
        Customer prototype = customerBuilder.stateOfComposite();
        prototype.name().set( name );
        return customerBuilder.newInstance();
    }

    private LineItem newLineItem()
    {
        System.out.println( "Creating new LineItem" );
        CompositeBuilder<LineItemComposite> lineItemBuilder = uow
            .newEntityBuilder( null, LineItemComposite.class );
        return lineItemBuilder.newInstance();
    }

    private LineItem newLineItem( Product product )
    {
        System.out.println( "Creating new LineItem with a Product" );
        LineItem item = newLineItem();
        item.product().set( product );
        return item;
    }

    private Order getOrder( String orderId )
    {
        return uow.getReference( orderId, OrderComposite.class );
    }

    private Order newOrder()
    {
        System.out.println( "Creating new Order" );
        CompositeBuilder<OrderComposite> orderBuilder = uow
            .newEntityBuilder( null, OrderComposite.class );
        return orderBuilder.newInstance();
    }

    private Product newProduct( String name, int price )
    {
        System.out.println( "Creatong new Product: name=\"" + name + "\", price=" + price );
        CompositeBuilder<ProductComposite> productBuilder = uow
            .newEntityBuilder( null, ProductComposite.class );
        Product prototype = productBuilder.stateOfComposite();
        prototype.name().set( name );
        prototype.price().set( price );
        return productBuilder.newInstance();
    }

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices(
            NeoEntityStoreService.class,
            NeoCoreService.class,
            NeoIdentityService.class,
            DirectEntityStateFactory.class
        );
        module.addComposites(
            AccountComposite.class,
            CustomerComposite.class,
            LineItemComposite.class,
            OrderComposite.class,
            ProductComposite.class
        );
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        uow = unitOfWorkFactory.newUnitOfWork();
        txFactory = serviceLocator.findService( NeoTransactionService.class ).get();
    }

    @Override
    public void tearDown() throws Exception
    {
        uow.complete();
        System.out.println( "tearDown" );
        super.tearDown();
    }

    private static String entityIdOf( Object order )
    {
        return ( (EntityComposite) order ).identity().get();
    }
}
