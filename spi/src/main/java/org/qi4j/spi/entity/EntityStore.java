/*  Copyright 2007 Niclas Hedhman.
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
package org.qi4j.spi.entity;

import java.lang.reflect.Method;
import java.util.Map;
import org.qi4j.entity.EntitySession;
import org.qi4j.spi.composite.CompositeBinding;

/**
 * Interface that must be implemented by store for
 * persistent state of EntityComposites.
 */
public interface EntityStore<T extends EntityState>
{
    /**
     * Create new EntityState for a given identity and
     * composite type.
     * <p/>
     * This should only create the EntityState
     * and not insert it into any database, since
     * that should occur during the {@link #prepare(org.qi4j.entity.EntitySession, Iterable)}
     * call.
     *
     * @param session          the session for which to create the EntityState
     * @param identity         the identity of the entity
     * @param compositeBinding the composite binding for the entity
     * @param propertyValues   the value of the properties.
     * @return a new EntityState implementation
     * @throws StoreException
     */
    T newEntityState( EntitySession session,
                      String identity,
                      CompositeBinding compositeBinding,
                      Map<Method, Object> propertyValues )
        throws StoreException;

    /**
     * Get the EntityState for a given identity
     * and composite type.
     *
     * @param session
     * @param identity
     * @param compositeBinding
     * @return
     * @throws StoreException
     */
    T getEntityState( EntitySession session,
                      String identity,
                      CompositeBinding compositeBinding )
        throws StoreException;

    /**
     * This method is called by {@link org.qi4j.entity.EntitySession#complete()}.
     * The implementation of this method should take the state and send any changes
     * to the underlying datastore. The method returns a StateCommitter that the session
     * will invoke once all EntityStore's have been prepared.
     *
     * @param session the session for the state
     * @param states  the state to send to the datastore
     * @return an implementation of StateCommitter
     * @throws StoreException if the state could not be sent to the datastore
     */
    StateCommitter prepare( EntitySession session, Iterable<T> states )
        throws StoreException;
}
