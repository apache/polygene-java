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
package org.qi4j.samples.dddsample.domain.model.cargo;

import org.qi4j.samples.dddsample.domain.model.Entity;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;

/**
 * A Cargo. This is the central class in the domain model,
 * and it is the root of the Cargo-Itinerary-Leg-DeliveryHistory aggregate.
 * <p/>
 * A cargo is identified by a unique tracking id, and it always has an origin
 * and a destination. The life cycle of a cargo begins with the booking procedure,
 * when the tracking id is assigned. During a (short) period of time, between booking
 * and initial routing, the cargo has no itinerary.
 * <p/>
 * The booking clerk requests a list of possible routes, matching a route specification,
 * and assigns the cargo to one route. An itinerary listing the legs of the route
 * is attached to the cargo.
 * <p/>
 * A cargo can be re-routed during transport, on demand of the customer, in which case
 * the destination is changed and a new route is requested. The old itinerary,
 * being a value object, is discarded and a new one is attached.
 * <p/>
 * It may also happen that a cargo is accidentally misrouted, which should notify the proper
 * personnel and also trigger a re-routing procedure.
 * <p/>
 * The life cycle of a cargo ends when the cargo is claimed by the customer.
 * <p/>
 * The cargo aggregate, and the entre domain model, is built to solve the problem
 * of booking and tracking cargo. All important business rules for determining whether
 * or not a cargo is misrouted, what the current status of the cargo is (on board carrier,
 * in port etc), are captured in this aggregate.
 *
 * @since 0.5
 */
public interface Cargo
    extends Entity<Cargo>
{
    /**
     * The tracking id is the identity of this entity, and is unique.
     *
     * @return Tracking id.
     */
    TrackingId trackingId();

    /**
     * @return Origin location.
     */
    Location origin();

    /**
     * @param newDestination the new destination. May not be null.
     */
    void changeDestination( Location newDestination );

    /**
     * @return Destination of the cargo.
     */
    Location destination();

    /**
     * @return The itinerary. Never null.
     */
    Itinerary itinerary();

    /**
     * @return Last known location of the cargo, or {@link LocationRepository#unknownLocation()}
     *         if the delivery history is empty.
     */
    Location lastKnownLocation();

    /**
     * @return True if the cargo has arrived at its final destination.
     */
    boolean hasArrived();

    /**
     * Attach a new itinerary to this cargo.
     *
     * @param itinerary an itinerary. May not be null.
     */
    void attachItinerary( Itinerary itinerary );

    /**
     * Detaches the current itinerary from the cargo.
     */
    void detachItinerary();

    /**
     * Check if cargo is misdirected.
     * <p/>
     * <ul>
     * <li>A cargo is misdirected if it is in a location that's not in the itinerary.
     * <li>A cargo with no itinerary can not be misdirected.
     * <li>A cargo that has received no handling events can not be misdirected.
     * </ul>
     *
     * @return <code>true</code> if the cargo has been misdirected,
     */
    boolean isMisdirected();

    /**
     * Does not take into account the possibility of the cargo having been
     * (errouneously) loaded onto another carrier after it has been unloaded
     * at the final destination.
     *
     * @return True if the cargo has been unloaded at the final destination.
     */
    boolean isUnloadedAtDestination();
}
