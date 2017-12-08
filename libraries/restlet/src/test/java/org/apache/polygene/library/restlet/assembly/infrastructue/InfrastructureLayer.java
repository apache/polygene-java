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

package org.apache.polygene.library.restlet.assembly.infrastructue;

import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.layered.LayerAssembler;
import org.apache.polygene.bootstrap.layered.LayeredLayerAssembler;
import org.apache.polygene.library.restlet.assembly.configuration.ConfigurationLayer;
import org.apache.polygene.library.restlet.assembly.configuration.ConfigurationModule;

public class InfrastructureLayer extends LayeredLayerAssembler
    implements LayerAssembler
{
    public static final String NAME = "Infrastructure Layer";
    private final ModuleAssembly configModule;

    public static InfrastructureLayer create( LayerAssembly layer )
    {
        ModuleAssembly config = layer.application().layer( ConfigurationLayer.NAME ).module( ConfigurationModule.NAME );
        return new InfrastructureLayer( config );
    }

    private InfrastructureLayer( ModuleAssembly configModule )
    {
        this.configModule = configModule;
    }

    @Override
    public LayerAssembly assemble( LayerAssembly layer )
    {
        new FileStorageModule( configModule ).assemble( layer, layer.module( FileStorageModule.NAME ) );
        new IndexingModule( configModule ).assemble( layer, layer.module( IndexingModule.NAME ) );
        new SerializationModule().assemble( layer, layer.module( SerializationModule.NAME ) );
        return layer;
    }
}
