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
package org.qi4j.samples.dddsample.domain.model.carrier.assembly;

import org.qi4j.api.association.Association;
import org.qi4j.api.property.Property;
import org.qi4j.samples.dddsample.domain.model.location.Location;

import java.util.Date;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
interface CarrierMovementState
{
    Association<Location> from();

    Association<Location> to();

    Property<Date> departureTime();

    Property<Date> arrivalTime();
}
