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
package org.apache.polygene.library.sql.assembly;

import java.util.Objects;
import javax.sql.DataSource;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.service.importer.ServiceInstanceImporter;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.circuitbreaker.CircuitBreaker;
import org.apache.polygene.library.sql.datasource.DataSources;

/**
 * Use this Assembler to register a javax.sql.DataSource.
 */
public class DataSourceAssembler
    extends Assemblers.VisibilityIdentity<DataSourceAssembler>
{
    public static String DEFAULT_DATASOURCE_IDENTITY = "datasource";

    private String dataSourceServiceId = AbstractPooledDataSourceServiceAssembler.DEFAULT_DATASOURCE_SERVICE_IDENTITY;

    private CircuitBreaker circuitBreaker;

    public DataSourceAssembler()
    {
        identifiedBy( DEFAULT_DATASOURCE_IDENTITY );
    }

    public DataSourceAssembler withDataSourceServiceIdentity( String dataSourceServiceId )
    {
        Objects.requireNonNull( dataSourceServiceId, "DataSourceService reference" );
        this.dataSourceServiceId = dataSourceServiceId;
        return this;
    }

    public DataSourceAssembler withCircuitBreaker()
    {
        this.circuitBreaker = DataSources.newDataSourceCircuitBreaker();
        return this;
    }

    public DataSourceAssembler withCircuitBreaker( CircuitBreaker circuitBreaker )
    {
        Objects.requireNonNull( circuitBreaker, "CircuitBreaker" );
        this.circuitBreaker = circuitBreaker;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.importedServices( DataSource.class ).
            importedBy( ServiceInstanceImporter.class ).
            setMetaInfo( new StringIdentity( dataSourceServiceId ) ).
            identifiedBy( identity() ).
            visibleIn( visibility() );
        if( circuitBreaker != null )
        {
            module.importedServices( DataSource.class ).identifiedBy( identity() ).setMetaInfo( circuitBreaker );
        }
    }
}
