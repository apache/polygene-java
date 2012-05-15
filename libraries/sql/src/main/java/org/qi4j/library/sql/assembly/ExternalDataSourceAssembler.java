/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.circuitbreaker.CircuitBreaker;
import org.qi4j.library.sql.datasource.DataSources;

public class ExternalDataSourceAssembler
        implements Assembler
{

    private final String dataSourceId;

    private final Visibility visibility;

    private final DataSource externalDataSource;

    public ExternalDataSourceAssembler( String dataSourceId, Visibility visibility, DataSource externalDataSource )
    {
        this( dataSourceId, visibility, externalDataSource, null );
    }

    public ExternalDataSourceAssembler( String dataSourceId, Visibility visibility, DataSource externalDataSource, CircuitBreaker circuitBreaker )
    {
        NullArgumentException.validateNotNull( "DataSource identity", dataSourceId );
        NullArgumentException.validateNotNull( "DataSource visibility", visibility );
        NullArgumentException.validateNotNull( "DataSource", externalDataSource );
        this.dataSourceId = dataSourceId;
        this.visibility = visibility;
        if ( circuitBreaker != null ) {
            this.externalDataSource = DataSources.wrapWithCircuitBreaker( dataSourceId, externalDataSource, circuitBreaker );
        } else {
            this.externalDataSource = externalDataSource;
        }
    }

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.importedServices( DataSource.class ).
                identifiedBy( dataSourceId ).
                visibleIn( visibility ).
                setMetaInfo( externalDataSource );
    }

}
