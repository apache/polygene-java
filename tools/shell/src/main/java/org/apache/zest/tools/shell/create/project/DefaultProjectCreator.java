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

package org.apache.zest.tools.shell.create.project;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.zest.tools.shell.create.project.common.ApplicationAssemblerWriter;
import org.apache.zest.tools.shell.create.project.common.ConfigLayerWriter;
import org.apache.zest.tools.shell.create.project.common.ConfigModuleWriter;
import org.apache.zest.tools.shell.create.project.common.ConnectivityLayerWriter;
import org.apache.zest.tools.shell.create.project.common.CrudModuleWriter;
import org.apache.zest.tools.shell.create.project.common.CustomerWriter;
import org.apache.zest.tools.shell.create.project.common.DomainLayerWriter;
import org.apache.zest.tools.shell.create.project.common.FileConfigurationModuleWriter;
import org.apache.zest.tools.shell.create.project.common.HardCodedSecurityRepositoryMixinWriter;
import org.apache.zest.tools.shell.create.project.common.IndexingModuleWriter;
import org.apache.zest.tools.shell.create.project.common.InfrastructureLayerWriter;
import org.apache.zest.tools.shell.create.project.common.OrderItemWriter;
import org.apache.zest.tools.shell.create.project.common.OrderModuleWriter;
import org.apache.zest.tools.shell.create.project.common.OrderWriter;
import org.apache.zest.tools.shell.create.project.common.SecurityModuleWriter;
import org.apache.zest.tools.shell.create.project.common.SecurityRepositoryWriter;
import org.apache.zest.tools.shell.create.project.common.SerializationModuleWriter;
import org.apache.zest.tools.shell.create.project.common.SettingsWriter;
import org.apache.zest.tools.shell.create.project.common.StorageModuleWriter;
import org.apache.zest.tools.shell.create.project.defaultp.ApplicationWriter;

public class DefaultProjectCreator extends AbstractProjectCreator
    implements ProjectCreator
{

    @Override
    public void create( String projectName, File projectDir, Map<String, String> properties )
        throws IOException
    {
        super.create( projectName, projectDir, properties );    // creates the directory structures.
        new ApplicationAssemblerWriter().writeClass( properties );
        new ApplicationWriter().writeClass( properties );
        new ConfigLayerWriter().writeClass( properties );
        new ConfigModuleWriter().writeClass( properties );
        new InfrastructureLayerWriter().writeClass( properties );
        new FileConfigurationModuleWriter().writeClass( properties );
        new StorageModuleWriter().writeClass( properties );
        new IndexingModuleWriter().writeClass( properties );
        new SerializationModuleWriter().writeClass( properties );
        new DomainLayerWriter().writeClass( properties );
        new OrderModuleWriter().writeClass( properties );
        new CrudModuleWriter().writeClass( properties );
        new ConnectivityLayerWriter().writeClass( properties );

        new SecurityModuleWriter().writeClass( properties );
        new SecurityRepositoryWriter().writeClass( properties );
        new HardCodedSecurityRepositoryMixinWriter().writeClass( properties );
        new OrderWriter().writeClass( properties );
        new OrderItemWriter().writeClass( properties );
        new CustomerWriter().writeClass( properties );

        new SettingsWriter().writeClass( properties );
    }
}
