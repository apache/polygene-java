package org.qi4j.samples.dddsample.application.remoting.assembly;

import org.qi4j.samples.dddsample.application.remoting.dto.LegDTO;

/**
 * DTO for a leg in an itinerary.
 */
final class LegDTOImpl
    implements LegDTO
{
    private final String carrierMovementId;
    private final String from;
    private final String to;

    /**
     * Constructor.
     *
     * @param carrierMovementId
     * @param from
     * @param to
     */
    LegDTOImpl( final String carrierMovementId, final String from, final String to )
    {
        this.carrierMovementId = carrierMovementId;
        this.from = from;
        this.to = to;
    }

    public String getCarrierMovementId()
    {
        return carrierMovementId;
    }

    public String getFrom()
    {
        return from;
    }

    public String getTo()
    {
        return to;
    }
}