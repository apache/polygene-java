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
import org.openrdf.sail.Sail;
import org.openrdf.sail.nativerdf.NativeStore;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;

public class NativeRepositoryMixin
    implements Repository, Activatable
{
    @This private Configuration<NativeConfiguration> configuration;

    SailRepository repo;

    public NativeRepositoryMixin()
    {
        repo = new SailRepository( new NativeStore() );
    }

    public void activate() throws Exception
    {
        Sail store = repo.getSail();
        NativeStore store2 = (NativeStore) store;
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
            configuration.configuration().unitOfWork().apply();
        }
        store2.setDataDir( new File( dataDir ) );
        String tripleIndexes = configuration.configuration().tripleIndexes().get();
        if( tripleIndexes == null )
        {
            tripleIndexes = "";
            configuration.configuration().tripleIndexes().set( tripleIndexes );
        }
        store2.setTripleIndexes( tripleIndexes );
        boolean forceSync = configuration.configuration().forceSync().get();
        store2.setForceSync( forceSync );
        repo.initialize();
    }

    public void passivate()
        throws Exception
    {
        repo.shutDown();
    }

    public void setDataDir( File dataDir )
    {
    }

    public File getDataDir()
    {
        return null;
    }

    public void initialize() throws RepositoryException
    {
    }

    public void shutDown() throws RepositoryException
    {
    }

    public boolean isWritable() throws RepositoryException
    {
        return repo.isWritable();
    }

    public RepositoryConnection getConnection() throws RepositoryException
    {
        return repo.getConnection();
    }

    public ValueFactory getValueFactory()
    {
        return repo.getValueFactory();
    }
}
