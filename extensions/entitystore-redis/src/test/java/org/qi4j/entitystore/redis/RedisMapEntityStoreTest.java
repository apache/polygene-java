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
package org.qi4j.entitystore.redis;

import org.junit.Ignore;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Ignore( "This test is ignored because it needs a Redis instance" )
public class RedisMapEntityStoreTest
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
        new RedisMapEntityStoreAssembler().withConfigModule( config ).assemble( module );
    }
    // END SNIPPET: assembly
    private JedisPool jedisPool;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        RedisMapEntityStoreService es = module.findService( RedisMapEntityStoreService.class ).get();
        jedisPool = es.jedisPool();

    }

    @Override
    public void tearDown()
        throws Exception
    {
        Jedis jedis = jedisPool.getResource();
        try
        {
            jedis.flushDB();
        }
        finally
        {
            jedisPool.returnResource( jedis );
        }
        super.tearDown();
    }
}
