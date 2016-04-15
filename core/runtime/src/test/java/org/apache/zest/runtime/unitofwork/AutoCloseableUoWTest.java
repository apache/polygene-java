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

import org.junit.After;
import org.junit.Test;
import org.apache.zest.api.constraint.ConstraintViolationException;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Assert that Automatic Resource Management (ie. Java 7 try-with-resources) work on UoWs.
 */
public class AutoCloseableUoWTest
    extends AbstractZestTest
{

    public interface TestEntity
    {

        Property<String> mandatory();

    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( TestEntity.class );
    }

    @Test
    public void givenGoodAutoCloseableUoWWhenTryWithResourceExpectSuccess()
        throws UnitOfWorkCompletionException
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            EntityBuilder<TestEntity> builder = uow.newEntityBuilder( TestEntity.class );
            builder.instance().mandatory().set( "Mandatory property" );
            builder.newInstance();
            uow.complete();
        }
    }

    @Test( expected = ConstraintViolationException.class )
    public void givenWrongAutoCloseableUoWWhenTryWithResourceExpectSuccess()
        throws UnitOfWorkCompletionException
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            uow.newEntity( TestEntity.class );
            uow.complete();
        }
    }

    @After
    public void afterEachTest()
    {
        assertThat( unitOfWorkFactory.isUnitOfWorkActive(), is( false ) );
    }

}
