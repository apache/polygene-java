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
package org.apache.polygene.entitystore.sql;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.sql.assembly.AbstractSQLEntityStoreAssembler;
import org.apache.polygene.entitystore.sql.assembly.PostgreSQLEntityStoreAssembler;
import org.apache.polygene.library.sql.assembly.DataSourceAssembler;
import org.apache.polygene.library.sql.common.SQLConfiguration;
import org.apache.polygene.library.sql.datasource.DataSourceConfiguration;
import org.apache.polygene.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.docker.DockerRule;
import org.apache.polygene.test.entity.AbstractEntityStoreTest;
import org.junit.ClassRule;

import static org.apache.polygene.api.usecase.UsecaseBuilder.newUsecase;

public class PostgreSQLEntityStoreTest
    extends AbstractEntityStoreTest
{
    @ClassRule
    public static final DockerRule DOCKER = new DockerRule( "postgres",
                                                            3000L,
                                                            "PostgreSQL init process complete; ready for start up." );

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // END SNIPPET: assembly
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().defaultServicesVisibleIn( Visibility.layer ).assemble( config );

        // START SNIPPET: assembly
        // DataSourceService
        new DBCPDataSourceServiceAssembler()
            .identifiedBy( "postgresql-datasource-service" )
            .visibleIn( Visibility.module )
            .withConfig( config, Visibility.layer )
            .assemble( module );

        // DataSource
        new DataSourceAssembler()
            .withDataSourceServiceIdentity( "postgresql-datasource-service" )
            .identifiedBy( "postgresql-datasource" )
            .visibleIn( Visibility.module )
            .withCircuitBreaker()
            .assemble( module );

        // SQL EntityStore
        new PostgreSQLEntityStoreAssembler()
            .visibleIn( Visibility.application )
            .withConfig( config, Visibility.layer )
            .assemble( module );
        // END SNIPPET: assembly
        String host = DOCKER.getDockerHost();
        int port = DOCKER.getExposedContainerPort( "5432/tcp" );
        config.forMixin( DataSourceConfiguration.class ).declareDefaults()
              .url().set( "jdbc:postgresql://" + host + ":" + port + "/jdbc_test_db" );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    @Override
    public void tearDown()
        throws Exception
    {
        String usecaseName = "Delete " + getClass().getSimpleName() + " test data";
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( usecaseName ) );
        try
        {
            SQLConfiguration config = uow.get( SQLConfiguration.class, AbstractSQLEntityStoreAssembler.DEFAULT_ENTITYSTORE_IDENTITY );
            Connection connection = serviceFinder.findService( DataSource.class ).get().getConnection();
            connection.setAutoCommit( false );
            String schemaName = config.schemaName().get();
            try( Statement stmt = connection.createStatement() )
            {
                stmt.execute( String.format( "DROP SCHEMA \"%s\" CASCADE", schemaName ) );
                connection.commit();
            }
        }
        finally
        {
            uow.discard();
            super.tearDown();
        }
    }
}
