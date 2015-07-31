/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.rdf.qi64.withPropagationRequiresNew;

import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.qi64.AbstractIssueTest;
import org.qi4j.index.rdf.qi64.AccountComposite;

import static org.junit.Assert.*;

public class IssueTest
    extends AbstractIssueTest
{
    private AccountService accountService;

    @Before
    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        accountService = module.findService( AccountService.class ).get();
    }

    @Test
    public final void testUnitOfWorkNotInitialized()
        throws Throwable
    {
        // Bootstrap the account
        String id = newQi4jAccount();

        // Make sure there's no unit of work
        assertFalse( module.isUnitOfWorkActive() );

        AccountComposite account = accountService.getAccountById( id );
        assertNotNull( account );

        assertFalse( module.isUnitOfWorkActive() );
    }

    @Test
    public final void testUnitOfWorkInitialized()
        throws Throwable
    {
        // Bootstrap the account
        String id = newQi4jAccount();

        // Make sure there's no unit of work
        assertFalse( module.isUnitOfWorkActive() );

        UnitOfWork parentUnitOfWork = module.newUnitOfWork();

        AccountComposite account = accountService.getAccountById( id );
        assertNotNull( account );

        UnitOfWork currentUnitOfWork = module.currentUnitOfWork();
        assertEquals( parentUnitOfWork, currentUnitOfWork );

        assertTrue( currentUnitOfWork.isOpen() );

        // Close the parent unit of work
        parentUnitOfWork.complete();
    }

    protected final void onAssemble( ModuleAssembly aModuleAssembly )
        throws AssemblyException
    {
        aModuleAssembly.services( AccountServiceComposite.class );
    }
}
