/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.structure;

import org.qi4j.entity.EntityBuilder;
import org.qi4j.runtime.entity.EntityBuilderInstance;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * TODO
 */
public class EntitiesInstance
{
    private EntitiesModel entities;
    private ModuleInstance moduleInstance;
    private EntityStore store;

    public EntitiesInstance( EntitiesModel entities, ModuleInstance moduleInstance )
    {
        this.entities = entities;
        this.moduleInstance = moduleInstance;
    }

    public EntitiesModel model()
    {
        return entities;
    }

    public <T> EntityBuilder<T> newEntityBuilder( Class<T> mixinType, UnitOfWorkInstance uow )
    {
        return new EntityBuilderInstance<T>( moduleInstance, entities.getEntityModelFor( mixinType ), uow, store );
    }

    public EntityInstance loadEntityInstance( String identity, EntityModel entityModel, UnitOfWorkInstance uow )
    {
        if( store == null )
        {
            store = moduleInstance.serviceFinder().findService( EntityStore.class ).get();
        }

        QualifiedIdentity qid = entityModel.newQualifiedIdentity( identity );

        EntityState state = store.getEntityState( entityModel, qid );

        return entityModel.loadInstance( uow, store, qid, moduleInstance, state );
    }

    public EntityInstance getEntityInstance( String identity, EntityModel entityModel, UnitOfWorkInstance unitOfWorkInstance )
    {
        if( store == null )
        {
            store = moduleInstance.serviceFinder().findService( EntityStore.class ).get();
        }

        QualifiedIdentity qid = entityModel.newQualifiedIdentity( identity );

        return entityModel.getInstance( unitOfWorkInstance, store, qid, moduleInstance );
    }
}
