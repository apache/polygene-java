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
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.memory.MemoryEntityStoreComposite;
import org.qi4j.spi.entity.UuidIdentityGeneratorComposite;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.entity.AccountComposite;
import org.qi4j.test.entity.CustomerComposite;
import org.qi4j.test.entity.OrderComposite;
import org.qi4j.test.entity.Product;
import org.qi4j.test.entity.ProductComposite;

/**
 * TODO
 */
public class UnitOfWorkFactoryTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( AccountComposite.class,
                              OrderComposite.class,
                              ProductComposite.class,
                              CustomerComposite.class );

        module.addServices( MemoryEntityStoreComposite.class,
                            UuidIdentityGeneratorComposite.class );
    }

    @Test
    public void testUnitOfWork()
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();

        // Create product
        CompositeBuilder<ProductComposite> cb = unitOfWork.newEntityBuilder( ProductComposite.class );
        cb.propertiesOfComposite().name().set( "Chair" );
        cb.propertiesOfComposite().price().set( 57 );
        Product chair = cb.newInstance();

        assertThat( "Chair.name()", chair.name().get(), equalTo( "Chair" ) );
        assertThat( "Chair.price()", chair.price().get(), equalTo( 57 ) );

        try
        {
            unitOfWork.complete();
        }
        catch( UnitOfWorkCompletionException e )
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testPrototypePattern()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();

        // Create product
        CompositeBuilder<ProductComposite> cb = unitOfWork.newEntityBuilder( ProductComposite.class );
        cb.propertiesOfComposite().name().set( "Chair" );
        cb.propertiesOfComposite().price().set( 57 );
        ProductComposite chair1 = cb.newInstance();
        ProductComposite chair2 = cb.newInstance();
        unitOfWork.complete();

        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        chair1 = unitOfWork.getReference( chair1 );
        chair2 = unitOfWork.getReference( chair2 );

        String id1 = chair1.identity().get();
        String id2 = chair2.identity().get();
        System.out.println( "Identity Chair1: " + id1 );
        System.out.println( "Identity Chair2: " + id2 );
        assertThat( "Identity are same.", id1, not( id2 ) );
    }
}
