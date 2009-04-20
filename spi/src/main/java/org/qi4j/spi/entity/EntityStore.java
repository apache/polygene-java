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

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;
import org.qi4j.spi.unitofwork.event.UnitOfWorkEvent;

/**
 * Interface that must be implemented by store for persistent state of EntityComposites.
 */
public interface EntityStore
{
    EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, MetaInfo unitOfWorkMetaInfo );

    /**
     * Create new EntityState for a given identity.
     * <p/>
     * This should only create the EntityState and not insert it into any database, since that should occur during
     * the {@link #prepare} call.
     *
     * @param anIdentity the identity of the entity
     * @return The new entity state.
     * @throws EntityStoreException Thrown if creational fails.
    EntityState newEntityState( EntityReference anIdentity )
    throws EntityStoreException;
     */

    /**
     * Get the EntityState for a given identity. Throws {@link EntityNotFoundException}
     * if the entity with given {@code anIdentity} is not found.
     *
     * @param anIdentity The entity identity. This argument must not be {@code null}.
     * @return Entity state given the composite descriptor and identity.
     * @throws EntityStoreException    thrown if retrieval failed.
     * @throws EntityNotFoundException thrown if wanted entity does not exist
    EntityState getEntityState( EntityReference anIdentity )
    throws EntityStoreException, EntityNotFoundException;
     */

    /**
     * This method is called by {@link org.qi4j.api.unitofwork.UnitOfWork#complete()}.
     * The implementation of this method should take the state and send any changes
     * to the underlying datastore. The method returns a StateCommitter that the unit of work
     * will invoke once all EntityStore's have been prepared.
     *
     * To ensure that the committed state is consistent it should (if able) compare the version of the loaded/removed states
     * and compare them to the versions currently in the store. If the store has different versions that means that
     * a concurrent modification was done, and the store should throw ConcurrentEntityModificationException so that
     * the client can do a refresh() of those entities and try again.
     *
     * @return an implementation of StateCommitter
     * @throws EntityStoreException if the state could not be sent to the datastore
     * @throws ConcurrentEntityStateModificationException
     *                              if the prepared state has changed in the store
     *                              StateCommitter prepare( Iterable<EntityState> newStates,
     *                              Iterable<EntityState> updatedStates,
     *                              Iterable<EntityReference> removedStates )
     *                              throws EntityStoreException, ConcurrentEntityStateModificationException;
     */

    StateCommitter apply( String unitOfWorkIdentity, Iterable<UnitOfWorkEvent> events, Usecase usecase, MetaInfo metaInfo )
        throws EntityStoreException;

    EntityStoreUnitOfWork visitEntityStates( EntityStateVisitor visitor );

    interface EntityStateVisitor
    {
        void visitEntityState( EntityState entityState );
    }
}
