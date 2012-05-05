package com.marcgrue.dcisample_b.communication.web.booking;

import com.marcgrue.dcisample_b.communication.query.BookingQueries;
import com.marcgrue.dcisample_b.context.interaction.booking.specification.DeriveUpdatedRouteSpecification;
import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Re-route page
 *
 * Presents a list of possible routes a cargo can take that the user can choose from.
 *
 * Each route candidate is presented by a {@link RoutePanel}.
 */
public class ReRouteCargoPage extends BookingBasePage
{
    private FeedbackPanel feedback = new FeedbackPanel( "feedback" );

    public ReRouteCargoPage( PageParameters parameters )
    {
        final String trackingIdString = parameters.get( 0 ).toString();

        add( new Label( "trackingId", trackingIdString ) );

        add( new FeedbackPanel( "feedback" ) );

        try
        {
            // Temporary routeSpec to get correct route candidates
            RouteSpecification tempRouteSpec = new DeriveUpdatedRouteSpecification( trackingIdString ).getRouteSpec();

            add( new ListView<IModel<Itinerary>>( "routes", query( BookingQueries.class ).routeCandidates( tempRouteSpec ) )
            {
                @Override
                protected void populateItem( ListItem<IModel<Itinerary>> item )
                {
                    item.add( new RoutePanel( "route", trackingIdString, item.getModelObject(), item.getIndex() + 1 ) );
                }
            } );
        }
        catch (Exception e)
        {
            logger.info( e.getMessage() );
            error( e.getMessage() );
            add( new WebMarkupContainer( "routes" ).add( new Label( "route" ) ) );
        }
    }
}