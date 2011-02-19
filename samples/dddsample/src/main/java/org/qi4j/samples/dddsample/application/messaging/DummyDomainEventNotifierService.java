/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.samples.dddsample.application.messaging;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent;
import org.qi4j.samples.dddsample.domain.service.DomainEventNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAVADOC: Do JMS handling
 *
 * @author edward.yakop@gmail.com
 */
@Mixins( DummyDomainEventNotifierService.DummyDomainEventNotifierServiceMixin.class )
interface DummyDomainEventNotifierService
    extends DomainEventNotifier, ServiceComposite
{
    public class DummyDomainEventNotifierServiceMixin
        implements DomainEventNotifier
    {
        private static final Logger LOGGER = LoggerFactory.getLogger( DomainEventNotifier.class );

        public void cargoWasHandled( HandlingEvent event )
        {
            HandlingEvent.Type type = event.eventType();
            String trackingId = event.cargo().trackingId().idString();

            LOGGER.info( "Cargo was handled " + trackingId + " event " + type );
        }
    }
}
