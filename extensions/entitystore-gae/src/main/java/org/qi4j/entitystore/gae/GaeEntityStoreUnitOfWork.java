/*
 * Copyright 2010 Niclas Hedhman.
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

package org.qi4j.entitystore.gae;

import com.google.appengine.api.datastore.*;
import java.util.LinkedList;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;

public class GaeEntityStoreUnitOfWork
    implements EntityStoreUnitOfWork
{
    private final DatastoreService datastore;
    private final ValueSerialization valueSerialization;
    private final String identity;
    private final Module module;
    private final long currentTime;
    private final LinkedList<GaeEntityState> states;

    public GaeEntityStoreUnitOfWork( DatastoreService datastore,
                                     ValueSerialization valueSerialization,
                                     String identity,
                                     Module module,
                                     long currentTime )
    {
        this.datastore = datastore;
        this.valueSerialization = valueSerialization;
        this.identity = identity;
        this.module = module;
        this.currentTime = currentTime;
        states = new LinkedList<GaeEntityState>();
    }

    @Override
    public String identity()
    {
        return identity;
    }

    @Override
    public long currentTime()
    {
        return currentTime;
    }

    @Override
    public EntityState newEntityState( EntityReference anIdentity, EntityDescriptor entityDescriptor )
        throws EntityStoreException
    {
        Key key = KeyFactory.createKey( "qi4j-entity", anIdentity.identity() );
        GaeEntityState state = new GaeEntityState( this, valueSerialization, key, entityDescriptor, module );
        states.add( state );
        return state;
    }

    @Override
    public EntityState entityStateOf( EntityReference reference )
        throws EntityStoreException, EntityNotFoundException
    {
        Key key = KeyFactory.createKey( "qi4j-entity", reference.identity() );
        try
        {
            Entity entity = datastore.get( key );
            GaeEntityState state = new GaeEntityState( this, valueSerialization, entity, module );
            states.add( state );
            return state;
        }
        catch( com.google.appengine.api.datastore.EntityNotFoundException e )
        {
            throw new EntityNotFoundException( reference );
        }
    }

    @Override
    public StateCommitter applyChanges()
        throws EntityStoreException
    {
        Transaction transaction = datastore.beginTransaction();
        for( GaeEntityState state : states )
        {
            Entity entity = state.entity();
            if( state.status() == EntityStatus.NEW ||
                state.status() == EntityStatus.UPDATED )
            {
                datastore.put( transaction, entity );
            }
            if( state.status() == EntityStatus.REMOVED )
            {
                datastore.delete( transaction, entity.getKey() );
            }
        }
        return new GaeStateCommitter( transaction );
    }

    @Override
    public void discard()
    {
        // nothing to do??
    }

    private static class GaeStateCommitter
        implements StateCommitter
    {
        private Transaction transaction;

        public GaeStateCommitter( Transaction transaction )
        {
            this.transaction = transaction;
        }

        @Override
        public void commit()
        {
            transaction.commit();
        }

        @Override
        public void cancel()
        {
            transaction.rollback();
        }
    }
}
