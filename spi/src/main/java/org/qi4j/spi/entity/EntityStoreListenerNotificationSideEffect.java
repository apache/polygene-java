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
package org.qi4j.spi.entity;

import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.EntityState;

/**
 * Notify all EntityStoreListeners that a change occurred in EntityState
 *
 * @author Alin Dreghiciu
 * @since March 18, 2008
 */
public abstract class EntityStoreListenerNotificationSideEffect extends SideEffectOf<EntityStore>
    implements EntityStore
{
    @Service private Iterable<EntityStoreListener> listeners;

    public StateCommitter prepare( Iterable<EntityState> newStates,
                                   Iterable<EntityState> updatedStates,
                                   Iterable<QualifiedIdentity> removedStates
    )
        throws EntityStoreException
    {
        next.prepare( newStates, updatedStates, removedStates );

        // Only do this if no exception was thrown
        for( EntityStoreListener listener : listeners )
        {
            listener.notifyChanges( newStates, updatedStates, removedStates );
        }
        return null;
    }
}
