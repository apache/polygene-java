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
package org.qi4j.index.rdf.qi66;

import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.query.RdfQueryParserFactory;
import org.qi4j.index.rdf.query.RdfQueryService;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static junit.framework.Assert.*;

/**
 * Test for Qi-66
 */
public class Qi66IssueTest
    extends AbstractQi4jTest
{
    private static final String ACCOUNT_NAME = "qi4j";

    @Test
    public final void testCompleteAfterFind()
        throws Exception
    {
        String accountIdentity = newQi4jAccount();

        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        AccountComposite account = work.get( AccountComposite.class, accountIdentity );
        assertNotNull( account );

        try
        {
            work.complete();
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            fail( "No exception can be thrown." );
        }
    }

    /**
     * Creates a new qi4j account.
     *
     * @return The identity of qi4j account.
     *
     * @throws UnitOfWorkCompletionException Thrown if creational fail.
     */
    private String newQi4jAccount()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        EntityBuilder<AccountComposite> entityBuilder = work.newEntityBuilder( AccountComposite.class );
        AccountComposite accountComposite = entityBuilder.instance();
        accountComposite.name().set( ACCOUNT_NAME );
        accountComposite = entityBuilder.newInstance();
        String accoutnIdentity = accountComposite.identity().get();
        work.complete();

        return accoutnIdentity;
    }

    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( AccountComposite.class );
        new EntityTestAssembler().assemble( module );
        module.services( RdfQueryService.class, RdfQueryParserFactory.class,
                         MemoryRepositoryService.class );
        module.objects( EntityStateSerializer.class, EntityTypeSerializer.class );
    }
}
