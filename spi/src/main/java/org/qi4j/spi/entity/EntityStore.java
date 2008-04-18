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

import org.qi4j.spi.structure.CompositeDescriptor;
import org.qi4j.structure.Module;

/**
 * Interface that must be implemented by store for
 * persistent state of EntityComposites.
 */
public interface EntityStore
{
    /**
     * Create new EntityState for a given identity.
     * <p/>
     * This should only create the EntityState
     * and not insert it into any database, since
     * that should occur during the {@link #prepare}
     * call.
     *
     * @param compositeDescriptor
     * @param identity            the identity of the entity @throws EntityStoreException
     */
    EntityState newEntityState( CompositeDescriptor compositeDescriptor, QualifiedIdentity identity )
        throws EntityStoreException;

    /**
     * Get the EntityState for a given identity
     * and composite type.
     *
     * @param compositeDescriptor
     * @param identity            @return
     * @throws EntityStoreException
     */
    EntityState getEntityState( CompositeDescriptor compositeDescriptor, QualifiedIdentity identity )
        throws EntityStoreException;

    /**
     * This method is called by {@link org.qi4j.entity.UnitOfWork#complete()}.
     * The implementation of this method should take the state and send any changes
     * to the underlying datastore. The method returns a StateCommitter that the unit of work
     * will invoke once all EntityStore's have been prepared.
     *
     * @param newStates
     * @param loadedStates
     * @param removedStates
     * @param module
     * @return an implementation of StateCommitter
     * @throws EntityStoreException if the state could not be sent to the datastore
     */
    StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates, Module module )
        throws EntityStoreException;

//    EntityIterator iterator();
}
