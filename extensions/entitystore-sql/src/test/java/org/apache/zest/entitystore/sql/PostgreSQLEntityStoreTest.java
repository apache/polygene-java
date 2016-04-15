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
package org.apache.zest.entitystore.sql;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.BeforeClass;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.sql.assembly.PostgreSQLEntityStoreAssembler;
import org.apache.zest.entitystore.sql.internal.SQLs;
import org.apache.zest.library.sql.assembly.DataSourceAssembler;
import org.apache.zest.library.sql.common.SQLConfiguration;
import org.apache.zest.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.entity.AbstractEntityStoreTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

import static org.apache.zest.test.util.Assume.assumeConnectivity;

/**
 * WARN This test run only if localhost:5432 is listening.
 *
 * To run it you need to have a user & database set up in postgresql. Here are two snippets to create and drop the
 * needed test environment.
 *
 * Use 'password' as password for the jdbc_test_login user.
 *
 * <pre>
 * createuser -A -D -P -E -W jdbc_test_login
 * createdb -O jdbc_test_login -W jdbc_test_db
 * psql -d jdbc_testdb
 * CREATE EXTENSION ltree;
 * </pre>
 *
 * To clear the data:
 *
 * <pre>
 * dropdb -W jdbc_test_db
 * createdb -O jdbc_test_login -W jdbc_test_db
 * psql -d jdbc_test_db
 * CREATE EXTENSION ltree;
 * </pre>
 *
 * To remove the test user:
 *
 * <pre>
 * dropuser -W jdbc_test_login
 * </pre>
 */
public class PostgreSQLEntityStoreTest
    extends AbstractEntityStoreTest
{
    @BeforeClass
    public static void beforePostgreSQLEntityStoreTests()
    {
        assumeConnectivity( "localhost", 5432 );
    }

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // END SNIPPET: assembly
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );
        new OrgJsonValueSerializationAssembler().assemble( module );

        // START SNIPPET: assembly
        // DataSourceService
        new DBCPDataSourceServiceAssembler().
            identifiedBy( "postgresql-datasource-service" ).
            visibleIn( Visibility.module ).
            withConfig( config, Visibility.layer ).
            assemble( module );

        // DataSource
        new DataSourceAssembler().
            withDataSourceServiceIdentity( "postgresql-datasource-service" ).
            identifiedBy( "postgresql-datasource" ).
            visibleIn( Visibility.module ).
            withCircuitBreaker().
            assemble( module );

        // SQL EntityStore
        new PostgreSQLEntityStoreAssembler().
            visibleIn( Visibility.application ).
            withConfig( config, Visibility.layer ).
            assemble( module );
    }
    // END SNIPPET: assembly

    @Override
    public void tearDown()
        throws Exception
    {
        UnitOfWork uow = uowf.newUnitOfWork(
            UsecaseBuilder.newUsecase( "Delete " + getClass().getSimpleName() + " test data" )
        );
        try
        {
            SQLConfiguration config = uow.get( SQLConfiguration.class,
                                               PostgreSQLEntityStoreAssembler.DEFAULT_ENTITYSTORE_IDENTITY );
            Connection connection = serviceFinder.findService( DataSource.class ).get().getConnection();
            connection.setAutoCommit( false );
            String schemaName = config.schemaName().get();
            if( schemaName == null )
            {
                schemaName = SQLs.DEFAULT_SCHEMA_NAME;
            }
            try( Statement stmt = connection.createStatement() )
            {
                stmt.execute( String.format( "DELETE FROM %s." + SQLs.TABLE_NAME, schemaName ) );
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
