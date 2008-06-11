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
package org.qi4j.entity.neo4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.neo4j.api.core.Transaction;
import org.qi4j.entity.neo4j.state.CommittableEntityState;
import org.qi4j.entity.neo4j.state.DirectEntityStateFactory;
import org.qi4j.entity.neo4j.state.IndirectEntityStateFactory;
import org.qi4j.entity.neo4j.state.LoadedDescriptor;
import org.qi4j.entity.neo4j.state.NeoEntityStateFactory;
import org.qi4j.injection.scope.Service;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.structure.Module;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class NeoEntityStoreMixin implements EntityStore
{
    // Dependancies
    private @Service NeoIdentityIndexService idService;
    private @Service DirectEntityStateFactory directFactory;
    private @Service( optional = true ) IndirectEntityStateFactory indirectFactory;
    private @Service NeoCoreService neo;

    // EntityStore implementation

    public EntityState newEntityState( CompositeDescriptor compositeDescriptor, QualifiedIdentity identity ) throws EntityStoreException
    {
        CommittableEntityState state = factory().createEntityState( idService, load( compositeDescriptor, identity ), identity, EntityStatus.NEW );
        state.preloadState();
        return state;
    }

    public EntityState getEntityState( CompositeDescriptor compositeDescriptor, QualifiedIdentity identity ) throws EntityStoreException
    {
        CommittableEntityState state = factory().createEntityState( idService, load( compositeDescriptor, identity ), identity, EntityStatus.LOADED );
        state.preloadState();
        return state;
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates, Module module ) throws EntityStoreException
    {
        List<CommittableEntityState> updated = new ArrayList<CommittableEntityState>();
        for( EntityState state : newStates )
        {
            updated.add( (CommittableEntityState) state );
        }
        for( EntityState state : loadedStates )
        {
            CommittableEntityState neoState = (CommittableEntityState) state;
            if( neoState.isUpdated() )
            {
                updated.add( neoState );
            }
        }
        return factory().prepareCommit( idService, updated, removedStates );
    }

    public Iterator<EntityState> iterator()
    {
        final Iterator<CommittableEntityState> iter = factory().iterator( idService );
        return new Iterator<EntityState>()
        {
            public boolean hasNext()
            {
                return iter.hasNext();
            }

            public EntityState next()
            {
                return iter.next();
            }

            public void remove()
            {
                iter.remove();
            }
        };
    }

    // Implementation details

    private LoadedDescriptor load( CompositeDescriptor compositeDescriptor, QualifiedIdentity identity )
    {
        Transaction tx = neo.beginTx();
        try
        {
            LoadedDescriptor descriptor = LoadedDescriptor.loadDescriptor( compositeDescriptor, idService.getTypeNode( identity.type() ) );
            tx.success();
            return descriptor;
        }
        finally
        {
            tx.finish();
        }
    }

    private NeoEntityStateFactory factory()
    {
        if( neo.inTransaction() )
        {
            return directFactory;
        }
        else if( indirectFactory != null )
        {
            return indirectFactory;
        }
        else
        {
            return directFactory;
        }
    }
}
