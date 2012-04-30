package com.marcgrue.dcisample_a.communication.web.booking;

import com.marcgrue.dcisample_a.communication.query.BookingQueries;
import com.marcgrue.dcisample_a.context.support.FoundNoRoutesException;
import com.marcgrue.dcisample_a.data.shipping.itinerary.Itinerary;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

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
        final String trackingId = parameters.get( 0 ).toString();

        add( new Label( "trackingId", trackingId ) );

        add( new FeedbackPanel( "feedback" ) );

//        List<IModel<Itinerary>> routes = null;
        try
        {
            add( new ListView<IModel<Itinerary>>( "routes", query( BookingQueries.class ).routeCandidates( trackingId ) )
            {
                @Override
                protected void populateItem( ListItem<IModel<Itinerary>> item )
                {
                    item.add( new RoutePanel( "route", trackingId, item.getModelObject(), item.getIndex() + 1 ) );
                }
            } );
        }
        catch (FoundNoRoutesException e)
        {
            error( e.getMessage() );

            add( new WebMarkupContainer( "routes" ).add( new Label("route" ) ) );
        }
    }
}