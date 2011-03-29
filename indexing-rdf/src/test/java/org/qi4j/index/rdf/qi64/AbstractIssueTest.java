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
package org.qi4j.index.rdf.qi64;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

public abstract class AbstractIssueTest
    extends AbstractQi4jTest
{
    private static final String DEFAULT_ACCOUNT_NAME = "qi4j";

    /**
     * Creates a new qi4j account.
     *
     * @return The new account identity.
     *
     * @throws org.qi4j.api.unitofwork.UnitOfWorkCompletionException
     *          Thrown if creational fail.
     */
    protected final String newQi4jAccount()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        EntityBuilder<AccountComposite> entityBuilder = work.newEntityBuilder( AccountComposite.class );
        AccountComposite accountComposite = entityBuilder.instance();
        accountComposite.name().set( DEFAULT_ACCOUNT_NAME );
        accountComposite = entityBuilder.newInstance();

        String identity = accountComposite.identity().get();
        work.complete();
        return identity;
    }

    public final void assemble( ModuleAssembly aModuleAssembly )
        throws AssemblyException
    {
        aModuleAssembly.entities( AccountComposite.class );
        aModuleAssembly.services( MemoryEntityStoreService.class,
                                  UuidIdentityGeneratorService.class,
                                  MemoryRepositoryService.class );
        onAssemble( aModuleAssembly );
    }

    protected abstract void onAssemble( ModuleAssembly aModuleAssembly )
        throws AssemblyException;
}
