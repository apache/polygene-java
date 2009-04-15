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
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.service.ServiceDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

public class CoherenceEntityStoreMixin
        implements Activatable, EntityStore
{
    private
    @This
    ReadWriteLock lock;
    private
    @This
    Configuration<CoherenceConfiguration> config;
    private
    @Uses
    ServiceDescriptor descriptor;
    private NamedCache cache;

    // Activatable implementation
    public void activate()
            throws Exception
    {
        String cacheName = config.configuration().cacheName().get();
        cache = CacheFactory.getCache(cacheName);
    }

    public void passivate()
            throws Exception
    {
        cache.destroy();
    }

    public EntityState newEntityState(EntityReference reference)
            throws EntityStoreException
    {
        return new CoherenceEntityState(reference);
    }

    public EntityState getEntityState(EntityReference reference)
            throws EntityStoreException
    {
        // Synchronization on non-final is valid semantics here. Only set in activate()/deactivate() pair of methods.
        //noinspection SynchronizeOnNonFinalField
        synchronized (cache)
        {
            CoherenceEntityState state = (CoherenceEntityState) cache.get(reference);
            if (state == null)
            {
                throw new EntityNotFoundException(reference);
            }
            return state;
        }
    }

    public StateCommitter prepare(Iterable<EntityState> newStates,
                                  Iterable<EntityState> loadedStates,
                                  final Iterable<EntityReference> removedStates)
            throws EntityStoreException
    {
        final Map<EntityReference, CoherenceEntityState> updatedState =
                new HashMap<EntityReference, CoherenceEntityState>();

        for (EntityState entityState : newStates)
        {
            CoherenceEntityState entityStateInstance = (CoherenceEntityState) entityState;
            updatedState.put(entityState.identity(), entityStateInstance);
        }

        for (EntityState entityState : loadedStates)
        {
            CoherenceEntityState entityStateInstance = (CoherenceEntityState) entityState;
            if (entityStateInstance.isModified())
            {
                updatedState.put(entityState.identity(), entityStateInstance);
            }
        }
        return new StateCommitter()
        {
            public void commit()
            {
                // Synchronization on non-final is valid semantics here. Only set in activate()/deactivate() pair of methods.
                //noinspection SynchronizeOnNonFinalField
                synchronized (cache)
                {
                    // Remove state
                    for (EntityReference removedEntityId : removedStates)
                    {
                        cache.remove(removedEntityId);
                    }

                    // Update state
                    for (Map.Entry<EntityReference, CoherenceEntityState> state : updatedState.entrySet())
                    {
                        final CoherenceEntityState value = state.getValue();
                        if (value.status() == EntityStatus.LOADED)
                        {
                            if (value.isModified())
                            {
                                value.increaseVersion();
                            }
                        } else
                        {
                            value.markAsLoaded();
                        }
                        value.clearModified();
                        cache.put(state.getKey(), value);
                    }
                }
            }

            public void cancel()
            {
                // Do nothing
            }
        };
    }

    public void visitEntityStates(EntityStateVisitor visitor)
    {
        for (Object entityState : cache.values())
        {
            visitor.visitEntityState((EntityState) entityState);
        }
    }
}
