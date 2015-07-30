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

package org.apache.zest.api.service.importer;

import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.service.ImportedServiceDescriptor;
import org.apache.zest.api.service.ServiceImporter;
import org.apache.zest.api.service.ServiceImporterException;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Layer;
import org.apache.zest.api.structure.MetaInfoHolder;
import org.apache.zest.api.structure.Module;
import org.apache.zest.functional.Function;
import org.apache.zest.functional.Iterables;

import static org.apache.zest.functional.Iterables.filter;
import static org.apache.zest.functional.Iterables.first;
import static org.apache.zest.functional.Iterables.map;
import static org.apache.zest.functional.Specifications.notNull;

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
        T instance = null;
        Iterable<MetaInfoHolder> holders = Iterables.iterable( serviceDescriptor, module, layer, application );
        for( final MetaInfoHolder metaInfoHolder : holders )
        {
            Function<Class<?>, T> metaFinder = new Function<Class<?>, T>()
            {
                @Override
                @SuppressWarnings( "unchecked" )
                public T map( Class<?> type )
                {
                    return (T) metaInfoHolder.metaInfo( type );
                }
            };
            instance = first( filter( notNull(), map( metaFinder, serviceDescriptor.types() ) ) );
            if( instance != null )
            {
                break;
            }
        }
        return instance;
    }

    @Override
    public boolean isAvailable( T instance )
    {
        return true;
    }
}
