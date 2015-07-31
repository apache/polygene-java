/*
 * Copyright (c) 2012, Paul Merlin.
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
package org.qi4j.spi.entitystore.helpers;

import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.service.ServiceReference;

/**
 * Activation for MapEntityStoreMixin.
 */
@Activators( MapEntityStoreActivation.Activator.class )
public interface MapEntityStoreActivation
{

    void activateMapEntityStore()
        throws Exception;

    /**
     * MapEntityStoreMixin Activator.
     */
    class Activator
        extends ActivatorAdapter<ServiceReference<MapEntityStoreActivation>>
    {

        @Override
        public void afterActivation( ServiceReference<MapEntityStoreActivation> activated )
            throws Exception
        {
            activated.get().activateMapEntityStore();
        }

    }

}
