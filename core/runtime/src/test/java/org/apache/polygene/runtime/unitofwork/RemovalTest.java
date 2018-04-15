/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime.unitofwork;

import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.NoSuchEntityException;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

public class RemovalTest
    extends AbstractPolygeneTest
{

    private static final Identity TEST_IDENTITY = StringIdentity.identityOf( "123" );

    public void assemble(ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( Abc.class );
    }

    @Test
    public void givenUnitOfWorkHasBeenCreateWhenCreatingNewEntityThenFindNewEntityWithGet()
        throws Exception
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<Abc> builder = uow.newEntityBuilder( Abc.class, TEST_IDENTITY);
            builder.instance().name().set( "Niclas" );
            builder.newInstance();
            uow.complete();
            uow = unitOfWorkFactory.newUnitOfWork();
            Abc abc = uow.get( Abc.class, TEST_IDENTITY);
            assertThat( abc.name().get(), equalTo( "Niclas" ) );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenEntityCreatedWhenRemovingEntityThenFindNewEntityShouldNotExist()
        throws Exception
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<Abc> builder = uow.newEntityBuilder( Abc.class, TEST_IDENTITY);
            builder.instance().name().set( "Niclas" );
            builder.newInstance();
            uow.complete();
            uow = unitOfWorkFactory.newUnitOfWork();
            Abc abc = uow.get( Abc.class, TEST_IDENTITY);
            assertThat( abc.name().get(), equalTo( "Niclas" ) );
            uow.remove( abc );
            uow.complete();
            uow = unitOfWorkFactory.newUnitOfWork();
            uow.get( Abc.class, TEST_IDENTITY);
            fail( "This '123' entity should not exist." );
        }
        catch( NoSuchEntityException e )
        {
            // Expected.
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenDetachedEntityWhenRemovingEntityThenFindNewEntityShouldNotExist()
        throws Exception
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<Abc> builder = uow.newEntityBuilder( Abc.class, TEST_IDENTITY);
            builder.instance().name().set( "Niclas" );
            Abc abc = builder.newInstance();
            uow.complete();
            uow = unitOfWorkFactory.newUnitOfWork();
            abc = uow.get( abc );  // Attach the detached entity to 'uow' session.
            uow.remove( abc );
            uow.complete();
            uow = unitOfWorkFactory.newUnitOfWork();
            uow.get( Abc.class, TEST_IDENTITY);
            fail( "This '123' entity should not exist." );
        }
        catch( NoSuchEntityException e )
        {
            // Expected.
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenEntityCreatedWhenRemovingEntityBeforeCompletingUowThenFindNewEntityShouldNotExist()
        throws Exception
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<Abc> builder = uow.newEntityBuilder( Abc.class, TEST_IDENTITY);
            builder.instance().name().set( "Niclas" );
            Abc abc = builder.newInstance();
            uow.remove( abc );
            uow.complete();
            uow = unitOfWorkFactory.newUnitOfWork();
            uow.get( Abc.class, TEST_IDENTITY);
            fail( "This '123' entity should not exist." );
        }
        catch( NoSuchEntityException e )
        {
            // Expected.
        }
        finally
        {
            uow.discard();
        }
    }

    public interface Abc
        extends EntityComposite
    {
        Property<String> name();
    }
}
