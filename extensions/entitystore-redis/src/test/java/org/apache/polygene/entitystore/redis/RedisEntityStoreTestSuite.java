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
package org.apache.polygene.entitystore.redis;

import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import com.github.junit5docker.WaitFor;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.redis.assembly.RedisEntityStoreAssembler;
import org.apache.polygene.test.entity.model.EntityStoreTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Docker( image = "redis:4.0.0-alpine",
         ports = @Port( exposed = 8801, inner = 6379),
         newForEachCase = false
)
public class RedisEntityStoreTestSuite extends EntityStoreTestSuite
{
    @Override
    protected void defineStorageModule( ModuleAssembly module )
    {
        module.defaultServices();
        new RedisEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( configModule, Visibility.application )
            .assemble( module );

        RedisEntityStoreConfiguration redisConfig = configModule.forMixin( RedisEntityStoreConfiguration.class )
                                                                .declareDefaults();
        redisConfig.host().set( "localhost" );
        redisConfig.port().set( 8801 );
    }

    private JedisPool jedisPool;

    @BeforeEach
    public void initializeRedis()
    {
        Module storageModule = application.findModule( "Infrastructure Layer", "Storage Module" );
        RedisEntityStoreService es = storageModule.findService( RedisEntityStoreService.class ).get();
        jedisPool = es.jedisPool();
    }

    @AfterEach
    public void cleanUpRedis()
    {
        try( Jedis jedis = jedisPool.getResource() )
        {
            jedis.flushDB();
        }
    }
}
