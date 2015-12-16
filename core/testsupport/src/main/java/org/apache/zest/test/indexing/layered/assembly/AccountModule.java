/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.zest.test.indexing.layered.assembly;

import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.layered.ModuleAssembler;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.apache.zest.test.indexing.model.Account;
import org.apache.zest.test.indexing.model.Domain;
import org.apache.zest.test.indexing.model.File;
import org.apache.zest.test.indexing.model.Host;
import org.apache.zest.test.indexing.model.Port;
import org.apache.zest.test.indexing.model.Protocol;
import org.apache.zest.test.indexing.model.QueryParam;
import org.apache.zest.test.indexing.model.URL;

class AccountModule
    implements ModuleAssembler
{

    @Override
    public ModuleAssembly assemble( LayerAssembly layer, ModuleAssembly module )
        throws AssemblyException
    {
        module.withDefaultUnitOfWorkFactory();
        module.entities( Account.class, Domain.class ).visibleIn( Visibility.layer );
        module.values( File.class, Host.class, Port.class, Protocol.class, QueryParam.class, URL.class )
            .visibleIn( Visibility.layer );
        module.services( UuidIdentityGeneratorService.class );
        return module;
    }
}
