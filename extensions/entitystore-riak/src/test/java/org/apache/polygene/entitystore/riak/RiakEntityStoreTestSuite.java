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

import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import com.github.junit5docker.WaitFor;
import java.util.Collections;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.riak.assembly.RiakEntityStoreAssembler;
import org.apache.polygene.test.entity.model.EntityStoreTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@Docker( image = "basho/riak-kv:ubuntu-2.2.3",
         ports = @Port( exposed = 8801, inner = 8087),
         waitFor = @WaitFor( value = "riak_auth_mods started on node", timeoutInMillis = 60000),
         newForEachCase = false
)
public class RiakEntityStoreTestSuite extends EntityStoreTestSuite
{
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
        String host = "localhost";
        int port = 8801;
        riakConfig.hosts().set( Collections.singletonList( host + ':' + port ) );
    }

    @BeforeEach
    public void initializeRiak()
        throws Exception
    {
        Module storageModule = application.findModule( "Infrastructure Layer", "Storage Module" );
        RiakEntityStoreService es = storageModule.findService( RiakEntityStoreService.class ).get();
        riakFixture = new RiakFixture( es.riakClient(), es.riakNamespace() );
        riakFixture.waitUntilReady();
    }

    @AfterEach
    public void cleanUpRiak()
    {
        riakFixture.deleteTestData();
        super.tearDown();
    }
}
