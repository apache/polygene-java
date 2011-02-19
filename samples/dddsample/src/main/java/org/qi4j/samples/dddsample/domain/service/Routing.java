package org.qi4j.samples.dddsample.domain.service;

import org.qi4j.api.query.Query;
import org.qi4j.samples.dddsample.domain.model.cargo.Itinerary;
import org.qi4j.samples.dddsample.domain.model.cargo.RouteSpecification;

/**
 * Routing service.
 */
public interface Routing
{

    /**
     * @param routeSpecification route specification
     *
     * @return A list of itineraries that satisfy the specification. May be an empty list if no route is found.
     */
    Query<Itinerary> fetchRoutesForSpecification( RouteSpecification routeSpecification );
}