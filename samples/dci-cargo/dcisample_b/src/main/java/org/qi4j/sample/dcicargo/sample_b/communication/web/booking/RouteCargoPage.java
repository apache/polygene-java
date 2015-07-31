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
package org.qi4j.sample.dcicargo.sample_b.communication.web.booking;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.qi4j.sample.dcicargo.sample_b.communication.query.BookingQueries;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;

/**
 * Re-route page - presents a list of possible routes a cargo can take that the user can choose from.
 *
 * Each route candidate is presented by a {@link RoutePanel}.
 */
public class RouteCargoPage extends BookingBasePage
{
    private FeedbackPanel feedback = new FeedbackPanel( "feedback" );

    public RouteCargoPage( PageParameters parameters )
    {
        final String trackingIdString = parameters.get( 0 ).toString();

        add( new Label( "trackingId", trackingIdString ) );

        add( new FeedbackPanel( "feedback" ) );

        try
        {
            add( new ListView<IModel<Itinerary>>( "routes", query( BookingQueries.class ).routeCandidates( trackingIdString ) )
            {
                @Override
                protected void populateItem( ListItem<IModel<Itinerary>> item )
                {
                    item.add( new RoutePanel( "route", trackingIdString, item.getModelObject(), item.getIndex() + 1 ) );
                }
            } );
        }
        catch( Exception e )
        {
            logger.info( e.getMessage() );
            error( "Unexpected error happened." );
            add( new WebMarkupContainer( "routes" ).add( new Label( "route" ) ) );
        }
    }
}