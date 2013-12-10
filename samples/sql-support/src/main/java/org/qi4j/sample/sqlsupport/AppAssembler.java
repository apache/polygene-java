/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.qi4j.sample.sqlsupport;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.sql.assembly.PostgreSQLEntityStoreAssembler;
import org.qi4j.index.sql.assembly.PostgreSQLIndexQueryAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.datasource.DataSources;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;

/**
 * Assemble the Application.
 *
 * Use SQL EntityStore and SQL Index/Query for Persistence using PostgreSQL.
 * EntityStore and Index/Query use different DataSource in order to allow splitting the two in two servers/databases.
 */
public class AppAssembler
        implements ApplicationAssembler
{

    @Override
    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
            throws AssemblyException
    {
        ApplicationAssembly appAss = applicationFactory.newApplicationAssembly();
        appAss.setName( "SQL Support Sample" );

        // Config
        LayerAssembly configLayer = appAss.layer( "config" );
        ModuleAssembly configModule = configLayer.module( "config" );
        {
            configModule.services( OrgJsonValueSerializationService.class ).
                taggedWith( ValueSerialization.Formats.JSON );
            configModule.services( MemoryEntityStoreService.class ).
                visibleIn( Visibility.module );
            // Use a PreferenceEntityStore instead if you want the configuration to be persistent
            // new PreferenceEntityStoreAssembler( Visibility.module ).assemble( configModule );
        }

        // Infra
        LayerAssembly infraLayer = appAss.layer( "infra" );
        ModuleAssembly persistenceModule = infraLayer.module( "persistence" );
        {
            persistenceModule.services( OrgJsonValueSerializationService.class ).
                taggedWith( ValueSerialization.Formats.JSON );

            // SQL DataSource Service
            String dataSourceServiceIdentity = "postgresql-datasource-service";
            new DBCPDataSourceServiceAssembler().
                    identifiedBy( dataSourceServiceIdentity ).
                    visibleIn( Visibility.module ).
                    withConfig( configModule ).
                    withConfigVisibility( Visibility.application ).
                    assemble( persistenceModule );

            // SQL EntityStore DataSource and Service
            new DataSourceAssembler().
                    withDataSourceServiceIdentity( dataSourceServiceIdentity ).
                    identifiedBy( "postgresql-es-datasource" ).
                    visibleIn( Visibility.module ).
                    withCircuitBreaker( DataSources.newDataSourceCircuitBreaker() ).assemble( persistenceModule );
            new PostgreSQLEntityStoreAssembler().
                    visibleIn( Visibility.application ).
                    withConfig( configModule ).
                    withConfigVisibility( Visibility.application ).
                    assemble( persistenceModule );

            // SQL Index/Query DataSource and Service
            new DataSourceAssembler().
                    withDataSourceServiceIdentity( dataSourceServiceIdentity ).
                    identifiedBy( "postgresql-index-datasource" ).
                    visibleIn( Visibility.module ).
                    withCircuitBreaker().
                    assemble( persistenceModule );
            new PostgreSQLIndexQueryAssembler().
                    visibleIn( Visibility.application ).
                    withConfig( configModule ).
                    withConfigVisibility( Visibility.application ).
                    assemble( persistenceModule );
        }

        // App
        LayerAssembly appLayer = appAss.layer( "app" );
        ModuleAssembly domainModule = appLayer.module( "domain" );
        {
            domainModule.entities( PretextEntity.class );
        }

        // Uses
        infraLayer.uses( configLayer );
        appLayer.uses( infraLayer );

        return appAss;
    }

}
