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

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.Availability;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.runtime.composite.TransientInstance;
import org.qi4j.runtime.composite.TransientStateInstance;
import org.qi4j.runtime.structure.ModuleInstance;

import java.lang.reflect.Proxy;

/**
 * JAVADOC
 */
public class ServiceInstance
    extends TransientInstance
    implements Activatable
{
    public static TransientInstance getCompositeInstance( ServiceComposite composite )
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

        implementsServiceAvailable = Availability.class.isAssignableFrom( type() );
        hasEnabledConfiguration = compositeModel.configurationType() != null && Enabled.class.isAssignableFrom( compositeModel
                                                                                                                    .configurationType() );
    }

    public void activate()
        throws Exception
    {
        ( (ServiceModel) compositeModel ).activate( mixins );
    }

    public void passivate()
        throws Exception
    {
        ( (ServiceModel) compositeModel ).passivate( mixins );
    }

    public boolean isAvailable()
    {
        // Check Enabled in configuration first
        if( hasEnabledConfiguration && !( (Configuration<Enabled>) proxy() ).configuration().enabled().get() )
        {
            return false;
        }

        // Ask service if it's available
        return !implementsServiceAvailable || ( (Availability) proxy() ).isAvailable();
    }
}
