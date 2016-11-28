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

package org.apache.zest.api.service.importer;

import java.util.stream.Stream;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.service.ImportedServiceDescriptor;
import org.apache.zest.api.service.ServiceImporter;
import org.apache.zest.api.service.ServiceImporterException;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Layer;
import org.apache.zest.api.structure.Module;

/**
 * Return a predefined service instance that was provided as meta-info. Search for meta-info in the following order:
 * the service itself, the module of the service, the layer of the service, the whole application.
 */
public final class InstanceImporter<T>
    implements ServiceImporter<T>
{
    @Structure
    private Application application;

    @Structure
    private Layer layer;

    @Structure
    private Module module;

    @Override
    public T importService( final ImportedServiceDescriptor serviceDescriptor )
        throws ServiceImporterException
    {
        return Stream.of( serviceDescriptor, module, layer, application )
            .flatMap( holder -> serviceDescriptor.types().map( type -> (T) holder.metaInfo( type ) ) )
            .filter( meta -> meta != null )
            .findFirst().orElse( null );
    }

    @Override
    public boolean isAvailable( T instance )
    {
        return true;
    }
}
