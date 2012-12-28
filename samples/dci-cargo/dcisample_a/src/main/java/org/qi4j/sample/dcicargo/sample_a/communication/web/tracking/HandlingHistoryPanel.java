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
package org.qi4j.sample.dcicargo.sample_a.communication.web.tracking;

import java.util.Date;
import java.util.List;
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
import org.qi4j.sample.dcicargo.sample_a.communication.query.TrackingQueries;
import org.qi4j.sample.dcicargo.sample_a.communication.query.dto.CargoDTO;
import org.qi4j.sample.dcicargo.sample_a.communication.query.dto.HandlingEventDTO;
import org.qi4j.sample.dcicargo.sample_a.infrastructure.wicket.color.ErrorColor;

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
                if( event.voyage().get() != null )
                {
                    map.put( "voyage", event.voyage().get().voyageNumber().get().number().get() );
                }
                IModel text = new StringResourceModel( "handlingEvent.${type}", this, new Model<ValueMap>( map ) );
                item.add( new Label( "event", text )
                              .add( new ErrorColor( isLast && isMisdirected ) )
                              .setEscapeModelStrings( false ) );
            }
        } );
    }
}

