package com.marcgrue.dcisample_a.communication.web.tracking;

import com.marcgrue.dcisample_a.communication.query.dto.CargoDTO;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEvent;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEventType;
import com.marcgrue.dcisample_a.data.shipping.location.Location;
import com.marcgrue.dcisample_a.data.shipping.delivery.ExpectedHandlingEvent;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.ValueMap;

import java.text.SimpleDateFormat;

/**
 * Next expected handling event
 *
 * Quite some logic to render 1 line of information!
 */
@StatelessComponent
public class NextHandlingEventPanel extends Panel
{
    public NextHandlingEventPanel( String id, IModel<CargoDTO> cargoModel )
    {
        super( id );

        ValueMap map = new ValueMap();
        Label label = new Label( "text", new StringResourceModel(
              "expectedEvent.${expectedEvent}", this, new Model<ValueMap>( map ) ) );
        add( label );

        CargoDTO cargo = cargoModel.getObject();
        Location destination = cargo.routeSpecification().get().destination().get();

        if (cargo.itinerary().get() == null)
        {
            map.put( "expectedEvent", "ROUTE" );
            return;
        }

        HandlingEvent previousEvent = cargo.delivery().get().lastHandlingEvent().get();
        if (previousEvent == null)
        {
            map.put( "expectedEvent", "RECEIVE" );
            map.put( "location", cargo.routeSpecification().get().origin().get().getString() );
            return;
        }

        Location lastLocation = previousEvent.location().get();
        if (previousEvent.handlingEventType().get() == HandlingEventType.CLAIM && lastLocation == destination)
        {
            map.put( "expectedEvent", "END_OF_CYCLE" );
            map.put( "location", destination.getString() );
            label.add( new AttributeModifier( "class", "correctColor" ) );
            return;
        }

        ExpectedHandlingEvent nextEvent = cargo.delivery().get().nextExpectedHandlingEvent().get();
        if (nextEvent == null)
        {
            map.put( "expectedEvent", "UNKNOWN" );
            label.add( new AttributeModifier( "class", "errorColor" ) );
            return;
        }

        map.put( "expectedEvent", nextEvent.handlingEventType().get().name() );
        map.put( "location", nextEvent.location().get().getString() );

        if (nextEvent.time() != null)
            map.put( "time", new SimpleDateFormat( "yyyy-MM-dd" ).format( nextEvent.time().get() ) );

        if (nextEvent.voyage().get() != null)
            map.put( "voyage", nextEvent.voyage().get().voyageNumber().get().number().get() );
    }
}
