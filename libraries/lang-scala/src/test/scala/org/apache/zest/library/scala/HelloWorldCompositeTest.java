/*
 * Copyright 2011 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.apache.zest.library.scala;

import org.junit.Assert;
import org.junit.Test;
import org.apache.zest.api.constraint.ConstraintViolationException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;
import org.apache.zest.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.apache.zest.spi.query.IndexExporter;
import org.apache.zest.test.EntityTestAssembler;

import static org.apache.zest.api.query.QueryExpressions.eq;
import static org.apache.zest.api.query.QueryExpressions.templateFor;

/**
 * TODO
 */
public class HelloWorldCompositeTest
{
    @Test
    public void testComposite()
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                // START SNIPPET: composite
                module.transients( HelloWorldComposite.class, HelloWorldComposite2.class ).
                    withMixins( ScalaTraitMixin.class ).
                    withConcerns( ExclamationGenericConcern.class );
                // END SNIPPET: composite
            }
        };

        HelloWorldComposite composite = assembler.module().newTransient( HelloWorldComposite.class );
        Assert.assertEquals( "Do stuff!", composite.doStuff() );
        Assert.assertEquals( "Hello there World!", composite.sayHello( "World" ) );

        try
        {
            composite.sayHello( "AReallyReallyLongName" );
        }
        catch( ConstraintViolationException e )
        {
            // Ok!
        }

        HelloWorldComposite2 composite2 = assembler.module().newTransient( HelloWorldComposite2.class );
        Assert.assertEquals( "Do custom stuff!", composite2.doStuff() );
    }

    @Test
    public void testEntity()
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                // START SNIPPET: entity
                module.entities( TestEntity.class ).withMixins( ScalaTraitMixin.class );
                // END SNIPPET: entity
                // START SNIPPET: service
                module.services( TestService.class ).withMixins( ScalaTraitMixin.class );
                // END SNIPPET: service

                new EntityTestAssembler().assemble( module );
                new RdfMemoryStoreAssembler().assemble( module );
            }
        };

        // Create and update Entity
        UnitOfWork uow = assembler.module().unitOfWorkFactory().newUnitOfWork();
        try
        {
            Commands entity = uow.newEntity( Commands.class );
            entity.updateFoo( "Foo" );

            Data data = uow.get( Data.class, entity.toString() );

            Assert.assertEquals( "FooFoo", data.foo().get() );
        }
        finally
        {
            uow.complete();
        }

        assembler.module().findService( IndexExporter.class ).get().exportReadableToStream( System.out );

        // Find it
        uow = assembler.module().unitOfWorkFactory().newUnitOfWork();
        try
        {
            Data data = uow.newQuery( assembler.module()
                                          .newQueryBuilder( Data.class )
                                          .where( eq( templateFor( Data.class ).foo(), "FooFoo" ) ) ).find();
            Assert.assertEquals( "FooFoo", data.foo().get() );
        }
        finally
        {
            uow.discard();
        }
    }
}
