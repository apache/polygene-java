/* Copyright 2008 Neo Technology, http://neotechnology.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.neo4j;

import org.neo4j.api.core.Transaction;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.entitystore.neo4j.state.*;
import org.qi4j.spi.entity.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class NeoEntityStoreMixin
        implements EntityStore
{
    // Dependancies
    private
    @Service
    NeoIdentityIndexService idService;
    private
    @Service
    DirectEntityStateFactory directFactory;
    private
    @Optional
    @Service
    IndirectEntityStateFactory indirectFactory;
    private
    @Service
    NeoCoreService neo;

    // EntityStore implementation

    public EntityState newEntityState(EntityReference reference) throws EntityStoreException
    {
/*
        EntityType type = getEntityType( reference.type() );
        CommittableEntityState state = factory().createEntityState( idService, load( type, reference), reference, EntityStatus.NEW );
        state.preloadState();
        return state;
*/
        return null;
    }

    public EntityState getEntityState(EntityReference reference) throws EntityStoreException
    {
/*
        EntityType type = getEntityType( reference.type() );
        CommittableEntityState state = factory().createEntityState( idService, load( type, reference), reference, EntityStatus.LOADED );
        state.preloadState();
        return state;
*/
        return null;
    }

    public StateCommitter prepare(Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<EntityReference> removedStates) throws EntityStoreException
    {
        List<CommittableEntityState> updated = new ArrayList<CommittableEntityState>();
        for (EntityState state : newStates)
        {
            updated.add((CommittableEntityState) state);
        }
        for (EntityState state : loadedStates)
        {
            CommittableEntityState neoState = (CommittableEntityState) state;
            if (neoState.isUpdated())
            {
                updated.add(neoState);
            }
        }
        return factory().prepareCommit(idService, updated, removedStates);
    }

    public void visitEntityStates(EntityStateVisitor visitor)
    {
        final Iterator<CommittableEntityState> iter = factory().iterator(idService);
        while (iter.hasNext())
        {
            CommittableEntityState state = iter.next();
            visitor.visitEntityState(state);
        }
    }

    // Implementation details

    private LoadedDescriptor load(EntityType entityType, EntityReference reference)
    {
        Transaction tx = neo.beginTx();
        try
        {
// TODO           LoadedDescriptor descriptor = LoadedDescriptor.loadDescriptor( entityType, idService.getTypeNode( reference.type() ) );
            tx.success();
            return null;
        }
        finally
        {
            tx.finish();
        }
    }

    private NeoEntityStateFactory factory()
    {
        if (neo.inTransaction())
        {
            return directFactory;
        } else if (indirectFactory != null)
        {
            return indirectFactory;
        } else
        {
            return directFactory;
        }
    }
}
