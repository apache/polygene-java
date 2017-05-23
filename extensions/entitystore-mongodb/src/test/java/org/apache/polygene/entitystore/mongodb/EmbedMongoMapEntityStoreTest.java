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
 */
package org.apache.polygene.entitystore.mongodb;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import java.util.Collections;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.mongodb.assembly.MongoDBEntityStoreAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.entity.AbstractEntityStoreTest;
import org.apache.polygene.test.util.FreePortFinder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

public class EmbedMongoMapEntityStoreTest extends AbstractEntityStoreTest
{
    private static final MongodStarter MONGO_STARTER = MongodStarter.getDefaultInstance();

    @Rule
    public TestName testName = new TestName();
    private static int port;
    private static MongodExecutable mongod;

    @BeforeClass
    public static void startEmbedMongo()
        throws IOException
    {
        port = FreePortFinder.findFreePortOnLoopback();
        mongod = MONGO_STARTER.prepare( new MongodConfigBuilder()
                                            .version( Version.Main.PRODUCTION )
                                            .net( new Net( "localhost", port, Network.localhostIsIPv6() ) )
                                            .build() );
        mongod.start();
    }

    @AfterClass
    public static void stopEmbedMongo()
    {
        if( mongod != null )
        {
            mongod.stop();
        }
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );

        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );

        new MongoDBEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );

        MongoDBEntityStoreConfiguration mongoConfig = config.forMixin( MongoDBEntityStoreConfiguration.class )
                                                            .declareDefaults();
        mongoConfig.writeConcern().set( MongoDBEntityStoreConfiguration.WriteConcern.MAJORITY );
        mongoConfig.database().set( "polygene-test" );
        mongoConfig.collection().set( testName.getMethodName() );
        mongoConfig.nodes().set( Collections.singletonList( "localhost:" + port ) );
    }
}
