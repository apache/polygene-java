/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.service.importer;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;

/**
 * Return a predefined service instance that was provided as meta-info. Search for meta-info in the following order:
 * the service itself, the module of the service, the layer of the service, the whole application.
 */
public final class InstanceImporter
    implements ServiceImporter
{
    @Structure
    Application application;
    @Structure
    Layer layer;
    @Structure
    Module module;

    public Object importService( ImportedServiceDescriptor serviceDescriptor )
        throws ServiceImporterException
    {
        Object instance = serviceDescriptor.metaInfo( serviceDescriptor.type() );
        if( instance == null )
        {
            instance = module.metaInfo( serviceDescriptor.type() );
            if( instance == null )
            {
                instance = layer.metaInfo( serviceDescriptor.type() );
            }
            {
                if( instance == null )
                {
                    instance = application.metaInfo( serviceDescriptor.type() );
                }
            }
        }

        return instance;
    }

    public boolean isActive( Object instance )
    {
        return true;
    }
}
