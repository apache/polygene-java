/*
 * Copyright 2007, 2008 Niclas Hedhman.
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
package org.qi4j.tutorials.cargo.step2;

import org.qi4j.api.concern.ConcernOf;

public class OverbookingConcern
    extends ConcernOf<ShippingService>
    implements ShippingService
{
    @Override
    public int makeBooking( Cargo cargo, Voyage voyage )
    {
        double cargoSize = cargo.size().get();
        double aleadyBookedSize = voyage.bookedCargoSize().get();
        double totalCapacity = voyage.capacity().get();

        if( cargoSize + aleadyBookedSize > totalCapacity )
        {
            // Not allowed.
            return -1;
        }
        return next.makeBooking( cargo, voyage );
    }
}
