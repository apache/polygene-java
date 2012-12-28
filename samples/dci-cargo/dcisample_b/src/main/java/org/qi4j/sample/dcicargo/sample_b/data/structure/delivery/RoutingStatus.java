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

/**
 * RoutingStatus
 *
 * A routing status indicates whether an itinerary is assigned to
 * a cargo and satisfying the route specification.
 */
public enum RoutingStatus
{
    NOT_ROUTED,    // Itinerary has not been assigned to cargo yet
    ROUTED,        // Itinerary is assigned to cargo
    MISROUTED;     // RouteSpecification is not satisfied by Itinerary
}
