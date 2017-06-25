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
package org.apache.polygene.runtime.structure;

import org.apache.polygene.api.composite.NoSuchTransientTypeException;
import org.apache.polygene.api.unitofwork.NoSuchEntityTypeException;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.value.NoSuchValueTypeException;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.memory.MemoryEntityStoreService;
import org.apache.polygene.test.entity.model.AbstractPolygeneMultiLayeredTestWithModel;
import org.apache.polygene.test.entity.model.monetary.CheckBookSlip;
import org.apache.polygene.test.entity.model.monetary.Currency;
import org.apache.polygene.test.entity.model.people.Person;
import org.apache.polygene.test.entity.model.people.Rent;
import org.junit.Test;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class NoSuchEntityTypeExceptionTest extends AbstractPolygeneMultiLayeredTestWithModel
{
    @Test
    public void givenNoVisibilityWhenCreatingValueExpectClearException()
    {
        try
        {
            ValueBuilder<Rent> builder = valueBuilderFactory.newValueBuilder( Rent.class );
            fail( NoSuchValueTypeException.class.getSimpleName() + " should have been thrown." );
        }
        catch( NoSuchValueTypeException e )
        {
            String expectedString = "\tInvisible ValueComposite types are:" + System.getProperty( "line.separator" )
                                    + "\t\t[ org.apache.polygene.test.entity.model.people.Rent] in [People Module] with visibility module";
            assertThat( e.getMessage(), containsString( expectedString ) );
        }
    }

    @Test
    public void givenNoVisibilityWhenCreatingTransientExpectClearException()
    {
        try
        {
            CheckBookSlip slip = transientBuilderFactory.newTransient( CheckBookSlip.class );
            fail( NoSuchTransientTypeException.class.getSimpleName() + " should have been thrown." );
        }
        catch( NoSuchTransientTypeException e )
        {
            String expectedString = "\tInvisible TransientComposite types are:" + System.getProperty( "line.separator" )
                                    + "\t\t[ org.apache.polygene.test.entity.model.monetary.CheckBookSlip] in [Monetary Module] with visibility module";
            assertThat( e.getMessage(), containsString( expectedString ) );
        }
    }

    @Test
    public void givenNoVisibilityWhenCreatingEntityExpectClearException()
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            Person p = uow.newEntity( Person.class );
            fail( NoSuchEntityTypeException.class.getSimpleName() + " should have been thrown." );
        }
        catch( NoSuchEntityTypeException e )
        {
            String expectedString = "\tInvisible EntityComposite types are:" + System.getProperty( "line.separator" )
                                    + "\t\t[ org.apache.polygene.test.entity.model.people.Person] in [People Module] with visibility layer";
            assertThat( e.getMessage(), containsString( expectedString ) );
        }
    }

    protected void defineStorageModule( ModuleAssembly module )
    {
        module.defaultServices();
        module.services( MemoryEntityStoreService.class ).instantiateOnStartup();
    }
}
