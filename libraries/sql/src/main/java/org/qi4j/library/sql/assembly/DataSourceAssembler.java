/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
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
package org.qi4j.library.sql.assembly;

import javax.sql.DataSource;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.importer.ServiceInstanceImporter;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.circuitbreaker.CircuitBreaker;
import org.qi4j.library.sql.datasource.DataSources;

/**
 * Use this Assembler to register a javax.sql.DataSource.
 */
public class DataSourceAssembler
        implements Assembler
{

    public static String DEFAULT_DATASOURCE_IDENTITY = "datasource";

    private String dataSourceServiceId = AbstractPooledDataSourceServiceAssembler.DEFAULT_DATASOURCE_SERVICE_IDENTITY;

    private String dataSourceId = DEFAULT_DATASOURCE_IDENTITY;

    private Visibility visibility = Visibility.module;

    private CircuitBreaker circuitBreaker;

    public DataSourceAssembler withDataSourceServiceIdentity( String dataSourceServiceId )
    {
        NullArgumentException.validateNotNull( "DataSourceService identity", dataSourceServiceId );
        this.dataSourceServiceId = dataSourceServiceId;
        return this;
    }

    public DataSourceAssembler identifiedBy( String dataSourceId )
    {
        NullArgumentException.validateNotNull( "DataSource identity", dataSourceId );
        this.dataSourceId = dataSourceId;
        return this;
    }

    public DataSourceAssembler visibleIn( Visibility visibility )
    {
        NullArgumentException.validateNotNull( "DataSource visibility", visibility );
        this.visibility = visibility;
        return this;
    }

    public DataSourceAssembler withCircuitBreaker()
    {
        this.circuitBreaker = DataSources.newDataSourceCircuitBreaker();
        return this;
    }

    public DataSourceAssembler withCircuitBreaker( CircuitBreaker circuitBreaker )
    {
        NullArgumentException.validateNotNull( "CircuitBreaker", circuitBreaker );
        this.circuitBreaker = circuitBreaker;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.importedServices( DataSource.class ).
                importedBy( ServiceInstanceImporter.class ).
                setMetaInfo( dataSourceServiceId ).
                identifiedBy( dataSourceId ).
                visibleIn( visibility );
        if ( circuitBreaker != null ) {
            module.importedServices( DataSource.class ).identifiedBy( dataSourceId ).setMetaInfo( circuitBreaker );
        }
    }

}
