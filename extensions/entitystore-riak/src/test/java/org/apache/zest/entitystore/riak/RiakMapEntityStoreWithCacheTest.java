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
package org.apache.zest.entitystore.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.riak.assembly.RiakEntityStoreAssembler;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.cache.AbstractEntityStoreWithCacheTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.junit.BeforeClass;

import static org.apache.zest.test.util.Assume.assumeConnectivity;

public class RiakMapEntityStoreWithCacheTest
    extends AbstractEntityStoreWithCacheTest
{
    @BeforeClass
    public static void beforeRiakMapEntityStoreTests()
    {
        assumeConnectivity( "localhost", 8087 );
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
    }

    private RiakClient riakClient;
    private String bucketKey;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        RiakMapEntityStoreService es = serviceFinder.findService( RiakMapEntityStoreService.class ).get();
        riakClient = es.riakClient();
        bucketKey = es.bucket();
    }

    @Override
    public void tearDown()
        throws Exception
    {
        // Riak don't expose bucket deletion in its API so we empty the Polygene Entities bucket.
        Namespace namespace = new Namespace( bucketKey );
        ListKeys listKeys = new ListKeys.Builder( namespace ).build();
        ListKeys.Response listKeysResponse = riakClient.execute( listKeys );
        for( Location location : listKeysResponse )
        {
            DeleteValue delete = new DeleteValue.Builder( location ).build();
            riakClient.execute( delete );
        }
        super.tearDown();
    }
}
