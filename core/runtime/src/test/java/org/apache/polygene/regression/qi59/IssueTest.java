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

package org.apache.polygene.regression.qi59;

import org.junit.Test;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.constraints.annotation.NotEmpty;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;

import static org.junit.Assert.fail;

/**
 * Test for QI-59
 */
public class IssueTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( TestCase.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void givenEntityWithConstrainedPropertyWhenInvalidPropertyValueSetThenThrowException()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        try
        {
            TestCase testCase = uow.newEntity( TestCase.class );

            testCase.someProperty().set( null );

            uow.complete();
            fail( "Should not be allowed to set invalid property value" );
        }
        catch( Exception e )
        {
            uow.discard();
        }
    }

    @Test
    public void givenEntityWithComplexConstrainedPropertyWhenInvalidPropertyValueSetThenThrowException()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        try
        {
            TestCase testCase = uow.newEntity( TestCase.class );

            testCase.otherProperty().set( "" );

            uow.complete();
            fail( "Should not be allowed to set invalid property value" );
        }
        catch( Exception e )
        {
            uow.discard();
        }
    }

    interface TestCase
        extends EntityComposite
    {
        Property<String> someProperty();

        @NotEmpty
        Property<String> otherProperty();
    }
}