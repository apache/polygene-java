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

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.bucket.Bucket;
import org.junit.BeforeClass;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.entity.AbstractEntityStoreTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

import static org.apache.zest.test.util.Assume.assumeConnectivity;

public class RiakProtobufMapEntityStoreTest
    extends AbstractEntityStoreTest
{
    @BeforeClass
    public static void beforeRiakProtobufMapEntityStoreTests()
    {
        assumeConnectivity( "localhost", 8087 );
    }

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // END SNIPPET: assembly
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );
        new OrgJsonValueSerializationAssembler().assemble( module );
        // START SNIPPET: assembly
        new RiakProtobufMapEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );
    }
    // END SNIPPET: assembly
    private IRiakClient riakClient;
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
        // Riak don't expose bucket deletion in its API so we empty the Zest Entities bucket.
        Bucket bucket = riakClient.fetchBucket( bucketKey ).execute();
        for( String key : bucket.keys() )
        {
            bucket.delete( key ).execute();
        }
        super.tearDown();
    }
}
