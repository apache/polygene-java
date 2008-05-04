/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.spi.query;

import org.qi4j.composite.SideEffectOf;
import org.qi4j.composite.scope.Service;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.structure.ModuleBinding;
import org.qi4j.structure.Module;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 18, 2008
 */
public abstract class IndexingSideEffect extends SideEffectOf<EntityStore>
    implements EntityStore
{
    @Service private EntityIndexer indexer;

    public StateCommitter prepare( Iterable<EntityState> newStates,
                                   Iterable<EntityState> loadedStates,
                                   Iterable<QualifiedIdentity> removedStates,
                                   Module module )
        throws EntityStoreException
    {
        indexer.index( newStates, loadedStates, removedStates, module );
        return null;
    }
}
