package se.citerus.dddsample.domain.model.cargo;

import java.util.Date;
import junit.framework.TestCase;
import static se.citerus.dddsample.domain.model.location.SampleLocations.CHICAGO;
import static se.citerus.dddsample.domain.model.location.SampleLocations.HONGKONG;

public class RouteSpecificationTest extends TestCase {

  private Cargo cargo;

  public void setUp() {
    cargo = new Cargo(new TrackingId("AAA"), HONGKONG, CHICAGO);
  }

  public void testIsSatisfiedBySuccess() {
    // TODO
    RouteSpecification spec = RouteSpecification.forCargo(cargo, new Date());
    Itinerary itinerary = new Itinerary();
    assertTrue(spec.isSatisfiedBy(itinerary));
  }

  public void testIsSatisfiedByInvalidDate() {
    // TODO
  }

  public void testIsSatisfiedByInvalidOrigin() {
    // TODO
  }

  public void testIsSatisfiedByInvalidDestination() {
    // TODO
  }

}
