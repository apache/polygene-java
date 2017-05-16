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
package org.apache.polygene.api;

import java.util.Collections;
import java.util.function.Predicate;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.query.QueryExpressions;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.apache.polygene.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.Assert;
import org.junit.Test;

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
            EntityBuilder<TestEntity> entityBuilder = uow.newEntityBuilder( TestEntity.class, new StringIdentity( "123" ) );
            entityBuilder.instance().value().set( assembler.module().newValue( TestValue.class ) );
            TestEntity testEntity = entityBuilder.newInstance();

            uow.complete();
            uow = uowf.newUnitOfWork();

            Iterable<TestEntity> entities = Collections.singleton( testEntity = uow.get( testEntity ) );

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
