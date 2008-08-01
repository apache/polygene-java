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

import org.qi4j.structure.Visibility;
import org.qi4j.util.MetaInfo;

/**
 * {@code ServiceDescriptor} provides meta informations of a service.
 *
 */
public final class ServiceDescriptor
{
    private final Class<?> serviceType;
    private final Class<? extends ServiceInstanceFactory> serviceFactory;
    private final String identity;
    private final Visibility visibility;
    private final boolean instantiateOnStartup;
    private final MetaInfo metaInfo;

    public ServiceDescriptor( Class serviceType,
                              Class<? extends ServiceInstanceFactory> serviceFactory,
                              String identity,
                              Visibility visibility,
                              boolean instantiateOnStartup,
                              MetaInfo metaInfo )
    {
        this.serviceType = serviceType;
        this.serviceFactory = serviceFactory;
        this.identity = identity;
        this.visibility = visibility;
        this.instantiateOnStartup = instantiateOnStartup;
        this.metaInfo = metaInfo;
    }

    public Class<?> type()
    {
        return serviceType;
    }

    public Class<? extends ServiceInstanceFactory> serviceFactory()
    {
        return serviceFactory;
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

    public <K> K metaInfo( Class<K> infoType )
    {
        return metaInfo.get( infoType );
    }

    @Override public String toString()
    {
        return identity + "(" + serviceType.getName() + ", visible in " + visibility + ")";
    }
}
