package org.qi4j.samples.dddsample.domain.model.location;

import org.qi4j.api.query.Query;

public interface LocationRepository
{
    /**
     * @return empty location.
     */
    Location unknownLocation();

    /**
     * Finds a location using given unlocode.
     *
     * @param unLocode UNLocode.
     *
     * @return Location.
     */
    Location find( UnLocode unLocode );

    /**
     * Finds all locations.
     *
     * @return All locations.
     */
//    @Transactional
    Query<Location> findAll();
}