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
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.test.entity.AbstractEntityStoreTest;

/**
 * Test the MongoMapEntityStoreService.
 * 
 * FIXME This test is ignored because it needs a MongoDB instance
 * 
 * Installing mongodb and starting it should suffice as the test use mongodb defaults: 127.0.0.1:27017
 * 
 * Do we have a build-wise way to switch on/off theses kind of tests ?
 */
@Ignore
public class MongoMapEntityStoreTest
        extends AbstractEntityStoreTest
{

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        super.assemble( module );

        ModuleAssembly config = module.layer().module( "config" );
        config.services( MemoryEntityStoreService.class );

        new MongoMapEntityStoreAssembler().withConfigModule( config ).assemble( module );

        MongoEntityStoreConfiguration mongoConfig = config.forMixin( MongoEntityStoreConfiguration.class ).declareDefaults();
        mongoConfig.writeConcern().set( MongoEntityStoreConfiguration.WriteConcern.FSYNC_SAFE );
    }

    private Mongo mongo;

    @Override
    public void setUp()
            throws Exception
    {
        super.setUp();
        mongo = module.findService( MongoMapEntityStoreService.class ).get().mongoInstanceUsed();
    }

    @Test
    @Override
    public void givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification()
            throws UnitOfWorkCompletionException
    {
        super.givenConcurrentUnitOfWorksWhenUoWCompletesThenCheckConcurrentModification();
    }

    @After
    @Override
    public void tearDown()
            throws Exception
    {
        mongo.dropDatabase( MongoMapEntityStoreAssembler.DEFAULT_DATABASE_NAME );
        super.tearDown();
    }

}
