package com.marcgrue.dcisample_b.communication.web.booking;

import com.marcgrue.dcisample_b.communication.query.CommonQueries;
import com.marcgrue.dcisample_b.communication.query.dto.CargoDTO;
import com.marcgrue.dcisample_b.communication.web.tracking.HandlingHistoryPanel;
import com.marcgrue.dcisample_b.communication.web.tracking.NextHandlingEventPanel;
import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.delivery.Delivery;
import com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus;
import com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus;
import com.marcgrue.dcisample_b.data.structure.itinerary.Leg;
import com.marcgrue.dcisample_b.infrastructure.wicket.color.CorrectColor;
import com.marcgrue.dcisample_b.infrastructure.wicket.color.ErrorColor;
import com.marcgrue.dcisample_b.infrastructure.wicket.link.LinkPanel;
import com.marcgrue.dcisample_b.infrastructure.wicket.prevnext.PrevNext;
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

import java.util.Date;
import java.util.List;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.MISROUTED;
import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.NOT_ROUTED;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.CLAIMED;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.UNKNOWN;

/**
 * Cargo details - an overview of all data available about a cargo.
 *
 * Wicket-comment:
 * The StatelessComponent annotation verifies that this component is not saved in session (remains stateless).
 */
@StatelessComponent
public class CargoDetailsPage extends BookingBasePage
{
    // Standard constructor for Prev/Next links...
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
        TransportStatus transportStatus = delivery.transportStatus().get();
        RouteSpecification routeSpecification = cargo.routeSpecification().get();
        final RoutingStatus routingStatus = delivery.routingStatus().get();
        boolean isMisrouted = routingStatus == MISROUTED;
        boolean isReRouted = !cargo.origin().get().getCode().equals( routeSpecification.origin().get().getCode() );

        add( new PrevNext( "prevNext", CargoDetailsPage.class, trackingId ) );

        add( new Label( "trackingId", trackingId ) );

        // Show both cargo origin and new route spec origin when re-routed during transport
        if (isReRouted)
        {
            Fragment originFragment = new Fragment( "origin", "re-routed-originFragment", this );
            originFragment.add( new Label( "cargoOrigin", cargo.origin().get().getString() ) );
            originFragment.add( new Label( "routeOrigin", routeSpecification.origin().get().getString() ).add( new CorrectColor( isMisrouted ) ) );
            add( originFragment );
        }
        else
        {
            Fragment originFragment = new Fragment( "origin", "originFragment", this );
            originFragment.add( new Label( "cargoOrigin", cargo.origin().get().getString() ) );
            add( originFragment );
        }

        add( new Label( "departure", Model.of( routeSpecification.earliestDeparture().get() ) ) );
        add( new Label( "destination", routeSpecification.destination().get().getString() ).add( new CorrectColor( isMisrouted ) ) );
        add( new Label( "deadline", Model.of( routeSpecification.arrivalDeadline().get() ) ) );
        add( new Label( "routingStatus", routingStatus.toString() ).add( new ErrorColor( isMisrouted ) ) );
        add( new LinkPanel( "changeDestination", ChangeDestinationPage.class, trackingId, "Change destination" ) );

        if (transportStatus.equals( CLAIMED ))
        {
            // Can't re-route claimed cargo
            add( new Label( "routingAction" ) );
            add( new DeliveryFragment( delivery ) );
            add( new ItineraryFragment( cargoModel, routingStatus ) );
        }
        else if (routingStatus.equals( NOT_ROUTED ))
        {
            add( new LinkPanel( "routingAction", RouteCargoPage.class, trackingId, "Route" ) );
            add( new Label( "delivery" ) );
            add( new Label( "itinerary" ) );
        }
        else
        {
            add( new LinkPanel( "routingAction", ReRouteCargoPage.class, trackingId, "Re-route" ) );
            add( new DeliveryFragment( delivery ) );
            add( new ItineraryFragment( cargoModel, routingStatus ) );
        }

        if (delivery.lastHandlingEvent().get() == null)
            add( new Label( "handlingHistoryPanel" ) );
        else
            add( new HandlingHistoryPanel( "handlingHistoryPanel", cargoModel, trackingId ) );

        add( new NextHandlingEventPanel( "nextHandlingEventPanel", cargoModel ) );
    }

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

                    Boolean isMisrouted = routingStatus == MISROUTED && item.getIndex() == ( getList().size() - 1 );
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

            if (delivery.transportStatus().get().equals( UNKNOWN ))
            {
                String msg = "UNKNOWN \n(Could be hi-jacked)";
                add( new MultiLineLabel( "transportStatus", msg ).add( new AttributeModifier( "class", "errorColor" ) ) );
            }
            else
            {
                add( new Label( "transportStatus", delivery.transportStatus().get().toString() ) );
            }


            if (delivery.isMisdirected().get())
            {
                String msg = "Cargo is misdirected \nPlease reroute cargo";
                if (delivery.transportStatus().get().equals( CLAIMED ))
                    msg = "Cargo is misdirected \n(Can't re-route claimed cargo)";

                add( new MultiLineLabel( "deliveryStatus", msg ).add( new AttributeModifier( "class", "errorColor" ) ) );
            }
            else
            {
                add( new Label( "deliveryStatus", "On track" ) );
            }
        }
    }
}