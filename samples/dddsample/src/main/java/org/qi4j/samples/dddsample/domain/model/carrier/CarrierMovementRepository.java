package org.qi4j.samples.dddsample.domain.model.carrier;

public interface CarrierMovementRepository
{
    /**
     * @return none carrier movement.
     *
     * @since 0.5
     */
    CarrierMovement noneCarrierMovement();

    /**
     * Finds a carrier movement using given id.
     *
     * @param carrierMovementId Id
     *
     * @return The carrier movement.
     *
     * @since 0.5
     */
    CarrierMovement find( CarrierMovementId carrierMovementId );
}