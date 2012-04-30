package com.marcgrue.dcisample_a.communication.web.tracking;

import com.marcgrue.dcisample_a.communication.query.TrackingQueries;
import com.marcgrue.dcisample_a.communication.query.dto.CargoDTO;
import com.marcgrue.dcisample_a.communication.query.dto.HandlingEventDTO;
import com.marcgrue.dcisample_a.infrastructure.wicket.color.ErrorColor;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.ValueMap;

import java.util.Date;
import java.util.List;

/**
 * Handling history - a table list of handling events
 *
 * A tracking id string is passed in to retrieve all events matching that cargo.
 */
@StatelessComponent
public class HandlingHistoryPanel extends Panel
{
    public HandlingHistoryPanel( String id, final IModel<CargoDTO> cargoModel, String trackingId )
    {
        super( id );

        IModel<List<HandlingEventDTO>> handlingEventsModel = new TrackingQueries().events( trackingId );

        add( new ListView<HandlingEventDTO>( "handlingEvents", handlingEventsModel )
        {
            @Override
            protected void populateItem( ListItem<HandlingEventDTO> item )
            {
                HandlingEventDTO event = item.getModelObject();
                Boolean isLast = item.getIndex() == getList().size() - 1;
                Boolean isMisdirected = cargoModel.getObject().delivery().get().isMisdirected().get();

                // Status icon
                IModel iconName = Model.of( isLast && isMisdirected ? "cross.png" : "tick.png" );
                item.add( new WebMarkupContainer( "onTrackIcon" ).add( new AttributeAppender( "src", iconName, "" ) ) );

                // Date
                item.add( new Label( "completion", new Model<Date>( event.completionTime().get() ) ) );

                // Event description (data substitution in strings from HandlingHistoryPanel.properties)
                ValueMap map = new ValueMap();
                map.put( "type", event.handlingEventType().get().name() );
                map.put( "location", event.location().get().getString() );
                if (event.voyage().get() != null)
                    map.put( "voyage", event.voyage().get().voyageNumber().get().number().get() );
                IModel text = new StringResourceModel( "handlingEvent.${type}", this, new Model<ValueMap>( map ) );
                item.add( new Label( "event", text )
                                .add( new ErrorColor( isLast && isMisdirected ) )
                                .setEscapeModelStrings( false ) );
            }
        } );
    }
}

