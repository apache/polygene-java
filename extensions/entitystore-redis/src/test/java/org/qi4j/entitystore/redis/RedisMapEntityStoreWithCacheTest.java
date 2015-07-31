/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.qi4j.entitystore.redis;

import org.junit.BeforeClass;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.cache.AbstractEntityStoreWithCacheTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.qi4j.test.util.Assume.assumeConnectivity;

public class RedisMapEntityStoreWithCacheTest
    extends AbstractEntityStoreWithCacheTest
{
    @BeforeClass
    public static void beforeRedisMapEntityStoreTests()
    {
        assumeConnectivity( "localhost", 6379 );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );
        new OrgJsonValueSerializationAssembler().assemble( module );
        new RedisMapEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );
    }

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
