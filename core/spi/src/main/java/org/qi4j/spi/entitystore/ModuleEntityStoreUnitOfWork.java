/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.spi.entitystore;

import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.module.ModuleSpi;

public class ModuleEntityStoreUnitOfWork
    implements EntityStoreUnitOfWork
{
    private final ModuleSpi module;
    private final EntityStoreUnitOfWork underlying;

    public ModuleEntityStoreUnitOfWork( ModuleSpi module, EntityStoreUnitOfWork underlying )
    {
        this.module = module;
        this.underlying = underlying;
    }

    public ModuleSpi module()
    {
        return module;
    }

    @Override
    public String identity()
    {
        return underlying.identity();
    }

    @Override
    public long currentTime()
    {
        return underlying.currentTime();
    }

    @Override
    public EntityState newEntityState( ModuleSpi module, EntityReference reference, EntityDescriptor descriptor )
        throws EntityStoreException
    {
        return underlying.newEntityState( module, reference, descriptor );
    }

    @Override
    public EntityState entityStateOf( ModuleSpi module, EntityReference reference )
        throws EntityStoreException, EntityNotFoundException
    {
        return underlying.entityStateOf( module, reference );
    }

    @Override
    public StateCommitter applyChanges()
        throws EntityStoreException
    {
        return underlying.applyChanges();
    }

    @Override
    public void discard()
    {
        underlying.discard();
    }

    public Usecase usecase()
    {
        return underlying.usecase();
    }
}
