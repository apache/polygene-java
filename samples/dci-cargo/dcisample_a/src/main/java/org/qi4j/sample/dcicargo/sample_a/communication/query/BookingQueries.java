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
package org.qi4j.sample.dcicargo.sample_a.communication.query;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.model.IModel;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.sample.dcicargo.sample_a.context.support.FoundNoRoutesException;
import org.qi4j.sample.dcicargo.sample_a.context.support.RoutingService;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.Cargo;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.itinerary.Itinerary;
import org.qi4j.sample.dcicargo.sample_a.infrastructure.model.JSONModel;

/**
 * Booking queries
 *
 * This is in a Qi4j composite since we can then conveniently get the routing service injected.
 * We could choose to implement all query classes like this too.
 *
 * Used by the communication layer only. Can change according to ui needs.
 */
@Mixins( BookingQueries.Mixin.class )
public interface BookingQueries
    extends TransientComposite
{
    List<IModel<Itinerary>> routeCandidates( String trackingIdString )
        throws FoundNoRoutesException;

    abstract class Mixin
        implements BookingQueries
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Service
        RoutingService routingService;

        public List<IModel<Itinerary>> routeCandidates( final String trackingIdString )
            throws FoundNoRoutesException
        {
            Cargo cargo = uowf.currentUnitOfWork().get( Cargo.class, trackingIdString );
            List<Itinerary> routes = routingService.fetchRoutesForSpecification( cargo.routeSpecification().get() );

            List<IModel<Itinerary>> modelList = new ArrayList<IModel<Itinerary>>();
            for( Itinerary itinerary : routes )
            {
                modelList.add( JSONModel.of( itinerary ) );
            }

            return modelList;
        }
    }
}
