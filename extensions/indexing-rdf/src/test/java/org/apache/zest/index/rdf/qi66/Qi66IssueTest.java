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
package org.apache.zest.index.rdf.qi66;

import org.apache.zest.api.identity.Identity;
import org.junit.Test;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.rdf.query.RdfQueryParserFactory;
import org.apache.zest.index.rdf.query.RdfQueryService;
import org.apache.zest.library.rdf.entity.EntityStateSerializer;
import org.apache.zest.library.rdf.entity.EntityTypeSerializer;
import org.apache.zest.library.rdf.repository.MemoryRepositoryService;
import org.apache.zest.test.AbstractPolygeneTest;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test for Qi-66
 */
public class Qi66IssueTest
    extends AbstractPolygeneTest
{
    private static final String ACCOUNT_NAME = "zest";

    @Test
    public final void testCompleteAfterFind()
        throws Exception
    {
        Identity accountIdentity = newPolygeneAccount();

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
     * Creates a new Apache Polygene account.
     *
     * @return The reference of Polygene account.
     *
     * @throws UnitOfWorkCompletionException Thrown if creational fail.
     */
    private Identity newPolygeneAccount()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        EntityBuilder<AccountComposite> entityBuilder = work.newEntityBuilder( AccountComposite.class );
        AccountComposite accountComposite = entityBuilder.instance();
        accountComposite.name().set( ACCOUNT_NAME );
        accountComposite = entityBuilder.newInstance();
        Identity accoutnIdentity = accountComposite.identity().get();
        work.complete();

        return accoutnIdentity;
    }

    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( AccountComposite.class );
        new EntityTestAssembler().assemble( module );
        module.services( RdfQueryService.class,
                         RdfQueryParserFactory.class,
                         MemoryRepositoryService.class );
        module.services( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
        module.objects( EntityStateSerializer.class, EntityTypeSerializer.class );
    }
}
