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
package org.qi4j.entitystore.coherence;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.EntityTypeRegistryMixin;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.UnknownEntityTypeException;

public class CoherenceEntityStoreMixin extends EntityTypeRegistryMixin
    implements Activatable
{
    private @This ReadWriteLock lock;
    private @This Configuration<CoherenceConfiguration> config;
    private @Uses ServiceDescriptor descriptor;
    private NamedCache cache;

    // Activatable implementation
    public void activate()
        throws Exception
    {
        String cacheName = config.configuration().cacheName().get();
        cache = CacheFactory.getCache( cacheName );
    }

    public void passivate()
        throws Exception
    {
        cache.destroy();
    }

    public EntityState newEntityState( QualifiedIdentity identity )
        throws EntityStoreException
    {
        EntityType entityType = getEntityType( identity.type() );
        return new CoherenceEntityState( identity, entityType );
    }

    public EntityState getEntityState( QualifiedIdentity identity )
        throws EntityStoreException
    {
        EntityType entityType = getEntityType( identity.type() );
        if( entityType == null )
        {
            throw new UnknownEntityTypeException( identity.type() );
        }

        // Synchronization on non-final is valid semantics here. Only set in activate()/deactivate() pair of methods.
        //noinspection SynchronizeOnNonFinalField
        synchronized( cache )
        {
            CoherenceEntityState state = (CoherenceEntityState) cache.get( identity );
            if( state == null )
            {
                throw new EntityNotFoundException( descriptor.identity(), identity );
            }
            state.markAsLoaded();
            state.clearModified();
            return state;
        }
    }

    public StateCommitter prepare( Iterable<EntityState> newStates,
                                   Iterable<EntityState> loadedStates,
                                   final Iterable<QualifiedIdentity> removedStates )
        throws EntityStoreException
    {
        final Map<QualifiedIdentity, CoherenceEntityState> updatedState =
            new HashMap<QualifiedIdentity, CoherenceEntityState>();

        for( EntityState entityState : newStates )
        {
            CoherenceEntityState entityStateInstance = (CoherenceEntityState) entityState;
            updatedState.put( entityState.qualifiedIdentity(), entityStateInstance );
        }

        for( EntityState entityState : loadedStates )
        {
            CoherenceEntityState entityStateInstance = (CoherenceEntityState) entityState;
            if( entityStateInstance.isModified() )
            {
                updatedState.put( entityState.qualifiedIdentity(), entityStateInstance );
            }
        }
        return new StateCommitter()
        {
            public void commit()
            {
                // Synchronization on non-final is valid semantics here. Only set in activate()/deactivate() pair of methods.
                //noinspection SynchronizeOnNonFinalField
                synchronized( cache )
                {
                    // Remove state
                    for( QualifiedIdentity removedEntityId : removedStates )
                    {
                        cache.remove( removedEntityId );
                    }

                    // Update state
                    for( Map.Entry<QualifiedIdentity, CoherenceEntityState> state : updatedState.entrySet() )
                    {
                        final CoherenceEntityState value = state.getValue();
                        if( value.status() == EntityStatus.LOADED )
                        {
                            value.updateVersion();
                        }
                        cache.put( state.getKey(), value );
                        value.clearModified();
                    }
                }
            }

            public void cancel()
            {
                // Do nothing
            }
        };
    }

    public Iterator<EntityState> iterator()
    {
        final Iterator iterator = cache.keySet().iterator();
        return new Iterator<EntityState>()
        {
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            public EntityState next()
            {
                return getEntityState( (QualifiedIdentity) iterator.next() );
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

}
