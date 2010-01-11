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

import java.io.File;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

@Mixins( { NativeRepositoryService.NativeRepositoryMixin.class } )
public interface NativeRepositoryService extends Repository, ServiceComposite, Activatable
{
    public static class NativeRepositoryMixin
        implements Repository, ResetableRepository, Activatable
    {
        @This
        private Configuration<NativeConfiguration> configuration;

        @Structure
        private UnitOfWorkFactory uowf;
        
        private SailRepository repo;
        private boolean isNotInitialized;

        public NativeRepositoryMixin()
        {
            isNotInitialized = true;
            repo = new SailRepository( new NativeStore() );
        }

        public void activate()
            throws UnitOfWorkCompletionException, RepositoryException
        {
            String dataDir = configuration.configuration().dataDirectory().get();
            if( dataDir == null || "".equals( dataDir ) )
            {
                String id = configuration.configuration().identity().get();
                if( id == null || "".equals( id ) )
                {
                    dataDir = "./rdf/repositories/qi4j";
                }
                else
                {
                    dataDir = "./rdf/repositories/" + id;
                }
                configuration.configuration().dataDirectory().set( dataDir );
                uowf.getUnitOfWork( configuration.configuration() ).apply();
            }
            initializeRepository( new File( dataDir ) );
        }

        public void passivate()
            throws Exception
        {
            repo.shutDown();
        }

        public void setDataDir( File dataDir )
        {
            repo.setDataDir( dataDir );
        }

        public File getDataDir()
        {
            return repo.getDataDir();
        }

        public void initialize()
            throws RepositoryException
        {
            repo.initialize();
        }

        public void shutDown()
            throws RepositoryException
        {
        }

        public boolean isWritable()
            throws RepositoryException
        {
            return repo.isWritable();
        }

        public RepositoryConnection getConnection()
            throws RepositoryException
        {
            if( isNotInitialized )
            {
                return null;
            }
            return repo.getConnection();
        }

        public ValueFactory getValueFactory()
        {
            return repo.getValueFactory();
        }

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
            String tripleIndexes = configuration.configuration().tripleIndexes().get();
            if( tripleIndexes == null )
            {
                tripleIndexes = "";
                configuration.configuration().tripleIndexes().set( tripleIndexes );
            }
            boolean forceSync = configuration.configuration().forceSync().get();

            NativeStore store = (NativeStore) repo.getSail();
            store.setDataDir( dataDir );
            store.setTripleIndexes( tripleIndexes );
            store.setForceSync( forceSync );
            repo.initialize();
            isNotInitialized = false;
        }
    }
}
