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
package org.qi4j.sample.dcicargo.sample_a.communication.web.booking;

import java.util.Date;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.qi4j.sample.dcicargo.sample_a.communication.query.CommonQueries;
import org.qi4j.sample.dcicargo.sample_a.communication.query.dto.CargoDTO;
import org.qi4j.sample.dcicargo.sample_a.communication.web.tracking.HandlingHistoryPanel;
import org.qi4j.sample.dcicargo.sample_a.communication.web.tracking.NextHandlingEventPanel;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.delivery.Delivery;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.delivery.RoutingStatus;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.itinerary.Leg;
import org.qi4j.sample.dcicargo.sample_a.infrastructure.wicket.color.CorrectColor;
import org.qi4j.sample.dcicargo.sample_a.infrastructure.wicket.color.ErrorColor;
import org.qi4j.sample.dcicargo.sample_a.infrastructure.wicket.link.LinkPanel;
import org.qi4j.sample.dcicargo.sample_a.infrastructure.wicket.prevnext.PrevNext;

/**
 * Cargo details - an overview of all data available about a cargo.
 */
@StatelessComponent
public class CargoDetailsPage extends BookingBasePage
{
    public CargoDetailsPage( PageParameters parameters )
    {
        this( parameters.get( 0 ).toString() );
    }

    public CargoDetailsPage( String trackingId )
    {
        super( new PageParameters().set( 0, trackingId ) );

        IModel<CargoDTO> cargoModel = new CommonQueries().cargo( trackingId );
        CargoDTO cargo = cargoModel.getObject();
        Delivery delivery = cargo.delivery().get();
        RouteSpecification routeSpecification = cargo.routeSpecification().get();
        final RoutingStatus routingStatus = delivery.routingStatus().get();
        Boolean isMisrouted = routingStatus == RoutingStatus.MISROUTED;

        add( new PrevNext( "prevNext", CargoDetailsPage.class, trackingId ) );

        add( new Label( "trackingId", trackingId ) );
        add( new Label( "origin", cargo.origin().get().getString() ) );
        add( new Label( "destination", routeSpecification.destination()
            .get()
            .getString() ).add( new CorrectColor( isMisrouted ) ) );
        add( new Label( "deadline", Model.of( routeSpecification.arrivalDeadline().get() ) ) );
        add( new Label( "routingStatus", routingStatus.toString() ).add( new ErrorColor( isMisrouted ) ) );
        add( new LinkPanel( "changeDestination", ChangeDestinationPage.class, trackingId, "Change destination" ) );

        if( routingStatus == RoutingStatus.NOT_ROUTED )
        {
            add( new LinkPanel( "routingAction", RouteCargoPage.class, trackingId, "Route" ) );
            add( new Label( "delivery" ) );
            add( new Label( "itinerary" ) );
        }
        else if( routingStatus == RoutingStatus.ROUTED )
        {
            add( new LinkPanel( "routingAction", RouteCargoPage.class, trackingId, "Re-route" ) );
            add( new DeliveryFragment( delivery ) );
            add( new ItineraryFragment( cargoModel, routingStatus ) );
        }
        else if( routingStatus == RoutingStatus.MISROUTED )
        {
            add( new LinkPanel( "routingAction", RouteCargoPage.class, trackingId, "Re-route" ) );
            add( new DeliveryFragment( delivery ) );
            add( new ItineraryFragment( cargoModel, routingStatus ) );
        }
        else
        {
            throw new RuntimeException( "Unknown routing status." );
        }

        if( delivery.lastHandlingEvent().get() == null )
        {
            add( new Label( "handlingHistoryPanel" ) );
        }
        else
        {
            add( new HandlingHistoryPanel( "handlingHistoryPanel", cargoModel, trackingId ) );
        }

        add( new NextHandlingEventPanel( "nextHandlingEventPanel", cargoModel ) );
    }
//
//    @Override
//    public boolean isVersioned()
//    {
//        return false;
//    }

    private class ItineraryFragment extends Fragment
    {
        public ItineraryFragment( final IModel<CargoDTO> cargoModel, final RoutingStatus routingStatus )
        {
            super( "itinerary", "itineraryFragment", CargoDetailsPage.this );

            IModel<List<Leg>> legListModel = new LoadableDetachableModel<List<Leg>>()
            {
                @Override
                protected List<Leg> load()
                {
                    return cargoModel.getObject().itinerary().get().legs().get();
                }
            };

            add( new ListView<Leg>( "legs", legListModel )
            {
                @Override
                protected void populateItem( ListItem<Leg> item )
                {
                    Leg leg = item.getModelObject();

                    item.add( new Label( "loadLocation", leg.loadLocation().get().getCode() ) );
                    item.add( new Label( "loadTime", new Model<Date>( leg.loadTime().get() ) ) );
                    item.add( new Label( "voyage", leg.voyage().get().voyageNumber().get().number().get() ) );

                    Boolean isMisrouted = routingStatus == RoutingStatus.MISROUTED && item.getIndex() == ( getList().size() - 1 );
                    item.add( new Label( "unloadLocation", leg.unloadLocation().get().getCode() )
                                  .add( new ErrorColor( isMisrouted ) ) );

                    item.add( new Label( "unloadTime", new Model<Date>( leg.unloadTime().get() ) ) );
                }
            } );
        }
    }

    private class DeliveryFragment extends Fragment
    {
        public DeliveryFragment( Delivery delivery )
        {
            super( "delivery", "deliveryFragment", CargoDetailsPage.this );

            add( new Label( "transportStatus", delivery.transportStatus().get().toString() ) );

            if( delivery.isMisdirected().get() )
            {
                String msg = "Cargo is misdirected. \nPlease reroute cargo.";
                add( new MultiLineLabel( "deliveryStatus", msg ).add( new AttributeModifier( "class", "errorColor" ) ) );
            }
            else
            {
                add( new Label( "deliveryStatus", "On track" ) );
            }
        }
    }
}