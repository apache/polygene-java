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

package org.apache.polygene.test.model.assembly;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.layered.ModuleAssembler;
import org.apache.polygene.test.model.Account;
import org.apache.polygene.test.model.Domain;
import org.apache.polygene.test.model.File;
import org.apache.polygene.test.model.Host;
import org.apache.polygene.test.model.Port;
import org.apache.polygene.test.model.Protocol;
import org.apache.polygene.test.model.QueryParam;
import org.apache.polygene.test.model.URL;

class AccountModule
    implements ModuleAssembler
{

    @Override
    public ModuleAssembly assemble( LayerAssembly layer, ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( Account.class, Domain.class ).visibleIn( Visibility.layer );
        module.values( File.class, Host.class, Port.class, Protocol.class, QueryParam.class, URL.class )
            .visibleIn( Visibility.layer );
        return module;
    }
}
