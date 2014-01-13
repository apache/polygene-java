/*
 * Copyright (c) 2012-2014, Paul Merlin. All Rights Reserved.
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
package org.qi4j.test.performance.entitystore.sql;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.assembly.DerbySQLEntityStoreAssembler;
import org.qi4j.entitystore.sql.internal.SQLs;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.library.sql.common.SQLUtil;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.performance.entitystore.AbstractEntityStorePerformanceTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

/**
 * Performance test for DerbySQLEntityStore.
 */
public class DerbySQLEntityStorePerformanceTest
    extends AbstractEntityStorePerformanceTest
{

    public DerbySQLEntityStorePerformanceTest()
    {
        super( "DerbySQLEntityStore", createAssembler() );
    }

    private static Assembler createAssembler()
    {
        return new Assembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                ModuleAssembly config = module.layer().module( "config" );
                new EntityTestAssembler().assemble( config );

                new OrgJsonValueSerializationAssembler().assemble( module );

                // DataSourceService
                new DBCPDataSourceServiceAssembler().
                    identifiedBy( "derby-datasource-service" ).
                    visibleIn( Visibility.module ).
                    withConfig( config ).
                    withConfigVisibility( Visibility.layer ).
                    assemble( module );

                // DataSource
                new DataSourceAssembler().
                    withDataSourceServiceIdentity( "derby-datasource-service" ).
                    identifiedBy( "derby-datasource" ).
                    withCircuitBreaker().
                    assemble( module );

                // SQL EntityStore
                new DerbySQLEntityStoreAssembler().
                    withConfig( config ).
                    withConfigVisibility( Visibility.layer ).
                    assemble( module );
            }
        };
    }

    @Override
    protected void cleanUp()
        throws Exception
    {
        if( module == null )
        {
            return;
        }
        UnitOfWork uow = this.module.newUnitOfWork( UsecaseBuilder.newUsecase(
            "Delete " + getClass().getSimpleName() + " test data" ) );
        try
        {
            SQLConfiguration config = uow.get( SQLConfiguration.class,
                                               DerbySQLEntityStoreAssembler.DEFAULT_ENTITYSTORE_IDENTITY );
            Connection connection = module.findService( DataSource.class ).get().getConnection();
            String schemaName = config.schemaName().get();
            if( schemaName == null )
            {
                schemaName = SQLs.DEFAULT_SCHEMA_NAME;
            }

            Statement stmt = null;
            try
            {
                stmt = connection.createStatement();
                stmt.execute( String.format( "DELETE FROM %s." + SQLs.TABLE_NAME, schemaName ) );
                connection.commit();
            }
            finally
            {
                SQLUtil.closeQuietly( stmt );
            }
        }
        finally
        {
            uow.discard();
            super.cleanUp();
        }
    }

}
