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
package org.apache.zest.index.rdf.qi64;

import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.rdf.repository.MemoryRepositoryService;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

public abstract class AbstractIssueTest
    extends AbstractZestTest
{
    private static final String DEFAULT_ACCOUNT_NAME = "zest";

    /**
     * Creates a new Apaxhe Zest account.
     *
     * @return The new account identity.
     *
     * @throws org.apache.zest.api.unitofwork.UnitOfWorkCompletionException
     *          Thrown if creational fail.
     */
    protected final String newZestAccount()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork work = uowf.newUnitOfWork();
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
        new EntityTestAssembler().assemble( aModuleAssembly );
        aModuleAssembly.services( MemoryRepositoryService.class );
        onAssemble( aModuleAssembly );
    }

    protected abstract void onAssemble( ModuleAssembly aModuleAssembly )
        throws AssemblyException;
}
