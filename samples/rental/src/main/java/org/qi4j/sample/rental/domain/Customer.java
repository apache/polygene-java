/*
 * Copyright 2009 Niclas Hedhman.
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

package org.qi4j.sample.rental.domain;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;

@Mixins( Customer.CustomerMixin.class )
public interface Customer
    extends EntityComposite
{
    Property<String> name();

    Property<Address> address();

    Query<Booking> currentBookings();

    Query<Booking> pastBookings();

    abstract class CustomerMixin
        implements Customer
    {
        public Query<Booking> currentBookings()
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Query<Booking> pastBookings()
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
