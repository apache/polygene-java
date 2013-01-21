/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.spi.entitystore;

import java.util.LinkedList;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityState;

/**
 * Default EntityStore UnitOfWork.
 */
public final class DefaultEntityStoreUnitOfWork
    implements EntityStoreUnitOfWork
{
    private EntityStoreSPI entityStoreSPI;
    private String identity;
    private Module module;
    private LinkedList<EntityState> states = new LinkedList<EntityState>();
    private Usecase usecase;
    private long currentTime;

    public DefaultEntityStoreUnitOfWork( EntityStoreSPI entityStoreSPI,
                                         String identity,
                                         Module module,
                                         Usecase usecase,
                                         long currentTime
    )
    {
        this.entityStoreSPI = entityStoreSPI;
        this.identity = identity;
        this.module = module;
        this.usecase = usecase;
        this.currentTime = currentTime;
    }

    @Override
    public String identity()
    {
        return identity;
    }

    public Module module()
    {
        return module;
    }

    @Override
    public long currentTime()
    {
        return currentTime;
    }

    public Usecase usecase()
    {
        return usecase;
    }

    // EntityStore

    @Override
    public EntityState newEntityState( EntityReference anIdentity, EntityDescriptor descriptor )
        throws EntityStoreException
    {
        EntityState state = entityStoreSPI.newEntityState( this, anIdentity, descriptor );
        states.add( state );
        return state;
    }

    @Override
    public EntityState entityStateOf( EntityReference anIdentity )
        throws EntityStoreException, EntityNotFoundException
    {
        EntityState entityState = entityStoreSPI.entityStateOf( this, anIdentity );
        states.add( entityState );
        return entityState;
    }

    @Override
    public StateCommitter applyChanges()
        throws EntityStoreException
    {
        return entityStoreSPI.applyChanges( this, states );
    }

    @Override
    public void discard()
    {
    }
}
