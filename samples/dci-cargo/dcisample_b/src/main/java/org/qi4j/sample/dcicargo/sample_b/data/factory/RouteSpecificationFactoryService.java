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
package org.qi4j.sample.dcicargo.sample_b.data.factory;

import java.util.Date;
import org.joda.time.DateMidnight;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.sample.dcicargo.sample_b.data.factory.exception.CannotCreateRouteSpecificationException;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;

/**
 * Route Specification factory
 *
 * Enforces 3 invariants:
 * - no identical origin and destination
 * - deadline in the future
 * - earliest departure before deadline
 *
 * Validation of Locations is considered out of this scope.
 */
@Mixins( RouteSpecificationFactoryService.Mixin.class )
public interface RouteSpecificationFactoryService
    extends ServiceComposite
{
    RouteSpecification build( Location origin, Location destination, Date earliestDeparture, Date deadline )
        throws CannotCreateRouteSpecificationException;

    abstract class Mixin
        implements RouteSpecificationFactoryService
    {
        @Structure
        ValueBuilderFactory vbf;

        public RouteSpecification build( Location origin, Location destination, Date earliestDeparture, Date deadline )
            throws CannotCreateRouteSpecificationException
        {
            if( origin == destination )
            {
                throw new CannotCreateRouteSpecificationException( "Origin location can't be same as destination location." );
            }

            Date endOfToday = new DateMidnight().plusDays( 1 ).toDate();
            if( deadline.before( endOfToday ) )
            {
                throw new CannotCreateRouteSpecificationException( "Arrival deadline is in the past or Today." +
                                                                   "\nDeadline           " + deadline +
                                                                   "\nToday (midnight)   " + endOfToday );
            }

            if( deadline.before( earliestDeparture ) )
            {
                throw new CannotCreateRouteSpecificationException( "Deadline can't be before departure:" +
                                                                   "\nDeparture   " + earliestDeparture +
                                                                   "\nDeadline    " + deadline );
            }

            ValueBuilder<RouteSpecification> routeSpec = vbf.newValueBuilder( RouteSpecification.class );
            routeSpec.prototype().origin().set( origin );
            routeSpec.prototype().destination().set( destination );
            routeSpec.prototype().earliestDeparture().set( earliestDeparture );
            routeSpec.prototype().arrivalDeadline().set( deadline );
            return routeSpec.newInstance();
        }
    }
}
