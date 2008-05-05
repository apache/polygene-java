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
package org.qi4j.entity.index.rdf.memory;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.Sail;
import org.qi4j.service.Activatable;

public class MemoryRepositoryMixin extends SailRepository
    implements Repository, Activatable
{
    public MemoryRepositoryMixin()
    {
        super( new ForwardChainingRDFSInferencer(new MemoryStore()) );
    }

    public void activate() throws Exception
    {
        getSail().initialize();
    }

    public void passivate() throws Exception
    {
        ForwardChainingRDFSInferencer sail1 = (ForwardChainingRDFSInferencer) getSail();
        Sail sail2 = sail1.getBaseSail();
        sail2.shutDown();
        sail1.shutDown();
        shutDown();
    }
}
