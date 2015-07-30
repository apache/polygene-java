/*
 * Copyright 2012 Paul Merlin.
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
package org.apache.zest.library.sql;

import javax.sql.DataSource;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.circuitbreaker.CircuitBreaker;
import org.apache.zest.library.sql.assembly.DataSourceAssembler;
import org.apache.zest.library.sql.bonecp.BoneCPDataSourceServiceAssembler;
import org.apache.zest.library.sql.dbcp.DBCPDataSourceServiceAssembler;

import static org.apache.zest.library.sql.DocumentationSupport.Constants.DS_ID;
import static org.apache.zest.library.sql.DocumentationSupport.Constants.DS_SERVICE_ID;
import static org.apache.zest.library.sql.DocumentationSupport.Constants.OTHER_DS_ID;
import static org.apache.zest.library.sql.datasource.DataSources.newDataSourceCircuitBreaker;

class DocumentationSupport
{
    interface Constants
    {
        String DS_ID = "datasource";

        String OTHER_DS_ID = "datasource2";

        String DS_SERVICE_ID = "datasource-service";
    }

    class CircuitBreakerDoc
        implements Assembler
    {
        // START SNIPPET: cb-datasource
        @Service
        DataSource dataSource; // Wrapped with a CircuitBreaker proxy
        // END SNIPPET: cb-datasource

        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            // START SNIPPET: cb-assembly
            CircuitBreaker circuitBreaker = newDataSourceCircuitBreaker( 5 /* threshold */,
                                                                         1000 * 60 * 5 /* 5min timeout */ );
            new DataSourceAssembler().
                withDataSourceServiceIdentity( DS_SERVICE_ID ).
                identifiedBy( DS_ID ).
                visibleIn( Visibility.layer ).
                withCircuitBreaker( circuitBreaker ).
                assemble( module );
            // END SNIPPET: cb-assembly
        }
    }

    class PoolsDoc
        implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            ModuleAssembly config = module;

            // START SNIPPET: bonecp
            // Assemble the BoneCP based Service Importer
            new BoneCPDataSourceServiceAssembler().
                identifiedBy( DS_SERVICE_ID ).
                visibleIn( Visibility.module ).
                withConfig( config, Visibility.layer ).
                assemble( module );
            // END SNIPPET: bonecp

            // START SNIPPET: dbcp
            // Assemble the Apache DBCP based Service Importer
            new DBCPDataSourceServiceAssembler().
                identifiedBy( DS_SERVICE_ID ).
                visibleIn( Visibility.module ).
                withConfig( config, Visibility.layer ).
                assemble( module );
            // END SNIPPET: dbcp

            // START SNIPPET: datasource
            // Assemble a DataSource
            new DataSourceAssembler().
                withDataSourceServiceIdentity( DS_SERVICE_ID ).
                identifiedBy( DS_ID ).
                visibleIn( Visibility.module ).
                assemble( module );
            // Another DataSource managed by the same C3P0 connection pool
            new DataSourceAssembler().
                withDataSourceServiceIdentity( DS_SERVICE_ID ).
                identifiedBy( OTHER_DS_ID ).
                visibleIn( Visibility.module ).
                assemble( module );
            // END SNIPPET: datasource
        }
    }

}
