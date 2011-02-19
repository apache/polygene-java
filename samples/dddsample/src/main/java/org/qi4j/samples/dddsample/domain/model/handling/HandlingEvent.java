/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.samples.dddsample.domain.model.handling;

import java.util.Date;
import org.qi4j.samples.dddsample.domain.model.DomainEvent;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovement;
import org.qi4j.samples.dddsample.domain.model.location.Location;

/**
 * A HandlingEvent is used to register the event when, for instance,
 * a cargo is unloaded from a carrier at a some loacation at a given time.
 * <p/>
 * The HandlingEvent's are sent from different Incident Logging Applications
 * some time after the event occured and contain information about the
 * {@link TrackingId}, {@link Location}, timestamp of the completion of the event,
 * and possibly, if applicable a {@link CarrierMovement}.
 * <p/>
 * This class is the only member, and consequently the root, of the HandlingEvent aggregate.
 * <p/>
 * HandlingEvent's could contain information about a {@link CarrierMovement} and if so,
 * the event type must be either {@link Type#LOAD} or {@link Type#UNLOAD}.
 * <p/>
 * All other events must be of {@link Type#RECEIVE}, {@link Type#CLAIM} or {@link Type#CUSTOMS}.
 */

public interface HandlingEvent
    extends DomainEvent<HandlingEvent>
{
    Type eventType();

    CarrierMovement carrierMovement();

    Date completionTime();

    Date registrationTime();

    Location location();

    Cargo cargo();

    /**
     * Handling event type. Either requires or prohibits a carrier movement
     * association, it's never optional.
     */
    public enum Type
    {
        LOAD( true ),
        UNLOAD( true ),
        RECEIVE( false ),
        CLAIM( false ),
        CUSTOMS( false );

        private boolean carrierMovementRequired;

        /**
         * Private enum constructor.
         *
         * @param carrierMovementRequired whether or not a carrier movement is associated with this event type
         */
        private Type( final boolean carrierMovementRequired )
        {
            this.carrierMovementRequired = carrierMovementRequired;
        }

        /**
         * @return True if a carrier movement association is required for this event type.
         */
        public boolean requiresCarrierMovement()
        {
            return carrierMovementRequired;
        }

        /**
         * @return True if a carrier movement association is prohibited for this event type.
         */
        public boolean prohibitsCarrierMovement()
        {
            return !requiresCarrierMovement();
        }
    }
}
