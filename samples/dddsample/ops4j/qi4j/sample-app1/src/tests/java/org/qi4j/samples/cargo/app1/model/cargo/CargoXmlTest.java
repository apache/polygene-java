/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.samples.cargo.app1.model.cargo;

import com.msdw.eqrisk.vorticity.messaging.qi4j.Person;
import com.msdw.eqrisk.vorticity.messaging.testsupport.VorticityMessagingTest;
import org.apache.cxf.aegis.type.AegisType;
import org.apache.cxf.aegis.xml.stax.ElementReader;
import org.apache.cxf.aegis.xml.stax.ElementWriter;
import org.apache.cxf.helpers.DOMUtils;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.model.location.LocationEntity;
import org.qi4j.samples.cargo.app1.model.voyage.CarrierMovement;
import org.qi4j.samples.cargo.app1.model.voyage.Schedule;
import org.qi4j.samples.cargo.app1.model.voyage.Voyage;
import org.qi4j.samples.cargo.app1.model.voyage.VoyageNumber;
import org.qi4j.samples.cargo.app1.system.factories.DeliveryFactory;
import org.qi4j.samples.cargo.app1.system.factories.HandlingActivityFactory;
import org.qi4j.samples.cargo.app1.system.repositories.HandlingEventRepository;
import org.qi4j.samples.cargo.app1.system.repositories.LocationRepository;
import org.qi4j.samples.cargo.app1.system.repositories.VoyageRepository;
import org.qi4j.samples.cargo.app1.ui.booking.CargoValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class CargoXmlTest extends VorticityMessagingTest {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private LocationRepository locationRepository;
    private VoyageRepository voyageRepository;

    @Override
    protected void assemble(final ModuleAssembly module) throws AssemblyException {
        module.entities( Cargo.class, LocationEntity.class, Voyage.class );
        module.values( Delivery.class, Itinerary.class, Leg.class, TrackingId.class, RouteSpecification.class,
                       HandlingActivity.class, CarrierMovement.class, Schedule.class, VoyageNumber.class,
                       CargoValue.class );
        module.services( LocationRepository.class,
                         VoyageRepository.class,
                         DeliveryFactory.class,
                         HandlingActivityFactory.class,
                         HandlingEventRepository.class

        ).instantiateOnStartup();
    }

    @Override
    protected void initialized() {
        locationRepository = (LocationRepository) serviceFinder.findService(LocationRepository.class).get();
        voyageRepository = (VoyageRepository) serviceFinder.findService(VoyageRepository.class).get();
    }

    @Test
    public void givenCargoWhenConvertingToSoapMessageExpectCorrectFormat()
            throws Exception {
        AegisType type = typeMapping.getType(CargoValue.class);
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try {
            // Test Writing
            Location origin = locationRepository.findLocationByUnLocode("SESTO");
            Location destination = locationRepository.findLocationByUnLocode("AUMEL");
            RouteSpecification specification = createRouteSpecification(origin.identity().get(), destination.identity().get(), createDate("2009-04-15"));
            final Voyage STO_TO_HAM = voyageRepository.findVoyageByVoyageIdentity("V400");
            final Voyage HAM_TO_MEL = voyageRepository.findVoyageByVoyageIdentity("V300");
            Leg leg1 = createLeg(origin.identity().get(), createDate("2010-12-01"), "DEHAM", createDate("2010-12-05"), STO_TO_HAM);
            Leg leg2 = createLeg("DEHAM", createDate("2010-12-01"), destination.identity().get(), createDate("2010-12-05"), HAM_TO_MEL);
            List<Leg> legs = createLegs(leg1, leg2);
            Itinerary itinerary = createItinerary(legs);
            CargoValue cargo = createCargo(origin, specification, itinerary, null);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ElementWriter writer = new ElementWriter(bos, "cargo", "urn:qi4j:type");
            type.writeObject(cargo, writer, getContext());
            writer.close();
            writer.flush();

            bos.close();

            byte[] bytes = bos.toByteArray();
            System.out.println(new String(bytes));
            Document doc = DOMUtils.readXml(new ByteArrayInputStream(bytes));
            Element element = doc.getDocumentElement();

            addNamespace("ns1", "urn:qi4j:type");
            assertValid("/ns1:response/fullName[text()='Niclas Hedhman']", element);
            assertValid("/ns1:response/car/year[text()='2005']", element);

            // Test reading
            ElementReader reader = new ElementReader(new ByteArrayInputStream(VALUE1.getBytes()));
            Person person = (Person) type.readObject(reader, getContext());
            reader.getXMLStreamReader().close();
            assertEquals("Niclas Hedhman", person.fullName().get());
            assertEquals("2005", person.car().get().year().get());
            assertEquals("Mini Cooper", person.car().get().model().get());
            assertEquals("BMW", person.car().get().manufacturer().get());
        } finally {
            uow.discard();
        }
    }
//
//    private HandlingEvent createHandlingEvent(HandlingEvent.Type eventType, Cargo cargo, Date completionTime) {
//        UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
//        EntityBuilder<HandlingEvent> builder = uow.newEntityBuilder(HandlingEvent.class);
//        HandlingEvent.State instance = builder.instanceFor(HandlingEvent.State.class);
//        instance.cargo().set( cargo );
//        instance.completionTime().set(completionTime);
//        instance.eventType().set(eventType);
//        return builder.newInstance();
//    }
//
//    private Delivery createDelivery(RouteSpecification routeSpecification, Itinerary itinerary, String lastHandlingEventIdentity) {
//        ValueBuilder<Delivery> builder = valueBuilderFactory.newValueBuilder(Delivery.class);
//        Delivery.State prototype = builder.prototypeFor(Delivery.State.class);
//        prototype.itinerary().set(itinerary);
//        prototype.lastHandlingEventIdentity().set(lastHandlingEventIdentity);
//        prototype.routeSpecification().set(routeSpecification);
//        return builder.newInstance();
//    }

    private Date createDate(final String dateString)
            throws ParseException {
        return sdf.parse(dateString);
    }

    private Leg createLeg(String loadLocation, Date loadTime,
                          String unloadLocation, Date unloadTime,
                          Voyage voyage) {
        ValueBuilder<Leg> builder = valueBuilderFactory.newValueBuilder(Leg.class);
        Leg.State prototype = builder.prototypeFor(Leg.State.class);
        prototype.loadLocationUnLocodeIdentity().set(loadLocation);
        prototype.loadTime().set(loadTime);
        prototype.unloadLocationUnLocodeIdentity().set(unloadLocation);
        prototype.unloadTime().set(unloadTime);
        prototype.voyageIdentity().set(voyage.voyageNumber().get().number().get());
        return builder.newInstance();
    }

    private List<Leg> createLegs(Leg... legs) {
        return Arrays.asList(legs);
    }

    private CargoValue createCargo(Location origin, RouteSpecification specification, Itinerary itinerary, Delivery delivery) {
        ValueBuilder<CargoValue> builder = valueBuilderFactory.newValueBuilder(CargoValue.class);
        CargoValue prototype = builder.prototype();
        prototype.origin().set(origin.identity().get());
        prototype.routeSpecification().set(specification);
        prototype.itinerary().set(itinerary);
        prototype.delivery().set(delivery);
        return builder.newInstance();
    }

    private Itinerary createItinerary(List<Leg> legs) {
        ValueBuilder<Itinerary> builder = valueBuilderFactory.newValueBuilder(Itinerary.class);
        Itinerary.State prototype = builder.prototypeFor(Itinerary.State.class);
        prototype.legs().set(legs);
        return builder.newInstance();
    }

    private RouteSpecification createRouteSpecification(String originUnLocodeIdentity,
                                                        String destinationUnLocodeIdentity,
                                                        Date arrivalDeadline) {
        ValueBuilder<RouteSpecification> builder = valueBuilderFactory.newValueBuilder(RouteSpecification.class);
        RouteSpecification.State template = builder.prototypeFor(RouteSpecification.State.class);
        template.arrivalDeadline().set(arrivalDeadline);
        template.originUnLocodeIdentity().set(originUnLocodeIdentity);
        template.destinationUnLocodeIdentity().set(destinationUnLocodeIdentity);
        return builder.newInstance();
    }

    private Location createLocation(String unLocode, String commonName) {
        ValueBuilder<Location> builder = valueBuilderFactory.newValueBuilder(Location.class);
        Location prototype = builder.prototype();
        prototype.identity().set(unLocode);
        prototype.commonName().set(commonName);
        return builder.newInstance();
    }

    private static final String VALUE1 = "";
}