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
package org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception;

import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;

/**
 * CargoMisroutedException
 *
 * This would have to set off notifying the cargo owner and request a re-route.
 */
public class CargoMisroutedException extends InspectionException
{
    private RouteSpecification routeSpecification;
    private Itinerary itinerary;

    public CargoMisroutedException( HandlingEvent handlingEvent,
                                    RouteSpecification routeSpecification,
                                    Itinerary itinerary
    )
    {
        super( handlingEvent );
        this.routeSpecification = routeSpecification;
        this.itinerary = itinerary;
    }

    @Override
    public String getMessage()
    {
        return "\nCargo is MISROUTED! Route specification is not satisfied with itinerary:\n"
               + routeSpecification.print() + itinerary.print()
               + "MOCKUP REQUEST TO CARGO OWNER: Please re-route misrouted cargo '" + id + "' (now in " + city + ").";
    }
}