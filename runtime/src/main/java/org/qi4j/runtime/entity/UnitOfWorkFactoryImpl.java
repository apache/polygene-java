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
package org.qi4j.runtime.entity;

import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.IdentityGenerator;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ServiceMap;
import org.qi4j.spi.entity.EntityStore;

public final class UnitOfWorkFactoryImpl
    implements UnitOfWorkFactory
{
    private ModuleInstance moduleInstance;
    private ModuleStateServices services;

    public UnitOfWorkFactoryImpl( ModuleInstance moduleInstance )
    {
        this.moduleInstance = moduleInstance;
        services = new ModuleStateServices( moduleInstance );
    }

    public UnitOfWork newUnitOfWork()
    {
        return new UnitOfWorkInstance( moduleInstance, services );
    }

    private class ModuleStateServices
        implements StateServices
    {
        ServiceMap<EntityStore> entityStores;
        ServiceMap<IdentityGenerator> identityGenerators;

        public ModuleStateServices( ModuleInstance moduleInstance )
        {
            entityStores = new ServiceMap<EntityStore>( moduleInstance, EntityStore.class );
            identityGenerators = new ServiceMap<IdentityGenerator>( moduleInstance, IdentityGenerator.class );
        }

        public EntityStore getEntityStore( Class<? extends EntityComposite> compositeType )
        {
            return entityStores.getService( compositeType );
        }

        public IdentityGenerator getIdentityGenerator( Class<? extends EntityComposite> compositeType )
        {
            return identityGenerators.getService( compositeType );
        }
    }
}
