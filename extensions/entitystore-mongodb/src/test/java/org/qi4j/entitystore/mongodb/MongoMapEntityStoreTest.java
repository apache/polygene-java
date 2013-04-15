/*
 * Copyright 2011 Paul Merlin.
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
package org.qi4j.entitystore.mongodb;

import com.mongodb.Mongo;
import org.junit.Ignore;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

/**
 * Test the MongoMapEntityStoreService.
 *
 *
 * Installing mongodb and starting it should suffice as the test use mongodb defaults: 127.0.0.1:27017
 *
 * Do we have a build-wise way to switch on/off theses kind of tests ?
 */
@Ignore( "This test is ignored because it needs a MongoDB instance" )
public class MongoMapEntityStoreTest
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
        config.services( MemoryEntityStoreService.class );

        new OrgJsonValueSerializationAssembler().assemble( module );

        // START SNIPPET: assembly
        new MongoMapEntityStoreAssembler().withConfigModule( config ).assemble( module );
        // END SNIPPET: assembly

        MongoEntityStoreConfiguration mongoConfig = config.forMixin( MongoEntityStoreConfiguration.class ).declareDefaults();
        mongoConfig.writeConcern().set( MongoEntityStoreConfiguration.WriteConcern.FSYNC_SAFE );
        mongoConfig.database().set( "qi4j:test" );
        mongoConfig.collection().set( "qi4j:test:entities" );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly
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
