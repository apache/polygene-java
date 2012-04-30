package com.marcgrue.dcisample_a.communication.web.booking;

import com.marcgrue.dcisample_a.communication.query.CommonQueries;
import com.marcgrue.dcisample_a.communication.query.dto.CargoDTO;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEventType;
import com.marcgrue.dcisample_a.data.shipping.delivery.Delivery;
import com.marcgrue.dcisample_a.data.shipping.delivery.RoutingStatus;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEventType;
import com.marcgrue.dcisample_a.infrastructure.wicket.color.ErrorColor;
import com.marcgrue.dcisample_a.infrastructure.wicket.link.LinkPanel;
import com.marcgrue.dcisample_a.infrastructure.wicket.prevnext.PrevNext;
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
                CargoDTO cargo = item.getModelObject();
                String trackingId = cargo.trackingId().get().id().get();
                Delivery delivery = cargo.delivery().get();
                RoutingStatus routingStatus = cargo.delivery().get().routingStatus().get();

                item.add( new LinkPanel( "trackingId", CargoDetailsPage.class, trackingId ) );

                item.add( new Label( "origin", cargo.origin().get().getCode() ) );

                item.add( new Label( "destination", cargo.routeSpecification().get().destination().get().getCode() ) );

                item.add( new Label( "deadline", new Model<Date>( cargo.routeSpecification().get().arrivalDeadline().get() ) ) );

                item.add( new Label( "routingStatus", routingStatus.toString() ).add( new ErrorColor( routingStatus == RoutingStatus.MISROUTED ) ) );

                Boolean inCustoms = delivery.lastHandlingEvent().get() != null
                      && delivery.lastHandlingEvent().get().handlingEventType().get() == HandlingEventType.CUSTOMS;
                String customsLabel = delivery.transportStatus().get().name() + ( inCustoms ? " (CUSTOMS)" : "" );
                item.add( new Label( "transportStatus", customsLabel ) );

                IModel directed = new Model<String>( delivery.isMisdirected().get() ? "Misdirected" : "On track" );
                item.add( new Label( "deliveryStatus", directed ).add( new ErrorColor( delivery.isMisdirected().get() ) ) );
            }
        } );
    }
}