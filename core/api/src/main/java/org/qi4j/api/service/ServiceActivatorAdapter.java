/*
 * Copyright 2014 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.api.service;

import org.qi4j.api.activation.Activator;

/**
 * Adapter for Service Activator.
 *
 * @param <ServiceType> Type of the service.
 */
public class ServiceActivatorAdapter<ServiceType>
    implements Activator<ServiceReference<ServiceType>>
{
    /**
     * Called before Service activation.
     * @param activating Activating Service
     */
    @Override
    public void beforeActivation( ServiceReference<ServiceType> activating )
        throws Exception
    {
    }

    /**
     * Called after Service activation.
     * @param activated Activated Service
     */
    @Override
    public void afterActivation( ServiceReference<ServiceType> activated )
        throws Exception
    {
    }

    /**
     * Called before Service passivation.
     * @param passivating Passivating Service
     */
    @Override
    public void beforePassivation( ServiceReference<ServiceType> passivating )
        throws Exception
    {
    }

    /**
     * Called after Service passivation.
     * @param passivated Passivated Service
     */
    @Override
    public void afterPassivation( ServiceReference<ServiceType> passivated )
        throws Exception
    {
    }
}
