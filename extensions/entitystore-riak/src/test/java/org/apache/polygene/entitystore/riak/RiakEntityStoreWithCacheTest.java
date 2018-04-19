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
package org.apache.polygene.entitystore.riak;

import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import com.github.junit5docker.WaitFor;
import java.util.Collections;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.riak.assembly.RiakEntityStoreAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.cache.AbstractEntityStoreWithCacheTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@Docker( image = "org.apache.polygene:org.apache.polygene.internal.docker-riak",
         ports = @Port( exposed = 8801, inner = 8087 ),
         waitFor = @WaitFor( value = "riak_auth_mods started on node", timeoutInMillis = 60000 ),
         newForEachCase = false
)
public class RiakEntityStoreWithCacheTest extends AbstractEntityStoreWithCacheTest
{
    private RiakFixture riakFixture;

    @BeforeEach
    public void setupRiak()
        throws Exception
    {
        super.setUp();
        RiakEntityStoreService es = serviceFinder.findService( RiakEntityStoreService.class ).get();
        riakFixture = new RiakFixture( es.riakClient(), es.riakNamespace() );
        riakFixture.waitUntilReady();
    }

    @AfterEach
    public void cleanUpRiak()
    {
        riakFixture.deleteTestData();
        super.tearDown();
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );
        new RiakEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );
        RiakEntityStoreConfiguration riakConfig = config.forMixin( RiakEntityStoreConfiguration.class )
                                                        .declareDefaults();
        String host = "localhost";
        int port = 8801;
        riakConfig.hosts().set( Collections.singletonList( host + ':' + port ) );
    }
}
