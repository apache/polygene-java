package com.marcgrue.dcisample_b.bootstrap;

import com.marcgrue.dcisample_b.communication.web.booking.*;
import com.marcgrue.dcisample_b.communication.web.handling.IncidentLoggingApplicationMockupPage;
import com.marcgrue.dcisample_b.communication.web.tracking.TrackCargoPage;
import com.marcgrue.dcisample_b.infrastructure.WicketQi4jApplication;
import com.marcgrue.dcisample_b.infrastructure.wicket.tabs.TabsPanel;
import org.apache.wicket.ConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.devutils.stateless.StatelessChecker;

import java.util.Date;

/**
 * DCI Sample application instance
 *
 * A Wicket application backed by Qi4j.
 */
public class DCISampleApplication_b
      extends WicketQi4jApplication
{
    public void wicketInit()
    {
        // Tabs and SEO urls
        mountPages();

        // Show/hide Ajax debugging
        getDebugSettings().setDevelopmentUtilitiesEnabled( true );

        // Check that components are stateless when required
        getComponentPostOnBeforeRenderListeners().add( new StatelessChecker() );

        // Show/hide wicket tags in html code
        getMarkupSettings().setStripWicketTags( true );

        // Default date format (we don't care for now about the hour of the day)
        ( (ConverterLocator) getConverterLocator() ).set( Date.class, new PatternDateConverter( "yyyy-MM-dd", true ) );
    }

    private void mountPages()
    {
        TabsPanel.registerTab( this, CargoListPage.class, "booking", "Booking and Routing" );
        TabsPanel.registerTab( this, TrackCargoPage.class, "tracking", "Tracking" );
        TabsPanel.registerTab( this, IncidentLoggingApplicationMockupPage.class, "handling", "Handling" );

        mountPage( "/booking", CargoListPage.class );
        mountPage( "/booking/book-new-cargo", BookNewCargoPage.class );
        mountPage( "/booking/cargo", CargoDetailsPage.class );
        mountPage( "/booking/change-destination", ChangeDestinationPage.class );
        mountPage( "/booking/route-cargo", RouteCargoPage.class );
        mountPage( "/booking/re-route-cargo", ReRouteCargoPage.class );

        mountPage( "/tracking", TrackCargoPage.class );

        mountPage( "/register-handling-event", IncidentLoggingApplicationMockupPage.class );
    }

    public Class<? extends Page> getHomePage()
    {
        return CargoListPage.class;
    }
}