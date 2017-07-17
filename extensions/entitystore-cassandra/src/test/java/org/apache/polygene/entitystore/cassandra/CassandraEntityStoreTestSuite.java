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

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.cassandra.assembly.CassandraEntityStoreAssembler;
import org.apache.polygene.test.docker.DockerRule;
import org.apache.polygene.test.entity.CanRemoveAll;
import org.apache.polygene.test.entity.model.EntityStoreTestSuite;
import org.junit.ClassRule;

/**
 * Test the CassandraEntityStoreService.
 * <p>Installing Cassandra and starting it should suffice as the test use Cassandra defaults: 127.0.0.1:3000</p>
 */
public class CassandraEntityStoreTestSuite extends EntityStoreTestSuite
{
    @ClassRule
    public static final DockerRule DOCKER = new DockerRule( "cassandra", "Starting listening for CQL clients" );

    @Override
    protected void defineStorageModule( ModuleAssembly module )
    {
        module.defaultServices();
        new CassandraEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( configModule, Visibility.application )
            .assemble( module );

        module.services( CassandraEntityStoreService.class )
              .withTypes( CanRemoveAll.class )
              .withMixins( EmptyCassandraTableMixin.class )
              .visibleIn( Visibility.application );


        CassandraEntityStoreConfiguration cassandraDefaults = configModule.forMixin( CassandraEntityStoreConfiguration.class ).declareDefaults();
        String host = DOCKER.getDockerHost();
        int port = DOCKER.getExposedContainerPort( "9042/tcp" );
        System.out.println("Cassandra: " + host + ":" + port);
        cassandraDefaults.hostnames().set( host + ':' + port );
        cassandraDefaults.createIfMissing().set( true );
    }

    @Override
    public void tearDown()
        throws Exception
    {
        Module module = application.findModule( "Infrastructure Layer", "Storage Module" );
        ServiceReference<CanRemoveAll> cleaner = module.serviceFinder().findService( CanRemoveAll.class );
        if( cleaner.isActive() && cleaner.isAvailable() )
        {
            cleaner.get().removeAll();
        }
        else
        {
            throw new IllegalStateException( "Clean up operation of Cassandra database was not availeble." );
        }
        super.tearDown();
    }
}
