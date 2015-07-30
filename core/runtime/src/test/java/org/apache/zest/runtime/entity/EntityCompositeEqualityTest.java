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
package org.apache.zest.runtime.entity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractQi4jTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Testing of equality of entity composites.
 */
public class EntityCompositeEqualityTest
    extends AbstractQi4jTest
{
    private UnitOfWork unitOfWork;
    private EntityBuilder<MyComposite> myCompositeBuilder;

    @Before
    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        unitOfWork = this.module.newUnitOfWork();
        myCompositeBuilder = unitOfWork.newEntityBuilder( MyComposite.class );
    }

    @After
    public void completeUnitOfWork()
        throws Exception
    {
        unitOfWork.complete();
        super.tearDown();
    }

    @Test
    public void shouldNotBeEqualToNull()
        throws UnitOfWorkCompletionException
    {
        MyComposite simpleComposite = myCompositeBuilder.newInstance();
        assertThat( "simpleComposite is not equal to null", simpleComposite.equals( null ), equalTo( false ) );
    }

    @Test
    public void shouldBeEqualToItself()
    {
        MyComposite simpleComposite = myCompositeBuilder.newInstance();
        assertThat( "simple composite is equal to itself", simpleComposite.equals( simpleComposite ), equalTo( true ) );
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().visibleIn( Visibility.layer ).assemble( module );
        module.objects( EntityCompositeEqualityTest.class );
        module.entities( MyComposite.class );
    }

    private static interface MyComposite
        extends EntityComposite
    {
    }
}
