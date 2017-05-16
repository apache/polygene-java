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
import java.util.HashMap;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.usecase.Usecase;
import org.apache.polygene.spi.entity.EntityState;

/**
 * Default EntityStore UnitOfWork.
 */
public final class DefaultEntityStoreUnitOfWork
    implements EntityStoreUnitOfWork
{
    private final ModuleDescriptor module;
    private EntityStoreSPI entityStoreSPI;
    private Identity identity;
    private HashMap<EntityReference, EntityState> states = new HashMap<>();
    private Usecase usecase;
    private Instant currentTime;

    public DefaultEntityStoreUnitOfWork( ModuleDescriptor module,
                                         EntityStoreSPI entityStoreSPI,
                                         Identity identity,
                                         Usecase usecase,
                                         Instant currentTime
    )
    {
        this.module = module;
        this.entityStoreSPI = entityStoreSPI;
        this.identity = identity;
        this.usecase = usecase;
        this.currentTime = currentTime;
    }

    @Override
    public Identity identity()
    {
        return identity;
    }

    @Override
    public Instant currentTime()
    {
        return currentTime;
    }

    public Usecase usecase()
    {
        return usecase;
    }

    @Override
    public ModuleDescriptor module()
    {
        return module;
    }
// EntityStore

    @Override
    public EntityState newEntityState( EntityReference anIdentity, EntityDescriptor descriptor )
        throws EntityStoreException
    {
        EntityState entityState = states.get( anIdentity );
        if( entityState != null )
        {
            throw new EntityAlreadyExistsException( anIdentity );
        }
        EntityState state = entityStoreSPI.newEntityState( this, anIdentity, descriptor );
        states.put( anIdentity, state );
        return state;
    }

    @Override
    public EntityState entityStateOf( ModuleDescriptor module, EntityReference anIdentity )
        throws EntityNotFoundException
    {
        EntityState entityState = states.get( anIdentity );
        if( entityState != null )
        {
            return entityState;
        }
        entityState = entityStoreSPI.entityStateOf( this, module, anIdentity );
        states.put( anIdentity, entityState );
        return entityState;
    }

    @Override
    public String versionOf( EntityReference anIdentity )
        throws EntityNotFoundException
    {
        EntityState entityState = states.get( anIdentity );
        if( entityState != null )
        {
            return entityState.version();
        }
        return entityStoreSPI.versionOf( this, anIdentity );
    }

    @Override
    public StateCommitter applyChanges()
        throws EntityStoreException
    {
        return entityStoreSPI.applyChanges( this, states.values() );
    }

    @Override
    public void discard()
    {
    }
}
