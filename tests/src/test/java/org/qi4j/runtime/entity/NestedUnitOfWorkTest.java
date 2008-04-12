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

package org.qi4j.runtime.entity;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.entity.AccountComposite;
import org.qi4j.test.entity.CustomerComposite;
import org.qi4j.test.entity.OrderComposite;
import org.qi4j.test.entity.Product;
import org.qi4j.test.entity.ProductComposite;

/**
 * TODO
 */
public class NestedUnitOfWorkTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( AccountComposite.class,
                              OrderComposite.class,
                              ProductComposite.class,
                              CustomerComposite.class );

        module.addServices( MemoryEntityStoreService.class,
                            UuidIdentityGeneratorService.class );
    }

    @Test
    public void whenNestedUnitOfWorkThenReturnCorrectPropertyValues()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();

        assertThat( "Current UnitOfWork is not set.", unitOfWork, equalTo( unitOfWorkFactory.currentUnitOfWork() ) );

        // Create product
        CompositeBuilder<ProductComposite> cb = unitOfWork.newEntityBuilder( ProductComposite.class );
        cb.stateOfComposite().name().set( "Chair" );
        cb.stateOfComposite().price().set( 57 );
        Product chair = cb.newInstance();

        assertThat( "Initial property is not correct", chair.price().get(), equalTo( 57 ) );

        // Create nested unitOfWork
        UnitOfWork nestedUnitOfWork = unitOfWork.newUnitOfWork();
        assertThat( "Current UnitOfWork is not set correctly.", nestedUnitOfWork, equalTo( unitOfWorkFactory.currentUnitOfWork() ) );

        Product nestedChair = nestedUnitOfWork.dereference( chair );
        assertThat( "Nested property is correct", chair.price().get(), equalTo( 57 ) );

        nestedChair.price().set( 60 );

        Property<Integer> originalPrice = chair.price();
        assertThat( "Initial property has not changed", originalPrice.get(), equalTo( 57 ) );

        assertThat( "Nested property has changed", nestedChair.price().get(), equalTo( 60 ) );

        nestedUnitOfWork.complete();
        assertThat( "Current UnitOfWork is not popped correctly.", unitOfWork, equalTo( unitOfWorkFactory.currentUnitOfWork() ) );

        assertThat( "Initial property has been updated", originalPrice.get(), equalTo( 60 ) );

        unitOfWork.complete();

        assertThat( "Current UnitOfWork is not reset.", null, equalTo( unitOfWorkFactory.currentUnitOfWork() ) );
    }
}