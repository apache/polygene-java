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
package org.apache.polygene.entitystore.cassandra;

import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import com.github.junit5docker.WaitFor;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.cassandra.assembly.CassandraEntityStoreAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.entity.AbstractEntityStoreTest;
import org.apache.polygene.test.entity.CanRemoveAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test the CassandraEntityStoreService.
 */
@Docker( image = "cassandra:3.10",
         ports = @Port( exposed = 8801, inner = 9042),
         waitFor = @WaitFor( value = "Starting listening for CQL clients", timeoutInMillis = 60000),
         newForEachCase = false)
public class CassandraEntityStoreTest
    extends AbstractEntityStoreTest
{
    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws Exception
    {
        // END SNIPPET: assembly
        super.assemble( module );

        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );
        module.services( CassandraEntityStoreService.class ).withTypes( CanRemoveAll.class ).withMixins( EmptyCassandraTableMixin.class );

        // START SNIPPET: assembly
        new CassandraEntityStoreAssembler()
            .withConfig( config, Visibility.layer )
            .assemble( module );
        // END SNIPPET: assembly

        CassandraEntityStoreConfiguration cassandraDefaults = config.forMixin( CassandraEntityStoreConfiguration.class ).declareDefaults();
        String host = "localhost";
        int port = 8801;
        // TODO: Logging system output here.
//        System.out.println("Cassandra: " + host + ":" + port);
        cassandraDefaults.hostnames().set( host + ':' + port );
        cassandraDefaults.createIfMissing().set( true );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
    }

    @Override
    @AfterEach
    public void tearDown()
    {
        CanRemoveAll cleaner = serviceFinder.findService( CanRemoveAll.class ).get();
        cleaner.removeAll();
        super.tearDown();
    }
}
