package org.qi4j.samples.dddsample.domain.model.handling;

import org.qi4j.api.query.Query;
import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;

/**
 * Handling event repository.
 */
public interface HandlingEventRepository
{
    /**
     * @param trackingId cargo tracking id
     *
     * @return All handling events for this cargo, ordered by completion time.
     *         Returns {@code null} if there is no cargo with the specified tracking id.
     */
//    @Transactional
    Query<HandlingEvent> findEventsForCargo( TrackingId trackingId );
}