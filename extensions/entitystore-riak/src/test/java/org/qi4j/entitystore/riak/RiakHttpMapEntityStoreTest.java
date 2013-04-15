/*
 * Copyright 2012 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.riak;

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.bucket.Bucket;
import org.junit.Ignore;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

@Ignore( "This test is ignored because it needs a Riak instance" )
public class RiakHttpMapEntityStoreTest
    extends AbstractEntityStoreTest
{

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
        new RiakHttpMapEntityStoreAssembler().withConfigModule( config ).assemble( module );
    }
    // END SNIPPET: assembly
    private IRiakClient riakClient;
    private String bucketKey;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        RiakMapEntityStoreService es = module.findService( RiakMapEntityStoreService.class ).get();
        riakClient = es.riakClient();
        bucketKey = es.bucket();
    }

    @Override
    public void tearDown()
        throws Exception
    {
        // Riak don't expose bucket deletion in its API so we empty the Qi4j Entities bucket.
        Bucket bucket = riakClient.fetchBucket( bucketKey ).execute();
        for( String key : bucket.keys() )
        {
            bucket.delete( key ).execute();
        }
        super.tearDown();
    }
}
