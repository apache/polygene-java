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

package org.apache.zest.spi.entitystore;

import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.spi.entity.EntityState;

/**
 * EntityStore UnitOfWork.
 */
public interface EntityStoreUnitOfWork
{
    String identity();

    long currentTime();

    /**
     * Create new EntityState for a given identity.
     * <p>
     * This should only create the EntityState and not insert it into any database, since that should occur during
     * the {@link EntityStoreUnitOfWork#applyChanges()} call.
     * </p>
     * @param anIdentity       the identity of the entity
     * @param entityDescriptor entity descriptor
     *
     * @return The new entity state.
     *
     * @throws EntityStoreException Thrown if creational fails.
     */
    EntityState newEntityState( EntityReference anIdentity, EntityDescriptor entityDescriptor )
        throws EntityStoreException;

    /**
     * Get the EntityState for a given identity. Throws {@link EntityNotFoundException}
     * if the entity with given {@code anIdentity} is not found.
     *
     *
     * @param module
     * @param anIdentity The entity identity. This argument must not be {@code null}.
     *
     * @return Entity state given the composite descriptor and identity.
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
