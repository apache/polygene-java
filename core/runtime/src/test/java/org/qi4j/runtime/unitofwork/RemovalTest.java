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

package org.qi4j.runtime.unitofwork;

import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RemovalTest
    extends AbstractQi4jTest
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
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            EntityBuilder<Abc> builder = uow.newEntityBuilder( Abc.class, "123" );
            builder.instance().name().set( "Niclas" );
            builder.newInstance();
            uow.complete();
            uow = module.newUnitOfWork();
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
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            EntityBuilder<Abc> builder = uow.newEntityBuilder( Abc.class, "123" );
            builder.instance().name().set( "Niclas" );
            builder.newInstance();
            uow.complete();
            uow = module.newUnitOfWork();
            Abc abc = uow.get( Abc.class, "123" );
            assertEquals( "Niclas", abc.name().get() );
            uow.remove( abc );
            uow.complete();
            uow = module.newUnitOfWork();
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
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            EntityBuilder<Abc> builder = uow.newEntityBuilder( Abc.class, "123" );
            builder.instance().name().set( "Niclas" );
            Abc abc = builder.newInstance();
            uow.complete();
            uow = module.newUnitOfWork();
            abc = uow.get( abc );  // Attach the detached entity to 'uow' session.
            uow.remove( abc );
            uow.complete();
            uow = module.newUnitOfWork();
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
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            EntityBuilder<Abc> builder = uow.newEntityBuilder( Abc.class, "123" );
            builder.instance().name().set( "Niclas" );
            Abc abc = builder.newInstance();
            uow.remove( abc );
            uow.complete();
            uow = module.newUnitOfWork();
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
