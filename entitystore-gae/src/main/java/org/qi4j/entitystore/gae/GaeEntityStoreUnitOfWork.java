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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import java.util.ArrayList;
import java.util.LinkedList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;
import org.qi4j.spi.structure.ModuleSPI;

public class GaeEntityStoreUnitOfWork
    implements EntityStoreUnitOfWork
{
    private DatastoreService datastore;
    private String identity;
    private LinkedList<GaeEntityState> states;
    private ModuleSPI module;
    private long currentTime;

    public GaeEntityStoreUnitOfWork( DatastoreService datastore, String identity, ModuleSPI module, long currentTime )
    {
        this.module = module;
        this.currentTime = currentTime;
        states = new LinkedList<GaeEntityState>();
        this.datastore = datastore;
        this.identity = identity;
    }

    public String identity()
    {
        return identity;
    }

    public long currentTime()
    {
        return currentTime;
    }

    public EntityState newEntityState( EntityReference anIdentity, EntityDescriptor entityDescriptor )
        throws EntityStoreException
    {
        Key key = KeyFactory.createKey( "qi4j-entity", anIdentity.identity() );
        GaeEntityState state = new GaeEntityState( this, key, entityDescriptor, module );
        states.add( state );
        return state;
    }

    public EntityState getEntityState( EntityReference reference )
        throws EntityStoreException, EntityNotFoundException
    {
        Key key = KeyFactory.createKey( "qi4j-entity", reference.identity() );
        try
        {
            Entity entity = datastore.get( key );
            GaeEntityState state = new GaeEntityState( this, entity, module );
            states.add( state );
            return state;
        }
        catch( com.google.appengine.api.datastore.EntityNotFoundException e )
        {
            throw new EntityNotFoundException( reference );
        }
    }

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

        public void commit()
        {
            transaction.commit();
        }

        public void cancel()
        {
            transaction.rollback();
        }
    }
}
