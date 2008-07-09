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

package org.qi4j.runtime.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.qi4j.composite.Composite;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.service.ServiceInstanceFactory;
import org.qi4j.structure.Module;
import org.qi4j.structure.Visibility;
import org.qi4j.util.MetaInfo;

/**
 * TODO
 */
public final class ServiceModel
{
    private final Class<? extends Composite> type;
    private final Visibility visibility;
    private final Class<? extends ServiceInstanceFactory> serviceFactory;
    private final String identity;
    private final boolean instantiateOnStartup;
    private final MetaInfo metaInfo;

    public ServiceModel( Class<? extends Composite> compositeType,
                         Visibility visibility,
                         Class<? extends ServiceInstanceFactory> serviceFactory,
                         String identity,
                         boolean instantiateOnStartup, MetaInfo metaInfo )
    {
        type = compositeType;
        this.visibility = visibility;
        this.serviceFactory = serviceFactory;
        this.identity = identity;
        this.instantiateOnStartup = instantiateOnStartup;
        this.metaInfo = metaInfo;
    }

    public Class<? extends Composite> type()
    {
        return type;
    }

    public Visibility visibility()
    {
        return visibility;
    }

    public MetaInfo metaInfo()
    {
        return metaInfo;
    }

    public boolean isInstantiateOnStartup()
    {
        return instantiateOnStartup;
    }

    public ServiceInstance<?> newInstance( Module module )
    {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor( type, serviceFactory, identity, visibility, instantiateOnStartup, metaInfo );
        ServiceInstanceFactory instanceFactory = module.objectBuilderFactory().newObject( serviceFactory );
        Object instance = instanceFactory.newInstance( serviceDescriptor );
        return new ServiceInstance<Object>( instance, instanceFactory, serviceDescriptor );
    }

    public Class<? extends ServiceInstanceFactory> serviceFactory()
    {
        return serviceFactory;
    }

    public String identity()
    {
        return identity;
    }

    public Object newProxy( InvocationHandler serviceInvocationHandler )
    {
        if( type.isInterface() )
        {
            return Proxy.newProxyInstance( type.getClassLoader(),
                                           new Class[]{ type },
                                           serviceInvocationHandler );
        }
        else
        {
            Class[] interfaces = type.getInterfaces();
            return Proxy.newProxyInstance( type.getClassLoader(),
                                           interfaces,
                                           serviceInvocationHandler );
        }

    }

    @Override public String toString()
    {
        return type.getName() + ":" + identity;
    }
}
