/*
 * Copyright 2011 Marc Grue.
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
package org.qi4j.sample.dcicargo.sample_b.data.structure.cargo;

import org.qi4j.api.association.Association;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.Delivery;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;
import org.qi4j.sample.dcicargo.sample_b.data.structure.tracking.TrackingId;

/**
 * Cargo
 *
 * Data describing a cargo, it's planned route and current delivery status.
 *
 * {@link TrackingId}           created automatically
 * {@link Location} origin      Specified upon creation (mandatory)
 * {@link RouteSpecification}   Specified upon creation (mandatory)
 * {@link Itinerary}            Description of chosen route (optional)
 * {@link Delivery}             Snapshot of the current delivery status (automatically created by the system)
 */
public interface Cargo
{
    @Immutable
    Property<TrackingId> trackingId();

    @Immutable
    Association<Location> origin();

    Property<RouteSpecification> routeSpecification();

    @Optional
    Property<Itinerary> itinerary();

    Property<Delivery> delivery();
}