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

import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWorkException;
import org.qi4j.spi.entitystore.EntityStore;

/**
 * JAVADOC
 */
public class EntitiesInstance
    implements Activatable
{
    private final EntitiesModel entities;
    private final ModuleInstance moduleInstance;
    //lazy assigned on accessor
    private EntityStore store;
    //lazy assigned on accessor
    private IdentityGenerator generator;

    public EntitiesInstance( EntitiesModel entities, ModuleInstance moduleInstance )
    {
        this.entities = entities;
        this.moduleInstance = moduleInstance;
    }

    public void activate()
        throws Exception
    {
    }

    public void passivate()
        throws Exception
    {
    }

    public EntitiesModel model()
    {
        return entities;
    }

    // todo DCL??

    public EntityStore entityStore()
    {
        synchronized( this )
        {
            if( store == null )
            {
                ServiceReference<EntityStore> service = moduleInstance.serviceFinder().findService( EntityStore.class );
                if( service == null )
                {
                    throw new UnitOfWorkException( "No EntityStore service available in module " + moduleInstance.name() );
                }
                store = service.get();
            }
        }
        return store;
    }

    // todo DCL??

    public IdentityGenerator identityGenerator()
    {
        synchronized( this )
        {
            if( generator == null )
            {
                ServiceReference<IdentityGenerator> service = moduleInstance.serviceFinder()
                    .findService( IdentityGenerator.class );
                if( service == null )
                {
                    return null;
                }
                generator = service.get();
            }
        }
        return generator;
    }
}
