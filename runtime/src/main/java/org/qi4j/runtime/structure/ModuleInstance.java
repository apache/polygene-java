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
import java.util.Iterator;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.EntitySessionFactory;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.entity.EntitySessionFactoryImpl;
import org.qi4j.spi.service.ServiceProvider;

/**
 * TODO
 */
public final class ModuleInstance
{
    private static final Class<? extends Composite> DUMMY = Dummy.class;

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

        Map<Class, Class<? extends Composite>> mapping = createMapping( moduleContext );
        compositeBuilderFactory = new ModuleCompositeBuilderFactory( this, mapping );
        objectBuilderFactory = new ModuleObjectBuilderFactory( this );
        entitySessionFactory = new EntitySessionFactoryImpl( compositeBuilderFactory, null, null );
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

    private Map<Class, Class<? extends Composite>> createMapping( ModuleContext moduleContext )
    {
        Map<Class, Class<? extends Composite>> mapping = new HashMap<Class, Class<? extends Composite>>();
        Map<Class<? extends Composite>, CompositeContext> composites = moduleContext.getCompositeContexts();
        for( Class<? extends Composite> compositeType : composites.keySet() )
        {
            mapComposite( compositeType, mapping );
        }
        cleanupDummies( mapping );
        return mapping;
    }

    private void mapComposite( Class<? extends Composite> compositeType, Map<Class, Class<? extends Composite>> mapping )
    {
        for( Class type : compositeType.getInterfaces() )
        {
            mapType( type, compositeType, mapping );
        }
    }

    private void mapType( Class type, Class<? extends Composite> compositeType, Map<Class, Class<? extends Composite>> mapping )
    {
        if( mapping.containsKey( type ) )
        {
            mapping.put( type, DUMMY );
        }
        else
        {
            mapping.put( type, compositeType );
        }

        for( Class subtype : type.getInterfaces() )
        {
            mapType( subtype, compositeType, mapping );
        }
    }

    private void cleanupDummies( Map<Class, Class<? extends Composite>> mapping )
    {
        Iterator<Class<? extends Composite>> it = mapping.values().iterator();
        while( it.hasNext() )
        {
            Class<? extends Composite> isDummy = it.next();
            if( isDummy == DUMMY )
            {
                it.remove();
            }
        }
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

    /**
     * Interface to mark unusable pojo types.
     */
    private static interface Dummy extends Composite
    {
    }
}
