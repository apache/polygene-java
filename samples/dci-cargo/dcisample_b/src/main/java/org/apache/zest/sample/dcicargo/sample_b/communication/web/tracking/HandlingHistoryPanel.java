/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.sample.dcicargo.sample_b.communication.web.tracking;

import java.time.ZoneOffset;
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
import org.apache.zest.sample.dcicargo.sample_b.communication.query.TrackingQueries;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.cargo.Cargo;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.wicket.color.ErrorColor;

import static java.util.Date.from;

/**
 * Handling history
 *
 * Shows a list of handling events for a cargo.
 *
 * A tracking id string is passed in to retrieve all events matching that cargo.
 */
@StatelessComponent
public class HandlingHistoryPanel extends Panel
{
    public HandlingHistoryPanel( String id, final IModel<Cargo> cargoModel, String trackingId )
    {
        super( id );

        IModel<List<HandlingEvent>> handlingEventsModel = new TrackingQueries().events( trackingId );

        add( new ListView<HandlingEvent>( "handlingEvents", handlingEventsModel )
        {
            @Override
            protected void populateItem( ListItem<HandlingEvent> item )
            {
                HandlingEvent event = item.getModelObject();
                Boolean isLast = item.getIndex() == getList().size() - 1;
                Boolean isMisdirected = cargoModel.getObject().delivery().get().isMisdirected().get();

                // Status icon
                IModel iconName = Model.of( isLast && isMisdirected ? "cross.png" : "tick.png" );
                item.add( new WebMarkupContainer( "onTrackIcon" ).add( new AttributeAppender( "src", iconName, "" ) ) );

                // Date
                item.add( new Label( "completion", new Model<>(
                    from( event.completionDate().get().atStartOfDay().toInstant( ZoneOffset.UTC ) ) )
                ));

                // Event description (data substitution in strings from HandlingHistoryPanel.properties)
                ValueMap map = new ValueMap();
                map.put( "type", event.handlingEventType().get().name() );
                map.put( "location", event.location().get().getString() );
                if( event.voyage().get() != null )
                {
                    map.put( "voyage", event.voyage().get().voyageNumber().get().number().get() );
                }
                IModel text = new StringResourceModel( "handlingEvent.${type}", this, new Model<>( map ) );
                item.add( new Label( "event", text )
                              .add( new ErrorColor( isLast && isMisdirected ) )
                              .setEscapeModelStrings( false ) );
            }
        } );
    }
}

