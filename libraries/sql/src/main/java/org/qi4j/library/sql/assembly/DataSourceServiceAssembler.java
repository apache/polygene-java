/*
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

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.conversion.values.EntityToValueService;
import org.qi4j.library.sql.datasource.C3P0DataSourceServiceImporter;
import org.qi4j.library.sql.datasource.DataSourceConfiguration;
import org.qi4j.library.sql.datasource.DataSourceConfigurationValue;

/**
 * Use this Assembler to register a DataSourceService and its Configuration entity.
 */
public class DataSourceServiceAssembler
        implements Assembler
{

    private String dataSourceServiceId;

    private ModuleAssembly configModuleAssembly;

    public DataSourceServiceAssembler( String dataSourceServiceId, ModuleAssembly configModuleAssembly )
    {
        this.dataSourceServiceId = dataSourceServiceId;
        this.configModuleAssembly = configModuleAssembly;
    }

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.values( DataSourceConfigurationValue.class );
        module.services( EntityToValueService.class );
        module.services( C3P0DataSourceServiceImporter.class ).identifiedBy( dataSourceServiceId );
        if ( configModuleAssembly == null ) {
            module.entities( DataSourceConfiguration.class ).visibleIn( Visibility.layer );
        } else {
            configModuleAssembly.entities( DataSourceConfiguration.class ).visibleIn( Visibility.layer );
        }

    }

}
