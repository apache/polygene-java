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
package org.apache.zest.library.sql.assembly;

import javax.sql.DataSource;
import org.apache.zest.api.identity.StringIdentity;
import org.apache.zest.api.util.NullArgumentException;
import org.apache.zest.bootstrap.Assemblers;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.circuitbreaker.CircuitBreaker;
import org.apache.zest.library.sql.datasource.DataSources;

public class ExternalDataSourceAssembler
    extends Assemblers.VisibilityIdentity<ExternalDataSourceAssembler>
{
    public static String DEFAULT_DATASOURCE_IDENTITY = "external-datasource";

    private DataSource externalDataSource;

    private CircuitBreaker circuitBreaker;

    public ExternalDataSourceAssembler()
    {
        identifiedBy( DEFAULT_DATASOURCE_IDENTITY );
    }

    public ExternalDataSourceAssembler( DataSource externalDataSource )
    {
        NullArgumentException.validateNotNull( "DataSource", externalDataSource );
        this.externalDataSource = externalDataSource;
    }

    public ExternalDataSourceAssembler withCircuitBreaker()
    {
        this.circuitBreaker = DataSources.newDataSourceCircuitBreaker();
        return this;
    }

    public ExternalDataSourceAssembler withCircuitBreaker( CircuitBreaker circuitBreaker )
    {
        NullArgumentException.validateNotNull( "CircuitBreaker", circuitBreaker );
        this.circuitBreaker = circuitBreaker;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        if( circuitBreaker != null )
        {
            externalDataSource = DataSources.wrapWithCircuitBreaker( new StringIdentity(identity()), externalDataSource, circuitBreaker );
        }
        module.importedServices( DataSource.class ).
            identifiedBy( identity() ).
            visibleIn( visibility() ).
            setMetaInfo( externalDataSource );
    }
}
