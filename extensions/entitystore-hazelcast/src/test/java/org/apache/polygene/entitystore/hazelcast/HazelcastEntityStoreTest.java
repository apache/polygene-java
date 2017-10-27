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
package org.apache.polygene.entitystore.hazelcast;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.hazelcast.assembly.HazelcastEntityStoreAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.entity.AbstractEntityStoreTest;
import org.junit.After;
import org.junit.Test;

public class HazelcastEntityStoreTest
    extends AbstractEntityStoreTest
{

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // END SNIPPET: assembly
        super.assemble( module );
        ModuleAssembly configModule = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( configModule );
        // START SNIPPET: assembly
        new HazelcastEntityStoreAssembler().withConfig( configModule, Visibility.layer ).assemble( module );
    }
    // END SNIPPET: assembly

    @Test
    @Override
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
        throws UnitOfWorkCompletionException
    {
        super.givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification();
    }

    @After
    @Override
    public void tearDown()
    {
        super.tearDown();
        // TODO : delete test data
    }
}
