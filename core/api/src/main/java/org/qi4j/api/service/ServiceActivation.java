/*
 * Copyright 2012 Paul Merlin.
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

import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;

/**
 * Convenience interface for simple Service Activation.
 *
 * Let your ServiceComposite extends ServiceActivation and implement it in one of its Mixins.
 * A corresponding Activator is automatically registered.
 */
@Activators( ServiceActivation.ServiceActivator.class )
public interface ServiceActivation
{

    /**
     * Called after ServiceComposite Activation.
     */
    void activateService()
            throws Exception;

    /**
     * Called before ServiceComposite Passivation.
     */
    void passivateService()
            throws Exception;

    /**
     * Service Activator.
     */
    class ServiceActivator
            extends ActivatorAdapter<ServiceReference<ServiceActivation>>
    {

        @Override
        public void afterActivation( ServiceReference<ServiceActivation> activated )
                throws Exception
        {
            activated.get().activateService();
        }

        @Override
        public void beforePassivation( ServiceReference<ServiceActivation> passivating )
                throws Exception
        {
            passivating.get().passivateService();
        }

    }

}
