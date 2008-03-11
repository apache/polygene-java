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

package org.qi4j.spi.structure;

import java.io.Serializable;
import java.util.Map;
import org.qi4j.spi.service.ServiceInstanceProvider;

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
    private boolean activateOnStartup;
    private Map<Class, Serializable> serviceInfos;

    public ServiceDescriptor( Class serviceType,
                              Class<? extends ServiceInstanceProvider> serviceProvider,
                              String identity,
                              Visibility visibility,
                              boolean activateOnStartup,
                              Map<Class, Serializable> serviceInfos )
    {
        this.serviceType = serviceType;
        this.serviceProvider = serviceProvider;
        this.identity = identity;
        this.visibility = visibility;
        this.activateOnStartup = activateOnStartup;
        this.serviceInfos = serviceInfos;
    }

    public Class getServiceType()
    {
        return serviceType;
    }

    public Class<? extends ServiceInstanceProvider> getServiceProvider()
    {
        return serviceProvider;
    }

    public String getIdentity()
    {
        return identity;
    }

    public Visibility getVisibility()
    {
        return visibility;
    }

    public boolean isActivateOnStartup()
    {
        return activateOnStartup;
    }

    public Map<Class, Serializable> getServiceInfos()
    {
        return serviceInfos;
    }

    public <K extends Serializable> K getServiceInfo( Class<K> infoType )
    {
        return (K) serviceInfos.get( infoType );
    }
}
