package org.qi4j.sample.scala;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.lang.scala.ScalaTraitMixin;
import org.qi4j.spi.query.IndexExporter;
import org.qi4j.test.EntityTestAssembler;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * TODO
 */
public class HelloWorldCompositeTest
{
    @Test
    public void testComposite()
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( HelloWorldComposite.class, HelloWorldComposite2.class ).
                    withMixins( ScalaTraitMixin.class ).
                    withConcerns( ExclamationGenericConcern.class );
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
        throws UnitOfWorkCompletionException, IOException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.entities( TestEntity.class ).withMixins( ScalaTraitMixin.class );
                module.services( TestService.class ).withMixins( ScalaTraitMixin.class );

                new EntityTestAssembler().assemble( module );
                new RdfMemoryStoreAssembler().assemble( module );
            }
        };

        // Create and update Entity
        UnitOfWork uow = assembler.module().newUnitOfWork();
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
        uow = assembler.module().newUnitOfWork();
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
