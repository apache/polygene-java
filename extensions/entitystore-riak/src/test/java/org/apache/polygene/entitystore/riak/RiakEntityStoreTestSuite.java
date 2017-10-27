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
 */
package org.apache.polygene.entitystore.riak;

import java.util.Collections;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.riak.assembly.RiakEntityStoreAssembler;
import org.apache.polygene.test.docker.DockerRule;
import org.apache.polygene.test.entity.model.EntityStoreTestSuite;
import org.junit.ClassRule;

public class RiakEntityStoreTestSuite extends EntityStoreTestSuite
{
    @ClassRule
    public static final DockerRule DOCKER = new DockerRule( "riak", "riak_auth_mods started on node" );

    private RiakFixture riakFixture;

    @Override
    protected void defineStorageModule( ModuleAssembly module )
    {
        module.defaultServices();
        new RiakEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( configModule, Visibility.application )
            .assemble( module );

        RiakEntityStoreConfiguration riakConfig = configModule.forMixin( RiakEntityStoreConfiguration.class )
                                                              .declareDefaults();
        String host = DOCKER.getDockerHost();
        int port = DOCKER.getExposedContainerPort( "8087/tcp" );
        riakConfig.hosts().set( Collections.singletonList( host + ':' + port ) );
    }

    @Override
    public void tearDown()
    {
        riakFixture.deleteTestData();
        super.tearDown();
    }

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        Module storageModule = application.findModule( "Infrastructure Layer", "Storage Module" );
        RiakEntityStoreService es = storageModule.findService( RiakEntityStoreService.class ).get();
        riakFixture = new RiakFixture( es.riakClient(), es.riakNamespace() );
        riakFixture.waitUntilReady();
    }
}
