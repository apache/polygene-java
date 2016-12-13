/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.spi.entitystore;

import java.time.Instant;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.usecase.Usecase;
import org.apache.polygene.spi.entity.EntityState;

/**
 * EntityStore UnitOfWork.
 */
public interface EntityStoreUnitOfWork
{
    Identity identity();

    Instant currentTime();

    /**
     * Create new EntityState for a given reference.
     * <p>
     * This should only create the EntityState and not insert it into any database, since that should occur during
     * the {@link EntityStoreUnitOfWork#applyChanges()} call.
     * </p>
     * @param anIdentity       the reference of the entity
     * @param entityDescriptor entity descriptor
     *
     * @return The new entity state.
     *
     * @throws EntityStoreException Thrown if creational fails.
     */
    EntityState newEntityState( EntityReference anIdentity, EntityDescriptor entityDescriptor )
        throws EntityStoreException;

    /**
     * Get the EntityState for a given reference. Throws {@link EntityNotFoundException}
     * if the entity with given {@code anIdentity} is not found.
     *
     *
     * @param module Module descriptor
     * @param anIdentity The entity reference. This argument must not be {@code null}.
     *
     * @return Entity state given the composite descriptor and reference.
     *
     * @throws EntityStoreException    thrown if retrieval failed.
     * @throws EntityNotFoundException if requested entity does not exist
     */
    EntityState entityStateOf( ModuleDescriptor module, EntityReference anIdentity )
        throws EntityStoreException, EntityNotFoundException;

    String versionOf( EntityReference anIdentity ) throws EntityStoreException;

    StateCommitter applyChanges()
        throws EntityStoreException;

    void discard();

    Usecase usecase();

    ModuleDescriptor module();
}
