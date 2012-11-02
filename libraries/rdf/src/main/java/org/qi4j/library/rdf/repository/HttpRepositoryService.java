/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.library.rdf.repository;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;

@Mixins( HttpRepositoryService.HttpRepositoryMixin.class )
@Activators( HttpRepositoryService.Activator.class )
public interface HttpRepositoryService
    extends Repository, ServiceComposite
{
    @Override
    void initialize()
            throws RepositoryException;

    @Override
    void shutDown()
            throws RepositoryException;

    public static class Activator
            extends ActivatorAdapter<ServiceReference<HttpRepositoryService>>
    {

        @Override
        public void afterActivation( ServiceReference<HttpRepositoryService> activated )
                throws Exception
        {
            activated.get().initialize();
        }

        @Override
        public void beforePassivation( ServiceReference<HttpRepositoryService> passivating )
                throws Exception
        {
            passivating.get().shutDown();
        }

    }

    public static class HttpRepositoryMixin
        extends HTTPRepository
        implements Repository
    {
        public HttpRepositoryMixin( @This Configuration<HttpRepositoryConfiguration> configuration )
        {
            super( getRepositoryUrl( configuration.get() ), getRepositoryId( configuration.get() ) );
        }

        private static String getRepositoryUrl( HttpRepositoryConfiguration configuration )
        {
            Property<String> repositoryUrl = configuration.repositoryUrl();
            String url = repositoryUrl.get();
            if( url == null )
            {
                url = "http://localhost:8183/";
                repositoryUrl.set( url );
            }
            return url;
        }

        private static String getRepositoryId( HttpRepositoryConfiguration configuration )
        {
            Property<String> id = configuration.repositoryId();
            if( id.get() == null )
            {
                id.set( "qi4j" );
            }
            return id.get();
        }
    }
}
