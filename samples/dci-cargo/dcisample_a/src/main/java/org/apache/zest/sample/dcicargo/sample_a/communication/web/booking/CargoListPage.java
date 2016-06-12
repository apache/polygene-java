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
package org.apache.zest.sample.dcicargo.sample_a.communication.web.booking;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.Session;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.zest.sample.dcicargo.sample_a.communication.query.CommonQueries;
import org.apache.zest.sample.dcicargo.sample_a.communication.query.dto.CargoDTO;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.cargo.RouteSpecification;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.delivery.Delivery;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.delivery.RoutingStatus;
import org.apache.zest.sample.dcicargo.sample_a.data.shipping.handling.HandlingEventType;
import org.apache.zest.sample.dcicargo.sample_a.infrastructure.wicket.color.ErrorColor;
import org.apache.zest.sample.dcicargo.sample_a.infrastructure.wicket.link.LinkPanel;
import org.apache.zest.sample.dcicargo.sample_a.infrastructure.wicket.prevnext.PrevNext;

import static java.util.Date.from;

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
        ArrayList<String> ids = new ArrayList<>();
        for( CargoDTO cargo : cargoList.getObject() )
        {
            ids.add( cargo.trackingId().get().id().get() );
        }
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

                RouteSpecification routeSpecification = cargo.routeSpecification().get();
                item.add( new Label( "destination", routeSpecification.destination().get().getCode() ) );

                LocalDateTime deadlineTime = routeSpecification.arrivalDeadline().get().atStartOfDay().plusDays( 1 );
                item.add( new Label( "deadline", new Model<>( from( deadlineTime.toInstant( ZoneOffset.UTC ) ) ) ) );

                item.add( new Label( "routingStatus", routingStatus.toString() ).add( new ErrorColor( routingStatus == RoutingStatus.MISROUTED ) ) );

                Boolean inCustoms = delivery.lastHandlingEvent().get() != null
                                    && delivery.lastHandlingEvent()
                                           .get()
                                           .handlingEventType()
                                           .get() == HandlingEventType.CUSTOMS;
                String customsLabel = delivery.transportStatus().get().name() + ( inCustoms ? " (CUSTOMS)" : "" );
                item.add( new Label( "transportStatus", customsLabel ) );

                IModel directed = new Model<>( delivery.isMisdirected().get() ? "Misdirected" : "On track" );
                item.add( new Label( "deliveryStatus", directed ).add( new ErrorColor( delivery.isMisdirected()
                                                                                           .get() ) ) );
            }
        } );
    }
}