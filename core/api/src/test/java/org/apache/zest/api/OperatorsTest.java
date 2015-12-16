/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.api;

import java.util.function.Predicate;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.junit.Assert;
import org.junit.Test;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.api.query.QueryExpressions;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;
import org.apache.zest.functional.Iterables;
import org.apache.zest.test.EntityTestAssembler;

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
                new DefaultUnitOfWorkAssembler().assemble( module );
            }
        };

        UnitOfWorkFactory uowf = assembler.module().unitOfWorkFactory();
        UnitOfWork uow = uowf.newUnitOfWork();

        try
        {
            EntityBuilder<TestEntity> entityBuilder = uow.newEntityBuilder( TestEntity.class, "123" );
            entityBuilder.instance().value().set( assembler.module().newValue( TestValue.class ) );
            TestEntity testEntity = entityBuilder.newInstance();

            uow.complete();
            uow = uowf.newUnitOfWork();

            Iterable<TestEntity> entities = Iterables.iterable( testEntity = uow.get( testEntity ) );

            QueryBuilder<TestEntity> builder = assembler.module().newQueryBuilder( TestEntity.class );

            {
                Predicate<Composite> where = QueryExpressions.eq( QueryExpressions.templateFor( TestEntity.class )
                                                                          .foo(), "Bar" );
                Assert.assertTrue( where.test( testEntity ) );
                System.out.println( where );
            }
            {
                Predicate<Composite> where = QueryExpressions.eq( QueryExpressions.templateFor( TestEntity.class )
                                                                          .value()
                                                                          .get()
                                                                          .bar(), "Xyz" );
                Assert.assertTrue( where.test( testEntity ) );
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
