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
package org.apache.polygene.runtime.entity;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Testing of equality of entity composites.
 */
public class EntityCompositeEqualityTest
    extends AbstractPolygeneTest
{
    private UnitOfWork unitOfWork;
    private EntityBuilder<MyComposite> myCompositeBuilder;

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
        unitOfWork = this.unitOfWorkFactory.newUnitOfWork();
        myCompositeBuilder = unitOfWork.newEntityBuilder( MyComposite.class );
    }

    @AfterEach
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
