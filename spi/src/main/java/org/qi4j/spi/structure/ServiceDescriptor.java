/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Edward Yakop. All Rights Reserved.
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

import java.util.Map;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
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
    private Visibility visibility;
    private Map<Class, Object> serviceInfos;

    /**
     * Construct an instance of {@code ServiceDescriptor}.
     *
     * @param aServiceType     The service type. This argument must not be {@code null}.
     * @param aServiceProvider The service provider. This argument must not be {@code null}.
     * @param aVisibility      The visibility of the service. This argument must not be {@code null}.
     * @param serviceInfoz     The service infos. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if one or some or all arguments are {@code null}.
     * @since 0.1.0
     */
    public ServiceDescriptor(
        Class aServiceType, Class<? extends ServiceInstanceProvider> aServiceProvider,
        Visibility aVisibility, Map<Class, Object> serviceInfoz )
        throws IllegalArgumentException
    {
        validateNotNull( "aServiceType", aServiceType );
        validateNotNull( "aServiceProvider", aServiceProvider );
        validateNotNull( "aVisibility", aVisibility );
        validateNotNull( "serviceInfoz", serviceInfoz );

        serviceType = aServiceType;
        serviceProvider = aServiceProvider;
        visibility = aVisibility;
        serviceInfos = serviceInfoz;
    }

    /**
     * Returns the service type. Must not return {@code null}.
     *
     * @return The service type.
     * @since 0.1.0
     */
    public final Class getServiceType()
    {
        return serviceType;
    }

    /**
     * Returns the service instance provider class. Must not return {@code null}.
     *
     * @return The service instance provider class.
     * @since 0.1.0
     */
    public final Class<? extends ServiceInstanceProvider> getServiceProvider()
    {
        return serviceProvider;
    }

    /**
     * Returns the visibility of the service. Must not return {@code null}.
     *
     * @return The visiblity of the service.
     * @since 0.1.0
     */
    public final Visibility getVisibility()
    {
        return visibility;
    }

    /**
     * Returns the service informations.
     *
     * @return The service informations.
     * @since 0.1.0
     */
    public final Map<Class, Object> getServiceInfos()
    {
        return serviceInfos;
    }
}
