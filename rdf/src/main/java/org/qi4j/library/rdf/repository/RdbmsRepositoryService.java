/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rdf.repository;

import java.io.File;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.RdbmsStore;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

@Mixins( RdbmsRepositoryService.RdbmsRepositoryMixin.class )
public interface RdbmsRepositoryService extends Repository, ServiceComposite, Activatable
{
    public static class RdbmsRepositoryMixin
        implements Repository, Activatable
    {
        @This
        private Configuration<RdbmsRepositoryConfiguration> configuration;

        @Structure
        private UnitOfWorkFactory uowf;

        private SailRepository repo;

        public void activate()
            throws Exception
        {
            initialize();
        }

        public void passivate()
            throws Exception
        {
            shutDown();
        }

        public void setDataDir( File file )
        {
            repo.setDataDir( file );
        }

        public File getDataDir()
        {
            return repo.getDataDir();
        }

        public void initialize()
            throws RepositoryException
        {
            RdbmsRepositoryConfiguration conf = configuration.configuration();
            String jdbcDriver = conf.jdbcDriver().get();
            String jdbcUrl = conf.jdbcUrl().get();
            String user = conf.user().get();
            String password = conf.password().get();
            repo = new SailRepository( new RdbmsStore( jdbcDriver, jdbcUrl, user, password ) );
            repo.initialize();
        }

        public void shutDown()
            throws RepositoryException
        {
            repo.shutDown();
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
    }
}
