package org.qi4j.samples.cargo.app1.services.routing.internal;

import com.pathfinder.api.GraphTraversalService;
import com.pathfinder.api.TransitEdge;
import com.pathfinder.api.TransitPath;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.samples.cargo.app1.model.cargo.Itinerary;
import org.qi4j.samples.cargo.app1.model.cargo.Leg;
import org.qi4j.samples.cargo.app1.model.cargo.RouteSpecification;
import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.model.voyage.VoyageNumber;
import org.qi4j.samples.cargo.app1.services.routing.RoutingService;
import org.qi4j.samples.cargo.app1.system.factories.LegFactory;
import org.qi4j.samples.cargo.app1.system.repositories.LocationRepository;
import org.qi4j.samples.cargo.app1.system.repositories.VoyageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Our end of the routing service. This is basically a data model
 * translation layer between our domain model and the API put forward
 * by the routing team, which operates in a different context from us.
 */
public abstract class ExternalRoutingService
        implements RoutingService {
    private static final Logger logger = LoggerFactory.getLogger(ExternalRoutingService.class);

    @Service
    private GraphTraversalService graphTraversalService;

    @Service
    private LocationRepository locationRepository;

    @Service
    private VoyageRepository voyageRepository;

    @Service
    private LegFactory legFactory;

    @Structure
    private ValueBuilderFactory vbf;

    public List<Itinerary> fetchRoutesForSpecification(RouteSpecification routeSpecification) {
        /*
         The RouteSpecification is picked apart and adapted to the external API.
        */
        Location origin = routeSpecification.origin();
        Location destination = routeSpecification.destination();

        Properties limitations = new Properties();
        limitations.setProperty("DEADLINE", routeSpecification.arrivalDeadline().toString());

        final List<TransitPath> transitPaths;
        try {
            transitPaths = graphTraversalService.findShortestPath(
                    origin.identity().get(),
                    destination.identity().get(),
                    limitations
            );
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
            return Collections.EMPTY_LIST;
        }

        /*
         The returned result is then translated back into our domain model.
        */
        final List<Itinerary> itineraries = new ArrayList<Itinerary>();

        for (TransitPath transitPath : transitPaths) {
            final Itinerary itinerary = toItinerary(transitPath);
            // Use the specification to safe-guard against invalid itineraries
            if (routeSpecification.isSatisfiedBy(itinerary)) {
                itineraries.add(itinerary);
            } else {
                logger.warn("Received itinerary that did not satisfy the route specification");
            }
        }
        return itineraries;
    }

    private Itinerary toItinerary(TransitPath transitPath) {
        List<Leg> legs = new ArrayList<Leg>(transitPath.getTransitEdges().size());
        for (TransitEdge edge : transitPath.getTransitEdges()) {
            legs.add(toLeg(edge));
        }
        return createItinerary(legs);
    }

    private Leg toLeg(TransitEdge edge) {
        VoyageNumber voyageNumber = createVoyageNumber(edge.getVoyageNumber());
        return legFactory.create(
                voyageRepository.findVoyageByVoyageNumber(voyageNumber),
                locationRepository.findLocationByUnLocode(edge.getFromUnLocode()),
                locationRepository.findLocationByUnLocode(edge.getToUnLocode()),
                edge.getFromDate(), edge.getToDate()
        );
    }

    private VoyageNumber createVoyageNumber(String voyageNumber) {
        ValueBuilder<VoyageNumber> builder = vbf.newValueBuilder(VoyageNumber.class);
        builder.prototype().number().set(voyageNumber);
        return builder.newInstance();
    }

    public Itinerary createItinerary(List<Leg> legs) {
        ValueBuilder<Itinerary> builder = vbf.newValueBuilder(Itinerary.class);
        builder.prototypeFor(Itinerary.State.class).legs().set(legs);
        return builder.newInstance();
    }
}
