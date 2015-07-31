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
import java.util.List;
import org.qi4j.api.activation.Activator;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.qualifier.ServiceTags;
import org.qi4j.bootstrap.ServiceDeclaration;

import static java.util.Arrays.asList;

/**
 * Declaration of a Service. Created by {@link org.qi4j.runtime.bootstrap.ModuleAssemblyImpl#services(Class[])}.
 */
public final class ServiceDeclarationImpl
    implements ServiceDeclaration
{
    private final Iterable<ServiceAssemblyImpl> serviceAssemblies;

    public ServiceDeclarationImpl( Iterable<ServiceAssemblyImpl> serviceAssemblies )
    {
        this.serviceAssemblies = serviceAssemblies;
    }

    @Override
    public ServiceDeclaration visibleIn( Visibility visibility )
    {
        for( ServiceAssemblyImpl serviceAssembly : serviceAssemblies )
        {
            serviceAssembly.visibility = visibility;
        }
        return this;
    }

    @Override
    public ServiceDeclaration identifiedBy( String identity )
    {
        for( ServiceAssemblyImpl serviceAssembly : serviceAssemblies )
        {
            serviceAssembly.identity = identity;
        }
        return this;
    }

    @Override
    public ServiceDeclaration taggedWith( String... tags )
    {
        for( ServiceAssemblyImpl serviceAssembly : serviceAssemblies )
        {
            ServiceTags previousTags = serviceAssembly.metaInfo.get( ServiceTags.class );
            if( previousTags != null )
            {
                List<String> tagList = new ArrayList<>();
                tagList.addAll( asList( previousTags.tags() ) );
                tagList.addAll( asList( tags ) );
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
    public ServiceDeclaration instantiateOnStartup()
    {
        for( ServiceAssemblyImpl serviceAssembly : serviceAssemblies )
        {
            serviceAssembly.instantiateOnStartup = true;
        }
        return this;
    }

    @Override
    public ServiceDeclaration setMetaInfo( Object serviceAttribute )
    {
        for( ServiceAssemblyImpl serviceAssembly : serviceAssemblies )
        {
            serviceAssembly.metaInfo.set( serviceAttribute );
        }
        return this;
    }

    @Override
    public ServiceDeclaration withConcerns( Class<?>... concerns )
    {
        for( ServiceAssemblyImpl serviceAssembly : serviceAssemblies )
        {
            serviceAssembly.concerns.addAll( asList( concerns ) );
        }
        return this;
    }

    @Override
    public ServiceDeclaration withSideEffects( Class<?>... sideEffects )
    {
        for( ServiceAssemblyImpl serviceAssembly : serviceAssemblies )
        {
            serviceAssembly.sideEffects.addAll( asList( sideEffects ) );
        }
        return this;
    }

    @Override
    public ServiceDeclaration withMixins( Class<?>... mixins )
    {
        for( ServiceAssemblyImpl serviceAssembly : serviceAssemblies )
        {
            serviceAssembly.mixins.addAll( asList( mixins ) );
        }
        return this;
    }

    @Override
    public ServiceDeclaration withTypes( Class<?>... types )
    {
        for( ServiceAssemblyImpl serviceAssembly : serviceAssemblies )
        {
            serviceAssembly.types.addAll( asList( types ) );
        }
        return this;
    }

    @Override
    @SafeVarargs
    public final ServiceDeclaration withActivators( Class<? extends Activator<?>>... activators )
    {
        for ( ServiceAssemblyImpl serviceAssembly : serviceAssemblies ) {
            serviceAssembly.activators.addAll( asList( activators ) );
        }
        return this;
    }

}
