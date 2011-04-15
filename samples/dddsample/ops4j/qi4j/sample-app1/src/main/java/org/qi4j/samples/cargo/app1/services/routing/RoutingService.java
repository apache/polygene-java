package org.qi4j.samples.cargo.app1.services.routing;

import org.qi4j.samples.cargo.app1.model.cargo.Itinerary;
import org.qi4j.samples.cargo.app1.model.cargo.RouteSpecification;
import org.qi4j.samples.cargo.app1.services.routing.internal.ExternalRoutingService;
import java.util.List;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

/**
 *
 */
@Mixins(ExternalRoutingService.class )
public interface RoutingService extends ServiceComposite {

    /**
     * @param routeSpecification route specification
     * @return A list of itineraries that satisfy the specification. May be an empty list if no route is found.
     */
    List<Itinerary> fetchRoutesForSpecification(RouteSpecification routeSpecification);

}