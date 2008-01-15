/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.EntitySessionFactory;
import org.qi4j.runtime.entity.EntitySessionFactoryImpl;
import org.qi4j.spi.service.ServiceProvider;

/**
 * TODO
 */
public final class ModuleInstance
{
    private ModuleContext moduleContext;

    private Map<Class<? extends Composite>, ModuleInstance> moduleForPublicComposites;
    private Map<Class, ModuleInstance> moduleForPublicObjects;

    private CompositeBuilderFactory compositeBuilderFactory;
    private ObjectBuilderFactory objectBuilderFactory;
    private EntitySessionFactory entitySessionFactory;

    public ModuleInstance( ModuleContext moduleContext, Map<Class<? extends Composite>, ModuleInstance> moduleInstances, Map<Class, ModuleInstance> moduleForPublicObjects )
    {
        this.moduleForPublicObjects = moduleForPublicObjects;
        this.moduleForPublicComposites = moduleInstances;
        this.moduleContext = moduleContext;

        compositeBuilderFactory = new ModuleCompositeBuilderFactory( this );
        objectBuilderFactory = new ModuleObjectBuilderFactory( this );
        entitySessionFactory = new EntitySessionFactoryImpl( compositeBuilderFactory, null, null );

        // Inject service providers
        Map<Class, ServiceProvider> providers = moduleContext.getModuleBinding().getModuleResolution().getModuleModel().getServiceProviders();
        for( ServiceProvider serviceProvider : providers.values() )
        {
            Class serviceProviderType = serviceProvider.getClass();
            objectBuilderFactory.newObjectBuilder( serviceProviderType ).inject( serviceProvider );
        }
    }

    public ModuleContext getModuleContext()
    {
        return moduleContext;
    }

    public CompositeBuilderFactory getCompositeBuilderFactory()
    {
        return compositeBuilderFactory;
    }

    public ObjectBuilderFactory getObjectBuilderFactory()
    {
        return objectBuilderFactory;
    }

    public EntitySessionFactory getEntitySessionFactory()
    {
        return entitySessionFactory;
    }

    public ModuleInstance getModuleForPublicComposite( Class<? extends Composite> compositeType )
    {
        return moduleForPublicComposites.get( compositeType );
    }

    public ModuleInstance getModuleForPublicObject( Class objectType )
    {
        return moduleForPublicObjects.get( objectType );
    }
}
