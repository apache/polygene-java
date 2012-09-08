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
package org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage;

import org.qi4j.api.property.Property;

/**
 * A voyage is a ship, train, flight etc carrying a cargo from one location
 * to another. A {@link Schedule} describes the route it takes.
 *
 * A cargo can be loaded onto part of, or the whole voyage.
 *
 * All properties are mandatory and immutable.
 */
public interface Voyage
{
    Property<VoyageNumber> voyageNumber();

    Property<Schedule> schedule();
}
