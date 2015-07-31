/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin.
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

package org.qi4j.runtime.bootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.qi4j.api.activation.Activator;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.qualifier.ServiceTags;
import org.qi4j.bootstrap.ImportedServiceDeclaration;

/**
 * Declaration of an imported Service.
 */
public final class ImportedServiceDeclarationImpl
    implements ImportedServiceDeclaration
{
    private final Iterable<ImportedServiceAssemblyImpl> assemblies;

    public ImportedServiceDeclarationImpl( Iterable<ImportedServiceAssemblyImpl> assemblies )
    {
        this.assemblies = assemblies;
    }

    @Override
    public ImportedServiceDeclaration importOnStartup()
    {
        for( ImportedServiceAssemblyImpl assembly : assemblies )
        {
            assembly.importOnStartup = true;
        }
        return this;
    }

    @Override
    public ImportedServiceDeclaration visibleIn( Visibility visibility )
    {
        for( ImportedServiceAssemblyImpl assembly : assemblies )
        {
            assembly.visibility = visibility;
        }
        return this;
    }

    @Override
    @SuppressWarnings( "raw" )
    public ImportedServiceDeclaration importedBy( Class<? extends ServiceImporter> sip )
    {
        for( ImportedServiceAssemblyImpl assembly : assemblies )
        {
            assembly.serviceProvider = sip;
        }
        return this;
    }

    @Override
    public ImportedServiceDeclaration identifiedBy( String identity )
    {
        for( ImportedServiceAssemblyImpl assembly : assemblies )
        {
            assembly.identity = identity;
        }
        return this;
    }

    @Override
    public ImportedServiceDeclaration taggedWith( String... tags )
    {
        for( ImportedServiceAssemblyImpl serviceAssembly : assemblies )
        {
            ServiceTags previousTags = serviceAssembly.metaInfo.get( ServiceTags.class );
            if( previousTags != null )
            {
                List<String> tagList = new ArrayList<>();
                Collections.addAll( tagList, previousTags.tags() );
                Collections.addAll( tagList, tags );
                serviceAssembly.metaInfo.set( new ServiceTags( tagList.toArray( new String[ tagList.size() ] ) ) );
            }
            else
            {
                serviceAssembly.metaInfo.set( new ServiceTags( tags ) );
            }
        }

        return this;
    }

    @Override
    public ImportedServiceDeclaration setMetaInfo( Object serviceAttribute )
    {
        for( ImportedServiceAssemblyImpl assembly : assemblies )
        {
            assembly.metaInfo.set( serviceAttribute );
        }
        return this;
    }

    @Override
    @SafeVarargs
    public final ImportedServiceDeclaration withActivators( Class<? extends Activator<?>>... activators )
    {
        for ( ImportedServiceAssemblyImpl serviceAssembly : assemblies ) {
            serviceAssembly.activators.addAll( Arrays.asList( activators ) );
        }
        return this;
    }
}