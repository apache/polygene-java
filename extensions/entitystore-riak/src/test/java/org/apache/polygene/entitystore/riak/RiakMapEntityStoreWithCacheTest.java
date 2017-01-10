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

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import java.util.Collections;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.riak.assembly.RiakEntityStoreAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.cache.AbstractEntityStoreWithCacheTest;
import org.apache.polygene.test.internal.DockerRule;
import org.apache.polygene.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.junit.BeforeClass;
import org.junit.ClassRule;

public class RiakMapEntityStoreWithCacheTest
    extends AbstractEntityStoreWithCacheTest
{
    @ClassRule
    public static final DockerRule DOCKER = new DockerRule( "riak", 8087 );

    @BeforeClass
    public static void waitForRiak() throws InterruptedException
    {
        // TODO:flakiness Properly wait for Riak to be ready
        // Riak listen to its network port before being ready to serve clients
        // This breaks the test-support integration
        // Wait a bit to give it a chance to be ready
        Thread.sleep( 10_000 );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );
        new OrgJsonValueSerializationAssembler().assemble( module );
        new RiakEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );
        RiakEntityStoreConfiguration riakConfig = config.forMixin( RiakEntityStoreConfiguration.class )
                                                        .declareDefaults();
        String host = DOCKER.getDockerHost();
        int port = DOCKER.getExposedContainerPort( "8087/tcp" );
        riakConfig.hosts().set( Collections.singletonList( host + ':' + port ) );
    }

    private RiakClient riakClient;
    private String bucketKey;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        RiakMapEntityStoreService es = serviceFinder.findService( RiakMapEntityStoreService.class ).get();
        riakClient = es.riakClient();
        bucketKey = es.bucket();
    }

    @Override
    public void tearDown() throws Exception
    {
        // Riak don't expose bucket deletion in its API so we empty the Polygene Entities bucket.
        if( bucketKey != null )
        {
            Namespace namespace = new Namespace( bucketKey );
            ListKeys listKeys = new ListKeys.Builder( namespace ).build();
            ListKeys.Response listKeysResponse = riakClient.execute( listKeys );
            for( Location location : listKeysResponse )
            {
                DeleteValue delete = new DeleteValue.Builder( location ).build();
                riakClient.execute( delete );
            }
        }
        super.tearDown();
    }
}
