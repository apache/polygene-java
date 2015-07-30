/*
 * Copyright 2011 Rickard Öberg.
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
package org.apache.zest.library.sql.assembly;

import javax.sql.DataSource;
import org.apache.zest.api.service.importer.ServiceInstanceImporter;
import org.apache.zest.api.util.NullArgumentException;
import org.apache.zest.bootstrap.Assemblers;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.circuitbreaker.CircuitBreaker;
import org.apache.zest.library.sql.datasource.DataSources;

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
        NullArgumentException.validateNotNull( "DataSourceService identity", dataSourceServiceId );
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
            identifiedBy( identity() ).
            visibleIn( visibility() );
        if( circuitBreaker != null )
        {
            module.importedServices( DataSource.class ).identifiedBy( identity() ).setMetaInfo( circuitBreaker );
        }
    }
}
