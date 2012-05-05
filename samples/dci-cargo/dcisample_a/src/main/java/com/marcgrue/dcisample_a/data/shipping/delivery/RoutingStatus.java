package com.marcgrue.dcisample_a.data.shipping.delivery;

/**
 * A routing status indicates whether an itinerary is assigned to
 * a cargo and satisfying the route specification.
 */
public enum RoutingStatus
{
    NOT_ROUTED,    // Itinerary has not been assigned to cargo yet
    ROUTED,        // Itinerary is assigned to cargo
    MISROUTED;     // RouteSpecification is not satisfied by Itinerary
}
