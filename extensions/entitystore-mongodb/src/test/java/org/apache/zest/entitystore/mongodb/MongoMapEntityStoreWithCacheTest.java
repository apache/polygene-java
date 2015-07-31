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
package org.apache.zest.entitystore.mongodb;

import com.mongodb.Mongo;
import org.junit.BeforeClass;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.cache.AbstractEntityStoreWithCacheTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

import static org.apache.zest.test.util.Assume.assumeConnectivity;

/**
 * Test the MongoMapEntityStoreService usage with a CachePool.
 * <p>Installing mongodb and starting it should suffice as the test use mongodb defaults: 127.0.0.1:27017</p>
 */
public class MongoMapEntityStoreWithCacheTest
    extends AbstractEntityStoreWithCacheTest
{
    @BeforeClass
    public static void beforeRedisMapEntityStoreTests()
    {
        assumeConnectivity( "localhost", 27017 );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );

        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );

        new OrgJsonValueSerializationAssembler().assemble( module );

        new MongoMapEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );

        MongoEntityStoreConfiguration mongoConfig = config.forMixin( MongoEntityStoreConfiguration.class ).declareDefaults();
        mongoConfig.writeConcern().set( MongoEntityStoreConfiguration.WriteConcern.FSYNC_SAFE );
        mongoConfig.database().set( "qi4j:test" );
        mongoConfig.collection().set( "qi4j:test:entities" );
    }

    private Mongo mongo;
    private String dbName;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        MongoMapEntityStoreService es = module.findService( MongoMapEntityStoreService.class ).get();
        mongo = es.mongoInstanceUsed();
        dbName = es.dbInstanceUsed().getName();

    }

    @Override
    public void tearDown()
        throws Exception
    {
        mongo.dropDatabase( dbName );
        super.tearDown();
    }
}
