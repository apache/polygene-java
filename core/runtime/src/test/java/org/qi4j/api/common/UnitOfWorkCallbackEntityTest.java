/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.api.common;

import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.fail;

/**
 * Test UnitOfWorkCallback and Validatable
 */
public class UnitOfWorkCallbackEntityTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( TestCase.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    @Ignore( "Validation is moved to sandbox, and UoW is under massive refactoring." )
    public void givenCompositeWithValidatableWhenUnitCompletesThenPerformValidation()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = module.newUnitOfWork();
        TestCase test = uow.newEntity( TestCase.class );

        try
        {
            uow.complete();
            fail( "Validation did not occur" );
        }
        finally
        {
            uow.discard();
        }
    }

    //    @Concerns( TestValidatableConcern.class )

    interface TestCase
        extends EntityComposite  //, ValidatableAbstractComposite
    {
    }
/*
    public static abstract class TestValidatableConcern extends AbstractValidatableConcern
    {
        @Override protected void isValid( Validator validator )
        {
            validator.error( true, "Validation error" );
        }

        @Override protected String getResourceBundle()
        {
            return null;
        }
    }
*/
}
