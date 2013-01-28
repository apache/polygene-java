package org.qi4j.api;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.test.EntityTestAssembler;

/**
 * TODO
 */
public class OperatorsTest
{
    @Test
    public void testOperators()
        throws UnitOfWorkCompletionException, ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                new EntityTestAssembler().assemble( module );

                module.entities( TestEntity.class );
                module.values( TestValue.class );
                module.forMixin( TestEntity.class ).declareDefaults().foo().set( "Bar" );
                module.forMixin( TestValue.class ).declareDefaults().bar().set( "Xyz" );
            }
        };

        UnitOfWork uow = assembler.module().newUnitOfWork();

        try
        {
            EntityBuilder<TestEntity> entityBuilder = uow.newEntityBuilder( TestEntity.class, "123" );
            entityBuilder.instance().value().set( assembler.module().newValue( TestValue.class ) );
            TestEntity testEntity = entityBuilder.newInstance();

            uow.complete();
            uow = assembler.module().newUnitOfWork();

            Iterable<TestEntity> entities = Iterables.iterable( testEntity = uow.get( testEntity ) );

            QueryBuilder<TestEntity> builder = assembler.module().newQueryBuilder( TestEntity.class );

            {
                Specification<Composite> where = QueryExpressions.eq( QueryExpressions.templateFor( TestEntity.class )
                                                                          .foo(), "Bar" );
                Assert.assertTrue( where.satisfiedBy( testEntity ) );
                System.out.println( where );
            }
            {
                Specification<Composite> where = QueryExpressions.eq( QueryExpressions.templateFor( TestEntity.class )
                                                                          .value()
                                                                          .get()
                                                                          .bar(), "Xyz" );
                Assert.assertTrue( where.satisfiedBy( testEntity ) );
                System.out.println( where );

                Assert.assertTrue( builder.where( where ).newQuery( entities ).find().equals( testEntity ) );
            }
        }
        finally
        {
            uow.discard();
        }
    }

    public interface TestEntity
        extends EntityComposite
    {
        Property<String> foo();

        Property<TestValue> value();
    }

    public interface TestValue
        extends ValueComposite
    {
        Property<String> bar();
    }
}
