/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.entity.index.rdf;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.qi4j.composite.scope.Service;
import org.qi4j.service.Activatable;

/**
 * TODO Add JavaDoc
 *
 */
public class RdfQueryContextMixin
    implements RdfQueryContext, Activatable
{
    @Service private Repository repository;

    public Repository getRepository()
    {
        return repository;
    }

    public void activate()
        throws Exception
    {
        repository.initialize();
    }

    public void passivate()
        throws Exception
    {
        repository.shutDown();
    }
}