/*
 * Copyright (c) 2012, Paul Merlin.
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
package org.qi4j.runtime.structure;

import org.junit.Test;
import org.qi4j.api.composite.NoSuchTransientException;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.object.NoSuchObjectException;
import org.qi4j.api.service.NoSuchServiceException;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.NoSuchValueException;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.fail;

/**
 * This test assert that Type to Composite lookup succeed for Objects, Transients, Values, Entities and Services.
 */
public class TypeToCompositeLookupTest
        extends AbstractQi4jTest
{

    public interface Foo
    {
    }

    public interface FooObject
            extends Foo
    {
    }

    public static class SomeFooObject
            implements FooObject
    {
    }

    public interface FooTransient
            extends Foo, TransientComposite
    {
    }

    public interface SomeFooTransient
            extends FooTransient
    {
    }

    public interface FooValue
            extends Foo, ValueComposite
    {
    }

    public interface SomeFooValue
            extends FooValue
    {
    }

    public interface FooEntity
            extends Foo, EntityComposite
    {
    }

    public interface SomeFooEntity
            extends FooEntity
    {
    }

    public interface FooService
            extends Foo, ServiceComposite
    {
    }

    public interface SomeFooService
            extends FooService
    {
    }

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );

        module.objects( SomeFooObject.class );
        module.transients( SomeFooTransient.class );
        module.values( SomeFooValue.class );
        module.entities( SomeFooEntity.class );
        module.services( SomeFooService.class );
    }

    @Test
    public void objectsTypeToImplementationLookup()
    {
        try {

            module.newObject( SomeFooObject.class );
            module.newObject( FooObject.class );
            module.newObject( Foo.class );

        } catch ( NoSuchObjectException ex ) {

            ex.printStackTrace();
            fail( "Type to Composite lookup failed for Objects" );

        }
    }

    @Test
    public void transientsTypeToCompositeLookup()
    {
        try {

            module.newTransientBuilder( SomeFooTransient.class );
            module.newTransientBuilder( FooTransient.class );
            module.newTransientBuilder( Foo.class );

        } catch ( NoSuchTransientException ex ) {

            ex.printStackTrace();
            fail( "Type to Composite lookup failed for Transients" );

        }
    }

    @Test
    public void valuesTypeToCompositeLookup()
    {
        try {

            module.newValueBuilder( SomeFooValue.class );
            module.newValueBuilder( FooValue.class );
            module.newValueBuilder( Foo.class );

        } catch ( NoSuchValueException ex ) {

            ex.printStackTrace();
            fail( "Type to Composite lookup failed for Values" );

        }
    }

    @Test
    public void entitiesTypeToCompositeLookup()
    {
        UnitOfWork uow = module.newUnitOfWork();
        try {

            uow.newEntityBuilder( SomeFooEntity.class );
            uow.newEntityBuilder( FooEntity.class );
            uow.newEntityBuilder( Foo.class );

        } catch ( NoSuchEntityException ex ) {

            ex.printStackTrace();
            fail( "Type to Composite lookup failed for Entities" );

        } finally {

            uow.discard();

        }
    }

    @Test
    public void servicesTypeToCompositeLookup()
    {
        try {

            module.findService( SomeFooService.class );
            module.findService( FooService.class );
            module.findService( Foo.class );

        } catch ( NoSuchServiceException ex ) {

            ex.printStackTrace();
            fail( "Type to Composite lookup failed for Services" );

        }
    }

}
