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

package org.apache.zest.test.indexing.layered.assembly;

import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.layered.ModuleAssembler;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;

class PersistenceModule
    implements ModuleAssembler
{

    @Override
    public ModuleAssembly assemble( LayerAssembly layer, ModuleAssembly module )
        throws AssemblyException
    {
        module.withDefaultUnitOfWorkFactory();
        module.services( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
        module.services( MemoryEntityStoreService.class ).visibleIn( Visibility.application );
        return module;
    }
}
