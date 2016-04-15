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
package org.apache.zest.library.rdf.repository;

import java.io.File;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.apache.zest.api.activation.ActivatorAdapter;
import org.apache.zest.api.activation.Activators;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.Availability;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.library.fileconfig.FileConfiguration;

@Mixins( { NativeRepositoryService.NativeRepositoryMixin.class } )
@Activators( NativeRepositoryService.Activator.class )
public interface NativeRepositoryService extends Repository, ServiceComposite, Availability
{
    @Override
    void initialize()
        throws RepositoryException;

    @Override
    void shutDown()
        throws RepositoryException;

    class Activator
        extends ActivatorAdapter<ServiceReference<NativeRepositoryService>>
    {

        @Override
        public void afterActivation( ServiceReference<NativeRepositoryService> activated )
            throws Exception
        {
            activated.get().initialize();
        }

        @Override
        public void beforePassivation( ServiceReference<NativeRepositoryService> passivating )
            throws Exception
        {
            passivating.get().shutDown();
        }
    }

    abstract class NativeRepositoryMixin
        implements NativeRepositoryService, ResetableRepository
    {
        @Optional
        @Service
        FileConfiguration fileConfiguration;

        @This
        private Configuration<NativeConfiguration> configuration;

        private SailRepository repo;
        private boolean isNotInitialized;

        public NativeRepositoryMixin()
        {
            isNotInitialized = true;
            repo = new SailRepository( new NativeStore() );
        }

        @Override
        public void setDataDir( File dataDir )
        {
            repo.setDataDir( dataDir );
        }

        @Override
        public File getDataDir()
        {
            return repo.getDataDir();
        }

        @Override
        public void initialize()
            throws RepositoryException
        {
            String dataDir = configuration.get().dataDirectory().get();
            File dataDirectory;
            if( dataDir == null || "".equals( dataDir ) )
            {
                String serviceIdentity = configuration.get().identity().get();
                if( fileConfiguration != null )
                {
                    dataDir = new File( fileConfiguration.dataDirectory(), serviceIdentity ).getAbsolutePath();
                }
                else
                {
                    if( serviceIdentity == null || "".equals( serviceIdentity ) )
                    {
                        dataDir = "./rdf/repositories/zest";
                    }
                    else
                    {
                        dataDir = "./rdf/repositories/" + serviceIdentity;
                    }
                }
                configuration.get().dataDirectory().set( dataDir );
                configuration.save();
                dataDirectory = new File( dataDir );
            }
            else
            {
                dataDirectory = new File( dataDir ).getAbsoluteFile();
            }
            initializeRepository( dataDirectory );
        }

        @Override
        public boolean isInitialized()
        {
            return !isNotInitialized;
        }

        @Override
        public void shutDown()
            throws RepositoryException
        {
            repo.shutDown();
        }

        @Override
        public boolean isWritable()
            throws RepositoryException
        {
            return repo.isWritable();
        }

        @Override
        public RepositoryConnection getConnection()
            throws RepositoryException
        {
            if( isNotInitialized )
            {
                return null;
            }
            return repo.getConnection();
        }

        @Override
        public ValueFactory getValueFactory()
        {
            return repo.getValueFactory();
        }

        @Override
        public void discardEntireRepository()
            throws RepositoryException
        {
            File dataDir = repo.getDataDir();
            repo.shutDown();
            delete( dataDir );
            initializeRepository( dataDir );
        }

        private void delete( File dataDir )
        {
            File[] children = dataDir.listFiles();
            if( children == null )
            {
                return;
            }
            for( File child : children )
            {
                if( child.isDirectory() )
                {
                    delete( child );
                }
                else
                {
                    //noinspection ResultOfMethodCallIgnored
                    child.delete();
                }
            }
        }

        private void initializeRepository( File dataDir )
            throws RepositoryException
        {
            String tripleIndexes = configuration.get().tripleIndexes().get();
            if( tripleIndexes == null )
            {
                tripleIndexes = "";
                configuration.get().tripleIndexes().set( tripleIndexes );
            }
            boolean forceSync = configuration.get().forceSync().get();

            NativeStore store = (NativeStore) repo.getSail();
            store.setDataDir( dataDir );
            store.setTripleIndexes( tripleIndexes );
            store.setForceSync( forceSync );
            repo.initialize();
            isNotInitialized = false;
        }

        @Override
        public boolean isAvailable()
        {
            return !isNotInitialized;
        }
    }
}
