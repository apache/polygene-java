/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.apache.zest.runtime.unitofwork;

import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.EntityTypeNotFoundException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.Energy4Java;
import org.apache.zest.test.EntityTestAssembler;
import org.junit.Test;

import static org.apache.zest.api.common.Visibility.application;
import static org.junit.Assert.fail;

/**
 * JAVADOC
 */
public class PrivateEntityUnitOfWorkTest
{
    @Structure
    private UnitOfWorkFactory uowf;

    @Test
    public void givenAppWithPrivateEntityWhenUnitOfWorkCanSeeItThenCanCommit()
        throws Exception
    {
        System.setProperty( "zest.compacttrace", "off" );

        Energy4Java is = new Energy4Java();
        Application app = is.newApplication(
            applicationFactory ->
                applicationFactory.newApplicationAssembly( new Assembler[][][]{
                    {
                        {
                            module -> {
                                module.objects( PrivateEntityUnitOfWorkTest.class );
                            }
                        }
                    },
                    {
                        {
                            module -> {
                                module.entities( ProductEntity.class );
                                module.entities( ProductCatalogEntity.class ).visibleIn( application );
                                module.values( ProductInfo.class );
                                new EntityTestAssembler().assemble( module );
                            }
                        }
                    }
                } ) );
        app.activate();

        Module module = app.findModule( "Layer 1", "Module 1" );
        module.injectTo( this );

        UnitOfWork unitOfWork = uowf.newUnitOfWork();

        try
        {
            unitOfWork.newEntity( ProductEntity.class );
            fail( "Should not be able to create product here" );
        }
        catch( EntityTypeNotFoundException e )
        {
            // Ok
            ProductCatalog catalog = unitOfWork.newEntity( ProductCatalog.class, "1" );
            unitOfWork.complete();
        }
        unitOfWork = uowf.newUnitOfWork();

        String id;
        try
        {
            ProductCatalog catalog = unitOfWork.get( ProductCatalog.class, "1" );
            id = ( (Identity) catalog.newProduct() ).identity().get();
            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }

        unitOfWork = uowf.newUnitOfWork();
        try
        {
            ProductCatalog catalog = unitOfWork.get( ProductCatalog.class, "1" );
            Product product = catalog.findProduct( id );
            product.price().set( 100 );
            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    public interface ProductCatalog
    {
        Product newProduct();

        Product findProduct( String id );
    }

    @Mixins( ProductCatalogEntity.ProductRepositoryMixin.class )
    interface ProductCatalogEntity
        extends ProductCatalog, EntityComposite
    {
        abstract class ProductRepositoryMixin
            implements ProductCatalog
        {
            @Structure
            private UnitOfWorkFactory uowf;

            @Structure
            private ValueBuilderFactory vbf;

            public Product newProduct()
            {
                ValueBuilder<ProductInfo> vb = vbf.newValueBuilder( ProductInfo.class );
                vb.prototype().description().set( "Some mundane description" );
                vb.prototype().weight().set( 1.0f );
                ProductInfo info = vb.newInstance();

                UnitOfWork uow = uowf.currentUnitOfWork();
                EntityBuilder<Product> eb = uow.newEntityBuilder( Product.class );
                eb.instance().name().set( "Product Name" );
                eb.instance().price().set( 100 );
                eb.instance().productInfo().set( info );
                return eb.newInstance();
            }

            public Product findProduct( String id )
            {
                UnitOfWork uow = uowf.currentUnitOfWork();
                return uow.get( Product.class, id );
            }
        }
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

    public interface Order
    {
        Association<Customer> customer();

        ManyAssociation<LineItem> lineItems();
    }

    public interface OrderComposite
        extends Order, EntityComposite
    {
    }

    public interface ProductInfo
        extends ValueComposite
    {
        Property<String> description();

        Property<Float> weight();
    }

    public interface Product
    {
        Property<String> name();

        Property<Integer> price();

        Property<ProductInfo> productInfo();
    }

    public interface ProductEntity
        extends Product, EntityComposite
    {
    }
}