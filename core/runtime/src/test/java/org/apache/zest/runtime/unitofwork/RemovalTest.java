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

package org.apache.zest.runtime.unitofwork;

import org.junit.Test;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RemovalTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( Abc.class );
    }

    @Test
    public void givenUnitOfWorkHasBeenCreateWhenCreatingNewEntityThenFindNewEntityWithGet()
        throws Exception
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            EntityBuilder<Abc> builder = uow.newEntityBuilder( Abc.class, "123" );
            builder.instance().name().set( "Niclas" );
            builder.newInstance();
            uow.complete();
            uow = uowf.newUnitOfWork();
            Abc abc = uow.get( Abc.class, "123" );
            assertEquals( "Niclas", abc.name().get() );
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
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            EntityBuilder<Abc> builder = uow.newEntityBuilder( Abc.class, "123" );
            builder.instance().name().set( "Niclas" );
            builder.newInstance();
            uow.complete();
            uow = uowf.newUnitOfWork();
            Abc abc = uow.get( Abc.class, "123" );
            assertEquals( "Niclas", abc.name().get() );
            uow.remove( abc );
            uow.complete();
            uow = uowf.newUnitOfWork();
            uow.get( Abc.class, "123" );
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
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            EntityBuilder<Abc> builder = uow.newEntityBuilder( Abc.class, "123" );
            builder.instance().name().set( "Niclas" );
            Abc abc = builder.newInstance();
            uow.complete();
            uow = uowf.newUnitOfWork();
            abc = uow.get( abc );  // Attach the detached entity to 'uow' session.
            uow.remove( abc );
            uow.complete();
            uow = uowf.newUnitOfWork();
            uow.get( Abc.class, "123" );
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
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            EntityBuilder<Abc> builder = uow.newEntityBuilder( Abc.class, "123" );
            builder.instance().name().set( "Niclas" );
            Abc abc = builder.newInstance();
            uow.remove( abc );
            uow.complete();
            uow = uowf.newUnitOfWork();
            uow.get( Abc.class, "123" );
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
