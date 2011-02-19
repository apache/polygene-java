package org.qi4j.samples.dddsample.spring.ui.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.qi4j.api.query.Query;
import org.qi4j.samples.dddsample.application.remoting.BookingFacade;
import org.qi4j.samples.dddsample.application.remoting.dto.CargoRoutingDTO;
import org.qi4j.samples.dddsample.application.remoting.dto.LocationDTO;
import org.qi4j.samples.dddsample.domain.model.cargo.Itinerary;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * Handles cargo booking and routing. Operates against a dedicated remoting service facade,
 * and could easily be rewritten as a thick Swing client. Completely separated from the domain layer,
 * unlike the tracking user interface.
 * <p/>
 * In order to successfully keep the domain model shielded from user interface considerations,
 * this approach is generally preferred to the one taken in the tracking controller. However,
 * there is never any one perfect solution for all situations, so we've chosen to demonstrate
 * two polarized ways to build user interfaces.
 *
 * @see se.citerus.dddsample.ui.CargoTrackingController
 */
public final class CargoAdminController
    extends MultiActionController
{
    private BookingFacade bookingFacade;

    public void setBookingServiceFacade( BookingFacade bookingFacade )
    {
        this.bookingFacade = bookingFacade;
    }

    public final Map list( HttpServletRequest request, HttpServletResponse response )
        throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        List<CargoRoutingDTO> cargoList = bookingFacade.listAllCargos();
        map.put( "cargoList", cargoList );

        return map;
    }

    public Map show( final HttpServletRequest request, final HttpServletResponse response )
        throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        String trackingId = request.getParameter( "trackingId" );
        if( trackingId != null )
        {
            CargoRoutingDTO cargo = bookingFacade.loadCargoForRouting( trackingId );
            map.put( "cargo", cargo );
        }

        return map;
    }

    public Map registrationForm( final HttpServletRequest request, final HttpServletResponse response )
        throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        List<LocationDTO> locations = bookingFacade.listShippingLocations();

        List<String> unLocodeStrings = new ArrayList<String>();
        for( LocationDTO location : locations )
        {
            unLocodeStrings.add( location.getUnLocode() );
        }

        map.put( "unlocodes", unLocodeStrings );

        return map;
    }

    public void register( final HttpServletRequest request, final HttpServletResponse response,
                          final RegistrationCommand command
    )
        throws Exception
    {

        String trackingId = bookingFacade.registerNewCargo(
            command.getOriginUnlocode(), command.getDestinationUnlocode()
        );
        response.sendRedirect( "show.html?trackingId=" + trackingId );
    }

    public Map selectItinerary( final HttpServletRequest request, final HttpServletResponse response )
        throws Exception
    {
        final Map<String, Object> map = new HashMap<String, Object>();
        final String trackingId = request.getParameter( "trackingId" );
        final Query<Itinerary> itineraryCandidates = bookingFacade.requestPossibleRoutesForCargo( trackingId );

        if( request.getParameter( "spec" ) != null )
        {
            map.put( "itineraryCandidates", itineraryCandidates );
        }

        final CargoRoutingDTO cargoDTO = bookingFacade.loadCargoForRouting( trackingId );
        map.put( "origin", cargoDTO.getOrigin() );
        map.put( "destination", cargoDTO.getFinalDestination() );
        map.put( "trackingId", trackingId );
        return map;
    }

    public void assignItinerary( final HttpServletRequest request,
                                 final HttpServletResponse response,
                                 RouteAssignmentCommand command
    )
        throws Exception
    {
/*
        final List<LegDTO> legDTOs = new ArrayList<LegDTO>( command.getLegs().size() );
        for( RouteAssignmentCommand.LegCommand leg : command.getLegs() )
        {
            legDTOs.add( new LegDTO( leg.getCarrierMovementId(), leg.getFromUnLocode(), leg.getToUnLocode() ) );
        }

        final Itinerary selectedItinerary = new ItineraryCandidateDTO( legDTOs );

        bookingFacade.assignCargoToRoute( command.getTrackingId(), selectedItinerary );
*/

        response.sendRedirect( "list.html" );
    }
}