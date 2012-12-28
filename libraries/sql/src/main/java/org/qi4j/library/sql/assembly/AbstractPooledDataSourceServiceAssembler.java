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
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.conversion.values.EntityToValueService;
import org.qi4j.library.sql.datasource.DataSourceConfiguration;
import org.qi4j.library.sql.datasource.DataSourceConfigurationValue;

public abstract class AbstractPooledDataSourceServiceAssembler<T extends AbstractPooledDataSourceServiceAssembler>
        implements Assembler
{

    public static String DEFAULT_DATASOURCE_SERVICE_IDENTITY = "datasource-service";

    private String dataSourceServiceId = DEFAULT_DATASOURCE_SERVICE_IDENTITY;

    private Visibility visibility = Visibility.module;

    private ModuleAssembly configModuleAssembly;

    private Visibility configVisibility = Visibility.module;

    public T identifiedBy( String dataSourceServiceId )
    {
        NullArgumentException.validateNotNull( "DataSourceService identity", dataSourceServiceId );
        this.dataSourceServiceId = dataSourceServiceId;
        return ( T ) this;
    }

    public T visibleIn( Visibility visibility )
    {
        NullArgumentException.validateNotNull( "DataSourceService visibility", visibility );
        this.visibility = visibility;
        return ( T ) this;
    }

    public T withConfig( ModuleAssembly configModule )
    {
        NullArgumentException.validateNotNull( "Configuration ModuleAssembly", configModule );
        this.configModuleAssembly = configModule;
        return ( T ) this;
    }

    public T withConfigVisibility( Visibility configVisibility )
    {
        NullArgumentException.validateNotNull( "Configuration Visibility", configVisibility );
        this.configVisibility = configVisibility;
        return ( T ) this;
    }

    @Override
    public final void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        if ( configModuleAssembly == null ) {
            configModuleAssembly = module;
        }
        module.values( DataSourceConfigurationValue.class ).visibleIn( Visibility.module );
        module.services( EntityToValueService.class ).visibleIn( Visibility.module );
        configModuleAssembly.entities( DataSourceConfiguration.class ).visibleIn( configVisibility );
        onAssemble( module, dataSourceServiceId, visibility );
    }

    protected abstract void onAssemble( ModuleAssembly module, String identity, Visibility visibility );

}
