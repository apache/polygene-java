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
package org.qi4j.sample.dcicargo.sample_b.data.structure.delivery;

import java.util.Date;
import org.qi4j.api.association.Association;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.Voyage;

/**
 * NextHandlingEvent
 *
 * (former "HandlingActivity" / "ExpectedHandlingEvent")
 *
 * This represents our assumptions about the next handling event for a cargo.
 *
 * Since a cargo could have been loaded onto an unexpected carrier it seems better
 * not to call the next unload an _expected_ handling event. It's expected to
 * the carrier voyage schedule, but unexpected to the itinerary.
 *
 * A time for the expected event was added.
 */
public interface NextHandlingEvent
    extends ValueComposite
{
    Property<HandlingEventType> handlingEventType();

    Association<Location> location();

    @Optional
    Property<Date> time();

    @Optional
    Association<Voyage> voyage();
}
