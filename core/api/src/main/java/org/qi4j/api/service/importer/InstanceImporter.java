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

package org.qi4j.api.service.importer;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.MetaInfoHolder;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;

import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.first;
import static org.qi4j.functional.Iterables.map;
import static org.qi4j.functional.Specifications.notNull;

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
