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

package org.qi4j.service;

import java.io.Serializable;
import java.util.Map;
import org.qi4j.structure.Visibility;

/**
 * {@code ServiceDescriptor} provides meta informations of a service.
 *
 * @author Rickard Öberg
 * @since 0.1.0
 */
public final class ServiceDescriptor
{
    private Class serviceType;
    private Class<? extends ServiceInstanceProvider> serviceProvider;
    private String identity;
    private Visibility visibility;
    private boolean instantiateOnStartup;
    private Map<Class, Serializable> serviceAttributes;

    public ServiceDescriptor( Class serviceType,
                              Class<? extends ServiceInstanceProvider> serviceProvider,
                              String identity,
                              Visibility visibility,
                              boolean instantiateOnStartup,
                              Map<Class, Serializable> serviceAttributes )
    {
        this.serviceType = serviceType;
        this.serviceProvider = serviceProvider;
        this.identity = identity;
        this.visibility = visibility;
        this.instantiateOnStartup = instantiateOnStartup;
        this.serviceAttributes = serviceAttributes;
    }

    public Class serviceType()
    {
        return serviceType;
    }

    public Class<? extends ServiceInstanceProvider> serviceProvider()
    {
        return serviceProvider;
    }

    public String identity()
    {
        return identity;
    }

    public Visibility visibility()
    {
        return visibility;
    }

    public boolean isInstantiateOnStartup()
    {
        return instantiateOnStartup;
    }

    public Iterable<Class> serviceAttributeTypes()
    {
        return serviceAttributes.keySet();
    }

    public <K extends Serializable> K serviceAttribute( Class<K> infoType )
    {
        return infoType.cast( serviceAttributes.get( infoType ) );
    }

    @Override public String toString()
    {
        return identity + "(" + serviceType.getName() + ", visible in " + visibility + ")";
    }
}
