package org.qi4j.api;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Operators;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.test.EntityTestAssembler;

import static org.qi4j.api.query.Operators.constant;
import static org.qi4j.api.query.Operators.eq;
import static org.qi4j.api.query.Operators.template;

/**
 * TODO
 */
public class OperatorsTest
{
    @Test
    public void testOperators() throws UnitOfWorkCompletionException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                new EntityTestAssembler(  ).assemble( module );

                module.entities( TestEntity.class );
                module.values( TestValue.class );
                module.forMixin( TestEntity.class ).declareDefaults().foo().set( "Bar" );
                module.forMixin( TestValue.class ).declareDefaults().bar().set( "Xyz" );
            }
        };

        UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork();

        EntityBuilder<TestEntity> entityBuilder = uow.newEntityBuilder( TestEntity.class, "123" );
        entityBuilder.instance().value().set( assembler.valueBuilderFactory().newValue( TestValue.class ) );
        TestEntity testEntity = entityBuilder.newInstance();

        uow.complete();
        uow = assembler.unitOfWorkFactory().newUnitOfWork();

        Iterable<TestEntity> entities = Iterables.iterable( testEntity = uow.get(testEntity) );

        QueryBuilder<TestEntity> builder = assembler.queryBuilderFactory().newQueryBuilder( TestEntity.class );

        {
            Specification<Entity> where = eq( template( TestEntity.class ).foo(), constant( "Bar" ) );
            Assert.assertTrue( where.satisfiedBy( testEntity ) );
            System.out.println(where);
        }
        {
            Specification<Entity> where = eq( template( TestEntity.class ).value().get().bar(), constant( "Xyz" ) );
            Assert.assertTrue( where.satisfiedBy( testEntity ) );
            System.out.println(where);

            Assert.assertTrue(builder.where( Operators.expression( where ) ).newQuery( entities ).find().equals( testEntity ));
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
