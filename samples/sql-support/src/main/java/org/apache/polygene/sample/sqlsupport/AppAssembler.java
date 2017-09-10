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
package org.apache.polygene.sample.sqlsupport;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.ApplicationAssembler;
import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.ApplicationAssemblyFactory;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.memory.MemoryEntityStoreService;
import org.apache.polygene.entitystore.sqlkv.assembly.PostgreSQLEntityStoreAssembler;
import org.apache.polygene.index.sql.assembly.PostgreSQLIndexQueryAssembler;
import org.apache.polygene.library.sql.assembly.DataSourceAssembler;
import org.apache.polygene.library.sql.datasource.DataSources;
import org.apache.polygene.library.sql.dbcp.DBCPDataSourceServiceAssembler;

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
    {
        ApplicationAssembly appAss = applicationFactory.newApplicationAssembly();
        appAss.setName( "SQL Support Sample" );

        // Config
        LayerAssembly configLayer = appAss.layer( "config" );
        ModuleAssembly configModule = configLayer.module( "config" );
        {
            configModule.services( MemoryEntityStoreService.class ).
                visibleIn( Visibility.module );
            // Use a PreferenceEntityStore instead if you want the configuration to be persistent
            // new PreferenceEntityStoreAssembler( Visibility.module ).assemble( configModule );
        }

        // Infra
        LayerAssembly infraLayer = appAss.layer( "infra" );
        ModuleAssembly persistenceModule = infraLayer.module( "persistence" );
        {
            // SQL DataSource Service
            String dataSourceServiceIdentity = "postgresql-datasource-service";
            new DBCPDataSourceServiceAssembler().
                identifiedBy( dataSourceServiceIdentity ).
                visibleIn( Visibility.module ).
                withConfig( configModule, Visibility.application ).
                assemble( persistenceModule );

            // SQL EntityStore DataSource and Service
            new DataSourceAssembler().
                withDataSourceServiceIdentity( dataSourceServiceIdentity ).
                identifiedBy( "postgresql-es-datasource" ).
                visibleIn( Visibility.module ).
                withCircuitBreaker( DataSources.newDataSourceCircuitBreaker() ).assemble( persistenceModule );
            new PostgreSQLEntityStoreAssembler().
                visibleIn( Visibility.application ).
                withConfig( configModule, Visibility.application ).
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
                withConfig( configModule, Visibility.application ).
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
