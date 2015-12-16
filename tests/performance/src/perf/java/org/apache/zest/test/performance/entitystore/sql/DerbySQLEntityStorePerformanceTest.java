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
package org.apache.zest.test.performance.entitystore.sql;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.sql.assembly.DerbySQLEntityStoreAssembler;
import org.apache.zest.entitystore.sql.internal.SQLs;
import org.apache.zest.library.sql.assembly.DataSourceAssembler;
import org.apache.zest.library.sql.common.SQLConfiguration;
import org.apache.zest.library.sql.common.SQLUtil;
import org.apache.zest.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.performance.entitystore.AbstractEntityStorePerformanceTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

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
                    withConfig( config, Visibility.layer ).
                    assemble( module );

                // DataSource
                new DataSourceAssembler().
                    withDataSourceServiceIdentity( "derby-datasource-service" ).
                    identifiedBy( "derby-datasource" ).
                    withCircuitBreaker().
                    assemble( module );

                // SQL EntityStore
                new DerbySQLEntityStoreAssembler().
                    withConfig( config, Visibility.layer ).
                    assemble( module );
            }
        };
    }

    @Override
    protected void cleanUp()
        throws Exception
    {
        if( uowf == null )
        {
            return;
        }
        UnitOfWork uow = this.uowf.newUnitOfWork( UsecaseBuilder.newUsecase(
            "Delete " + getClass().getSimpleName() + " test data" ) );
        try
        {
            SQLConfiguration config = uow.get( SQLConfiguration.class,
                                               DerbySQLEntityStoreAssembler.DEFAULT_ENTITYSTORE_IDENTITY );
            Connection connection = serviceFinder.findService( DataSource.class ).get().getConnection();
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
