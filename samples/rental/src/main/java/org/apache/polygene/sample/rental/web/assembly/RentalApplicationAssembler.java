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

package org.apache.polygene.sample.rental.web.assembly;

import org.apache.polygene.api.structure.Application;
import org.apache.polygene.bootstrap.ApplicationAssembler;
import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.ApplicationAssemblyFactory;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.LayerAssembly;

public class RentalApplicationAssembler
    implements ApplicationAssembler
{
    private Application.Mode mode;

    public RentalApplicationAssembler( Application.Mode mode )
    {
        this.mode = mode;
    }

    @Override
    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
        throws AssemblyException
    {
        ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
        assembly.setMode( mode );
        LayerAssembly webLayer = assembly.layer( "WebLayer" );
        new PagesModule().assemble( webLayer.module( "PagesModule" ) );
        LayerAssembly domainLayer = assembly.layer( "DomainLayer" );
        new RentalModule().assemble( domainLayer.module( "RentalModule" ) );
        LayerAssembly infraLayer = assembly.layer( "InfraLayer" );
        new StorageModule().assemble( infraLayer.module( "StorageModule" ) );

        webLayer.uses( domainLayer );
        domainLayer.uses( infraLayer );
        return assembly;
    }
}
