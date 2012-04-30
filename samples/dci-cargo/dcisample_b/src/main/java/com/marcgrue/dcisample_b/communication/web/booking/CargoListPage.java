package com.marcgrue.dcisample_b.communication.web.booking;

import com.marcgrue.dcisample_b.communication.query.CommonQueries;
import com.marcgrue.dcisample_b.communication.query.dto.CargoDTO;
import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.delivery.Delivery;
import com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus;
import com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import com.marcgrue.dcisample_b.infrastructure.wicket.color.ErrorColor;
import com.marcgrue.dcisample_b.infrastructure.wicket.link.LinkPanel;
import com.marcgrue.dcisample_b.infrastructure.wicket.prevnext.PrevNext;
import org.apache.wicket.Session;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.UNKNOWN;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.CUSTOMS;

/**
 * List of Cargos
 */
@StatelessComponent
public class CargoListPage extends BookingBasePage
{
    public CargoListPage()
    {
        IModel<List<CargoDTO>> cargoList = new CommonQueries().cargoList();

        // Save current trackingIds in session (for prev/next buttons on details page)
        ArrayList<String> ids = new ArrayList<String>();
        for (CargoDTO cargo : cargoList.getObject())
            ids.add( cargo.trackingId().get().id().get() );
        PrevNext.registerIds( Session.get(), ids );

        add( new ListView<CargoDTO>( "list", cargoList )
        {
            @Override
            protected void populateItem( ListItem<CargoDTO> item )
            {
                // Cargo
                CargoDTO cargo = item.getModelObject();
                String trackingId = cargo.trackingId().get().id().get();
                String origin = cargo.origin().get().getCode();

                // Route specification
                RouteSpecification routeSpec = cargo.routeSpecification().get();
                String destination = routeSpec.destination().get().getCode();
                Date deadline = routeSpec.arrivalDeadline().get();

                // Routing status
                Delivery delivery = cargo.delivery().get();
                RoutingStatus routingStatus = cargo.delivery().get().routingStatus().get();
                boolean isMisrouted = routingStatus == RoutingStatus.MISROUTED;

                // Transport status
                TransportStatus transportStatus = delivery.transportStatus().get();
                boolean isHiJacked = transportStatus.equals( UNKNOWN );

                // Delivery status
                boolean isMisdirected = delivery.isMisdirected().get();
                HandlingEvent event = delivery.lastHandlingEvent().get();
                boolean inCustoms = event != null && event.handlingEventType().get() == CUSTOMS;


                // Output

                item.add( new LinkPanel( "trackingId", CargoDetailsPage.class, trackingId ) );

                item.add( new Label( "origin", origin ) );

                item.add( new Label( "destination", destination ) );

                item.add( new Label( "deadline", new Model<Date>( deadline ) ) );

                item.add( new Label( "routingStatus", routingStatus.toString() ).add( new ErrorColor( isMisrouted ) ) );

                String customsLabel = transportStatus.name() + ( inCustoms ? " (CUSTOMS)" : "" );
                item.add( new Label( "transportStatus", customsLabel ).add( new ErrorColor( isHiJacked ) ) );

                String directed = isMisdirected ? "Misdirected" : "On track";
                item.add( new Label( "deliveryStatus", directed ).add( new ErrorColor( isMisdirected ) ) );
            }
        } );
    }
}