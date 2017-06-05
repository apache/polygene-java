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

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.sql.datasource.DataSourceConfiguration;

public abstract class AbstractPooledDataSourceServiceAssembler<AssemblerType>
    extends Assemblers.VisibilityIdentityConfig<AssemblerType>
{
    public static String DEFAULT_DATASOURCE_SERVICE_IDENTITY = "datasource-service";

    @Override
    public final void assemble( ModuleAssembly module )
    {
        module.values( DataSourceConfiguration.class ).visibleIn( Visibility.module );
        if( hasConfig() )
        {
            configModule().entities( DataSourceConfiguration.class ).visibleIn( configVisibility() );
        }
        onAssemble( module, identity() == null ? DEFAULT_DATASOURCE_SERVICE_IDENTITY : identity(), visibility() );
    }

    protected abstract void onAssemble( ModuleAssembly module, String identity, Visibility visibility );
}
