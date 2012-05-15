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

/* package */ abstract class AbstractPooledDataSourceServiceAssembler
        implements Assembler
{

    protected final String dataSourceServiceId;

    protected final Visibility visibility;

    private final ModuleAssembly configModuleAssembly;

    private final Visibility configVisibility;

    public AbstractPooledDataSourceServiceAssembler( String dataSourceServiceId, Visibility visibility, ModuleAssembly configModuleAssembly, Visibility configVisibility )
    {
        NullArgumentException.validateNotNull( "DataSourceService identity", dataSourceServiceId );
        NullArgumentException.validateNotNull( "DataSourceService visibility", visibility );
        NullArgumentException.validateNotNull( "Configuration ModuleAssembly", configModuleAssembly );
        NullArgumentException.validateNotNull( "Configuration visibility", configVisibility );
        this.dataSourceServiceId = dataSourceServiceId;
        this.visibility = visibility;
        this.configModuleAssembly = configModuleAssembly;
        this.configVisibility = configVisibility;
    }

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.values( DataSourceConfigurationValue.class ).visibleIn( Visibility.module );
        module.services( EntityToValueService.class ).visibleIn( Visibility.module );
        configModuleAssembly.entities( DataSourceConfiguration.class ).visibleIn( configVisibility );
        onAssemble( module );
    }

    protected abstract void onAssemble( ModuleAssembly module );

}
