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

package org.apache.polygene.index.rdf.qi64.withPropagationRequiresNew;

import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.index.rdf.qi64.AbstractIssueTest;
import org.apache.polygene.index.rdf.qi64.AccountComposite;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class IssueTest
    extends AbstractIssueTest
{
    private AccountService accountService;

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();

        accountService = serviceFinder.findService( AccountService.class ).get();
    }

    @Test
    public final void testUnitOfWorkNotInitialized()
        throws Throwable
    {
        // Bootstrap the account
        Identity id = newPolygeneAccount();

        // Make sure there's no unit of work
        assertThat( unitOfWorkFactory.isUnitOfWorkActive(), is( false ) );

        AccountComposite account = accountService.getAccountById( id );
        assertThat( account, notNullValue() );

        assertThat( unitOfWorkFactory.isUnitOfWorkActive(), is( false ) );
    }

    @Test
    public final void testUnitOfWorkInitialized()
        throws Throwable
    {
        // Bootstrap the account
        Identity id = newPolygeneAccount();

        // Make sure there's no unit of work
        assertThat( unitOfWorkFactory.isUnitOfWorkActive(), is( false ) );

        UnitOfWork parentUnitOfWork = unitOfWorkFactory.newUnitOfWork();

        AccountComposite account = accountService.getAccountById( id );
        assertThat( account, notNullValue() );

        UnitOfWork currentUnitOfWork = unitOfWorkFactory.currentUnitOfWork();
        assertThat( currentUnitOfWork, equalTo( parentUnitOfWork ) );

        assertThat( currentUnitOfWork.isOpen(), is( true ) );

        // Close the parent unit of work
        parentUnitOfWork.complete();
    }

    protected final void onAssemble( ModuleAssembly aModuleAssembly )
        throws AssemblyException
    {
        aModuleAssembly.services( AccountServiceComposite.class );
    }
}
