/*
 * Copyright (c) 2012, Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.entitystore.sql;

import org.apache.derby.iapi.services.io.FileUtil;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.assembly.H2SQLEntityStoreAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

public class H2SQLEntityStoreTest
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
        new EntityTestAssembler().assemble( config );
        new OrgJsonValueSerializationAssembler().assemble( module );

        // START SNIPPET: assembly
        // DataSourceService
        new DBCPDataSourceServiceAssembler().
                identifiedBy( "h2-datasource-service" ).
                visibleIn( Visibility.module ).
                withConfig( config ).
                withConfigVisibility( Visibility.layer ).
                assemble( module );

        // DataSource
        new DataSourceAssembler().
                withDataSourceServiceIdentity( "h2-datasource-service" ).
                identifiedBy( "h2-datasource" ).
                visibleIn( Visibility.module ).
                withCircuitBreaker().
                assemble( module );

        // SQL EntityStore
        new H2SQLEntityStoreAssembler().
                visibleIn( Visibility.application ).
                withConfig( config ).
                withConfigVisibility( Visibility.layer ).
                assemble( module );
    }
    // END SNIPPET: assembly

    @Override
    public void tearDown()
            throws Exception
    {
        if ( module == null ) {
            return;
        }
        UnitOfWork uow = this.module.newUnitOfWork( UsecaseBuilder.newUsecase(
                "Delete " + getClass().getSimpleName() + " test data" ) );
        try {

            FileUtil.removeDirectory( "target/qi4j-data" );

        } finally {
            uow.discard();
            super.tearDown();
        }
    }

}
