package org.qi4j.samples.dddsample.application.remoting;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.library.constraints.annotation.Matches;
import org.qi4j.samples.dddsample.application.remoting.dto.CargoRoutingDTO;
import org.qi4j.samples.dddsample.application.remoting.dto.LocationDTO;
import org.qi4j.samples.dddsample.domain.model.cargo.Itinerary;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This facade shields the domain layer - model, services, repositories -
 * from concerns about such things as the user interface and remoting.
 * It is an application service.
 */
public interface BookingFacade
    extends Remote
{
    String registerNewCargo( @Name( "origin" ) @Matches( "[a-zA-Z]{2}[a-zA-Z2-9]{3}" ) String origin,
                             @Name( "destination" ) @UnLocodePattern String destination
    )
        throws RemoteException;

    CargoRoutingDTO loadCargoForRouting( String trackingId )
        throws RemoteException;

    void assignCargoToRoute( String trackingId, Itinerary itinerary )
        throws RemoteException;

    Query<Itinerary> requestPossibleRoutesForCargo( String trackingId )
        throws RemoteException;

    List<LocationDTO> listShippingLocations()
        throws RemoteException, UnitOfWorkCompletionException;

    List<CargoRoutingDTO> listAllCargos()
        throws RemoteException;
}