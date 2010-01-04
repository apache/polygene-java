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
import org.openrdf.sail.memory.MemoryStore;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

@Mixins( MemoryRepositoryService.MemoryRepositoryMixin.class )
public interface MemoryRepositoryService extends Repository, ServiceComposite, Activatable
{
    public static class MemoryRepositoryMixin
        implements Repository, ResetableRepository, Activatable
    {
        SailRepository repo;

        public MemoryRepositoryMixin()
        {
            repo = new SailRepository( new MemoryStore() );
        }

        public void activate()
            throws Exception
        {
            repo.initialize();
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
            return repo.getConnection();
        }

        public ValueFactory getValueFactory()
        {
            return repo.getValueFactory();
        }

        public void discardEntireRepository()
            throws RepositoryException
        {
            repo = new SailRepository( new MemoryStore() );
        }
    }
}
