/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

import java.lang.reflect.Proxy;
import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.service.Availability;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.util.Classes;
import org.qi4j.runtime.composite.TransientInstance;
import org.qi4j.runtime.composite.TransientStateInstance;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * JAVADOC
 */
public class ServiceInstance
    extends TransientInstance
    implements Activation
{
    public static TransientInstance serviceInstanceOf( ServiceComposite composite )
    {
        return (TransientInstance) Proxy.getInvocationHandler( composite );
    }

    private boolean implementsServiceAvailable;
    private boolean hasEnabledConfiguration;

    public ServiceInstance( ServiceModel compositeModel,
                            ModuleInstance moduleInstance,
                            Object[] mixins,
                            TransientStateInstance state
    )
    {
        super( compositeModel, moduleInstance, mixins, state );

        implementsServiceAvailable =
            Classes.assignableTypeSpecification( Availability.class ).satisfiedBy( descriptor() );
        hasEnabledConfiguration = compositeModel.configurationType() != null
                                  && Enabled.class.isAssignableFrom( compositeModel.configurationType() );
    }

    @Override
    public void activate()
        throws ActivationException
    {
        // NOOP
    }

    @Override
    public void passivate()
        throws PassivationException
    {
        // NOOP
    }

    public boolean isAvailable()
    {
        // Check Enabled in configuration first
        if( hasEnabledConfiguration && !( (Configuration<Enabled>) proxy() ).get().enabled().get() )
        {
            return false;
        }

        // Ask service if it's available
        return !implementsServiceAvailable || ( (Availability) proxy() ).isAvailable();
    }
}
