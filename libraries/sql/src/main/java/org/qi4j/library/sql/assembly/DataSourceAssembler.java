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

/**
 * Use this Assembler to register a javax.sql.DataSource.
 */
public class DataSourceAssembler
        implements Assembler
{

    private final String dataSourceServiceId;

    private final String dataSourceId;

    private final Visibility visibility;

    private final CircuitBreaker circuitBreaker;

    public DataSourceAssembler( String dataSourceServiceId, String dataSourceId, Visibility visibility )
    {
        this( dataSourceServiceId, dataSourceId, visibility, null );
    }

    public DataSourceAssembler( String dataSourceServiceId, String dataSourceId, Visibility visibility, CircuitBreaker circuitBreaker )
    {
        NullArgumentException.validateNotNull( "DataSourceService identity", dataSourceServiceId );
        NullArgumentException.validateNotNull( "DataSource identity", dataSourceId );
        NullArgumentException.validateNotNull( "DataSource visibility", visibility );
        this.dataSourceServiceId = dataSourceServiceId;
        this.dataSourceId = dataSourceId;
        this.visibility = visibility;
        this.circuitBreaker = circuitBreaker;
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
