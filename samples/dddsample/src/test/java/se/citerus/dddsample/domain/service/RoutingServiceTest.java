package se.citerus.dddsample.domain.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import junit.framework.TestCase;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import se.citerus.dddsample.application.persistence.LocationRepositoryInMem;
import se.citerus.dddsample.application.routing.ExternalRoutingService;
import se.citerus.dddsample.domain.model.cargo.Cargo;
import se.citerus.dddsample.domain.model.cargo.Itinerary;
import se.citerus.dddsample.domain.model.cargo.Leg;
import se.citerus.dddsample.domain.model.cargo.RouteSpecification;
import se.citerus.dddsample.domain.model.cargo.TrackingId;
import se.citerus.dddsample.domain.model.carrier.CarrierMovement;
import se.citerus.dddsample.domain.model.carrier.CarrierMovementId;
import se.citerus.dddsample.domain.model.carrier.CarrierMovementRepository;
import se.citerus.dddsample.domain.model.location.Location;
import se.citerus.dddsample.domain.model.location.LocationRepository;
import static se.citerus.dddsample.domain.model.location.SampleLocations.CHICAGO;
import static se.citerus.dddsample.domain.model.location.SampleLocations.GOTHENBURG;
import static se.citerus.dddsample.domain.model.location.SampleLocations.HAMBURG;
import static se.citerus.dddsample.domain.model.location.SampleLocations.HELSINKI;
import static se.citerus.dddsample.domain.model.location.SampleLocations.HONGKONG;
import static se.citerus.dddsample.domain.model.location.SampleLocations.STOCKHOLM;
import static se.citerus.dddsample.domain.model.location.SampleLocations.TOKYO;
import se.citerus.routingteam.GraphTraversalService;
import se.citerus.routingteam.internal.GraphDAO;
import se.citerus.routingteam.internal.GraphTraversalServiceImpl;

public class RoutingServiceTest extends TestCase {

  private ExternalRoutingService routingService;
  private CarrierMovementRepository carrierMovementRepository;

  protected void setUp() throws Exception {
    routingService = new ExternalRoutingService();
    LocationRepository locationRepository = new LocationRepositoryInMem();
    routingService.setLocationRepository(locationRepository);

    carrierMovementRepository = createMock(CarrierMovementRepository.class);
    routingService.setCarrierMovementRepository(carrierMovementRepository);

    GraphTraversalService graphTraversalService = new GraphTraversalServiceImpl(new GraphDAO(createMock(DataSource.class)) {
      public List<String> listLocations() {
        return Arrays.asList(TOKYO.unLocode().idString(), STOCKHOLM.unLocode().idString(), GOTHENBURG.unLocode().idString());
      }

      public void storeCarrierMovementId(String cmId, String from, String to) {
      }
    });
    routingService.setGraphTraversalService(graphTraversalService);
  }

  public void testCalculatePossibleRoutes() {
    TrackingId trackingId = new TrackingId("ABC");
    Cargo cargo = new Cargo(trackingId, HONGKONG, HELSINKI);
    RouteSpecification routeSpecification = RouteSpecification.forCargo(cargo, new Date());

    expect(carrierMovementRepository.find(isA(CarrierMovementId.class))).
      andStubReturn(new CarrierMovement(new CarrierMovementId("CM"), CHICAGO, HAMBURG, new Date(), new Date()));
    
    replay(carrierMovementRepository);

    List<Itinerary> candidates = routingService.fetchRoutesForSpecification(routeSpecification);
    assertNotNull(candidates);
    
    for (Itinerary itinerary : candidates) {
      List<Leg> legs = itinerary.legs();
      assertNotNull(legs);
      assertFalse(legs.isEmpty());

      // Cargo origin and start of first leg should match
      assertEquals(cargo.origin(), legs.get(0).from());

      // Cargo final destination and last leg stop should match
      Location lastLegStop = legs.get(legs.size() - 1).to();
      assertEquals(cargo.destination(), lastLegStop);

      for (int i = 0; i < legs.size() - 1; i++) {
        // Assert that all legs are conencted
        assertEquals(legs.get(i).to(), legs.get(i + 1).from());
      }
    }

    verify(carrierMovementRepository);
  }

}
