package org.qi4j.samples.dddsample.application.remoting.assembly;

import org.qi4j.samples.dddsample.application.remoting.dto.CargoRoutingDTO;
import org.qi4j.samples.dddsample.application.remoting.dto.LegDTO;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.Leg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DTO for registering and routing a cargo.
 */
final class CargoRoutingDTOImpl
    implements CargoRoutingDTO
{
    public static CargoRoutingDTO toDTO( final Cargo cargo )
    {
        String trackingId = cargo.trackingId().idString();
        String origin = cargo.origin().unLocode().idString();
        String destination = cargo.destination().unLocode().idString();
        CargoRoutingDTOImpl dto = new CargoRoutingDTOImpl( trackingId, origin, destination );

        List<Leg> legs = cargo.itinerary().legs();
        for( Leg leg : legs )
        {
            String carrierMovementId = leg.carrierMovement().carrierMovementId().idString();
            String legFrom = leg.from().unLocode().idString();
            String legTo = leg.to().unLocode().idString();
            dto.addLeg( carrierMovementId, legFrom, legTo );
        }

        return dto;
    }

    private final String trackingId;
    private final String origin;
    private final String finalDestination;
    private final List<LegDTO> legs;

    private CargoRoutingDTOImpl( final String trackingId, final String origin, final String finalDestination )
    {
        this.trackingId = trackingId;
        this.origin = origin;
        this.finalDestination = finalDestination;
        this.legs = new ArrayList<LegDTO>();
    }

    public String getTrackingId()
    {
        return trackingId;
    }

    public String getOrigin()
    {
        return origin;
    }

    public String getFinalDestination()
    {
        return finalDestination;
    }

    public void addLeg( final String carrierMovementId, final String from, final String to )
    {
        legs.add( new LegDTOImpl( carrierMovementId, from, to ) );
    }

    /**
     * @return An unmodifiable list DTOs.
     */
    public List<LegDTO> getLegs()
    {
        return Collections.unmodifiableList( legs );
    }

    public boolean isRouted()
    {
        return !legs.isEmpty();
    }
}