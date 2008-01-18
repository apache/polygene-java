/*
 * Copyright (c) 2007-2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007-2008, Niclas Hedhman. All Rights Reserved.
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

import java.util.HashMap;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.EntitySessionFactory;
import org.qi4j.runtime.context.ContextCompositeInstance;
import org.qi4j.runtime.entity.EntitySessionFactoryImpl;
import org.qi4j.spi.service.ServiceProvider;
import org.qi4j.spi.service.ServiceProviderException;

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
    private HashMap<Class<? extends Composite>, ContextCompositeInstance> contextCompositeInstances;

    public ModuleInstance( ModuleContext moduleContext, Map<Class<? extends Composite>, ModuleInstance> moduleInstances, Map<Class, ModuleInstance> moduleForPublicObjects )
    {
        this.moduleForPublicObjects = moduleForPublicObjects;
        this.moduleForPublicComposites = moduleInstances;
        this.moduleContext = moduleContext;

        compositeBuilderFactory = new ModuleCompositeBuilderFactory( this );
        objectBuilderFactory = new ModuleObjectBuilderFactory( this );
        entitySessionFactory = new EntitySessionFactoryImpl( this );
        injectServiceProvidersIntoObjectBuilders( moduleContext );
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

    public ModuleInstance getModuleForComposite( Class<? extends Composite> compositeType )
    {
        ModuleInstance realInstance = getModuleForPublicComposite( compositeType );
        if( realInstance == null )
        {
            realInstance = this;
        }
        return realInstance;
    }

    public ModuleInstance getModuleForObject( Class objectType )
    {
        ModuleInstance realInstance = getModuleForPublicObject( objectType );
        if( realInstance == null )
        {
            realInstance = this;
        }
        return realInstance;
    }

    /**
     * This method is used by the CompositeContext only, and should typically not be used for anything.
     *
     * @param compositeType The type of ContextComposite in this Module to be retrieved.
     * @return The CompositeInstance of the ContextComposite of the given compositeType.
     */
    public ContextCompositeInstance getContextCompositeInstance( Class<? extends Composite> compositeType )
    {
        return contextCompositeInstances.get( compositeType );
    }

    public <T> ServiceRef<T> getService( Class<T> serviceType )
        throws ServiceProviderException
    {
        ServiceProvider provider = moduleContext.getModuleBinding().getModuleResolution().getServiceProvider( serviceType );

        T instance = provider.getService( serviceType );
        return new ServiceRef<T>( instance, provider );
    }

    private void injectServiceProvidersIntoObjectBuilders( ModuleContext moduleContext )
    {
        // Inject service providers
        Map<Class, ServiceProvider> providers = moduleContext.getModuleBinding().getModuleResolution().getModuleModel().getServiceProviders();
        for( ServiceProvider serviceProvider : providers.values() )
        {
            Class serviceProviderType = serviceProvider.getClass();
            ObjectBuilder builder = objectBuilderFactory.newObjectBuilder( serviceProviderType );
            builder.inject( serviceProvider );
        }
    }
}
