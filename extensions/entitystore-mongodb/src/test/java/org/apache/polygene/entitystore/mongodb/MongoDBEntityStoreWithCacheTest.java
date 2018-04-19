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
package org.apache.polygene.entitystore.mongodb;

import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import com.mongodb.Mongo;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.mongodb.assembly.MongoDBEntityStoreAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.cache.AbstractEntityStoreWithCacheTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test the MongoDBEntityStoreService usage with a CachePool.
 */
@Docker( image = "org.apache.polygene:org.apache.polygene.internal.docker-mongo",
         ports = @Port( exposed = 8801, inner = 27017),
         newForEachCase = false
)
public class MongoDBEntityStoreWithCacheTest
    extends AbstractEntityStoreWithCacheTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );

        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );

        new MongoDBEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );

        MongoDBEntityStoreConfiguration mongoConfig = config.forMixin( MongoDBEntityStoreConfiguration.class ).declareDefaults();
        mongoConfig.writeConcern().set( MongoDBEntityStoreConfiguration.WriteConcern.MAJORITY );
        mongoConfig.database().set( "polygene:test" );
        mongoConfig.collection().set( "polygene:test:entities" );
        mongoConfig.hostname().set( "localhost" );
        mongoConfig.port().set( 8801 );
    }

    private Mongo mongo;
    private String dbName;

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
        MongoDBEntityStoreService es = serviceFinder.findService( MongoDBEntityStoreService.class ).get();
        mongo = es.mongoInstanceUsed();
        dbName = es.dbInstanceUsed().getName();

    }

    @Override
    @AfterEach
    public void tearDown()
    {
        mongo.dropDatabase( dbName );
        super.tearDown();
    }
}
