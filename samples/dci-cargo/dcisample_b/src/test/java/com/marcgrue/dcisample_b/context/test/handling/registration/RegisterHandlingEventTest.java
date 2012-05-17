package com.marcgrue.dcisample_b.context.test.handling.registration;

import com.marcgrue.dcisample_b.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import com.marcgrue.dcisample_b.context.interaction.handling.registration.RegisterHandlingEvent;
import com.marcgrue.dcisample_b.context.interaction.handling.registration.exception.*;
import com.marcgrue.dcisample_b.data.entity.HandlingEventEntity;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;

import java.util.Date;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.ROUTED;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.IN_PORT;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.NOT_RECEIVED;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.*;

/**
 * {@link RegisterHandlingEvent} tests
 */
public class RegisterHandlingEventTest extends TestApplication {
    ParsedHandlingEventData parsedEventData;
    UnitOfWork tempUow;

    @BeforeClass
    public static void setup() throws Exception {
        TestApplication.setup();

        // Create new cargo
        routeSpec = routeSpecFactory.build(HONGKONG, STOCKHOLM, new Date(), deadline = DAY24);
        delivery = delivery(TODAY, NOT_RECEIVED, ROUTED, unknownLeg);
        cargo = CARGOS.createCargo(routeSpec, delivery, "ABC");
        cargo.itinerary().set(itinerary);
        trackingId = cargo.trackingId().get();
    }


    @Test
    public void deviation_1a_UnknownCargo() throws Exception {
        parsedEventData = parsedHandlingEventData(DAY1, DAY1, "XXX", RECEIVE, "CNHKG", null);
        thrown.expect(UnknownCargoException.class, "Found no cargo with tracking id 'XXX'.");
        handlingEvent = new RegisterHandlingEvent(parsedEventData).getEvent();
    }

    @Test
    public void deviation_2a_UnknownUnlocode() throws Exception {
        parsedEventData = parsedHandlingEventData(DAY1, DAY1, "ABC", RECEIVE, "ZZZZZ", null);
        thrown.expect(UnknownLocationException.class, "Found no location with UN locode 'ZZZZZ'.");
        handlingEvent = new RegisterHandlingEvent(parsedEventData).getEvent();
    }


    @Test
    public void deviation_3a_VoyageNumber_SilentlySkipIfNotRequired() throws Exception {
        parsedEventData = parsedHandlingEventData(DAY1, DAY1, "ABC", RECEIVE, "CNHKG", "V201");
        handlingEvent = new RegisterHandlingEvent(parsedEventData).getEvent();
    }

    @Test
    public void deviation_3b_VoyageNumber_Missing() throws Exception {
        parsedEventData = parsedHandlingEventData(DAY1, DAY1, "ABC", LOAD, "CNHKG", null);
        thrown.expect(MissingVoyageNumberException.class, "Missing voyage number. Handling event LOAD requires a voyage.");
        handlingEvent = new RegisterHandlingEvent(parsedEventData).getEvent();
    }

    @Test
    public void deviation_3c_VoyageNumber_Unknown() throws Exception {
        parsedEventData = parsedHandlingEventData(DAY1, DAY1, "ABC", LOAD, "CNHKG", "V600S");
        thrown.expect(UnknownVoyageException.class, "Found no voyage with voyage number 'V600S'.");
        handlingEvent = new RegisterHandlingEvent(parsedEventData).getEvent();
    }

    @Test
    public void deviation_4a_DuplicateEvent_Receive() throws Exception {
        // Receive 1st time (store event so that it turns up in query)
        uow.complete();
        tempUow = uowf.newUnitOfWork();
        handlingEvent = HANDLING_EVENTS.createHandlingEvent(DAY1, DAY1, trackingId, RECEIVE, HONGKONG, noVoyage);
        tempUow.complete();

        uow = uowf.newUnitOfWork();
        // Receive 2nd time
        parsedEventData = parsedHandlingEventData(DAY2, DAY2, "ABC", RECEIVE, "CNHKG", null);
        thrown.expect(DuplicateEventException.class, "Cargo can't be received more than once");
        handlingEvent = new RegisterHandlingEvent(parsedEventData).getEvent();
    }

    @Test
    public void deviation_4a_DuplicateEvent_Customs() throws Exception {
        uow.complete();
        // In customs 1st time
        tempUow = uowf.newUnitOfWork();
        handlingEvent = HANDLING_EVENTS.createHandlingEvent(DAY1, DAY1, trackingId, CUSTOMS, HONGKONG, noVoyage);
        tempUow.complete();

        // In customs 2nd time
        uow = uowf.newUnitOfWork();
        parsedEventData = parsedHandlingEventData(DAY2, DAY2, "ABC", CUSTOMS, "CNHKG", null);
        thrown.expect(DuplicateEventException.class, "Cargo can't be in customs more than once");
        handlingEvent = new RegisterHandlingEvent(parsedEventData).getEvent();
    }

    @Test
    public void deviation_4a_DuplicateEvent_Claim() throws Exception {
        uow.complete();
        // Claimed 1st time
        tempUow = uowf.newUnitOfWork();
        handlingEvent = HANDLING_EVENTS.createHandlingEvent(DAY1, DAY1, trackingId, CLAIM, HONGKONG, noVoyage);
        tempUow.complete();

        // Claimed 2nd time
        uow = uowf.newUnitOfWork();
        parsedEventData = parsedHandlingEventData(DAY2, DAY2, "ABC", CLAIM, "CNHKG", null);
        thrown.expect(DuplicateEventException.class, "Cargo can't be claimed more than once");
        new RegisterHandlingEvent(parsedEventData).getEvent();
    }

    @Test
    public void deviation_5a_NoHandlingAfterClaim() throws Exception {
        // Try loading (saved claim event in previous test will prevent this)
        parsedEventData = parsedHandlingEventData(DAY2, DAY2, "ABC", LOAD, "CNHKG", "V201");
        thrown.expect(AlreadyClaimedException.class, "LOAD handling event can't be registered after cargo has been claimed");
        new RegisterHandlingEvent(parsedEventData).getEvent();
    }

    @Test
    public void successfull_Registration() throws Exception {
        // Delete handling events from memory
        tempUow = uowf.newUnitOfWork();
        Query<HandlingEventEntity> events = tempUow.newQuery( qbf.newQueryBuilder(HandlingEventEntity.class));
        for (HandlingEvent event : events)
            tempUow.remove(event);
        tempUow.complete();

        cargo.delivery().set(delivery(null, NOT_RECEIVED, notArrived,
                ROUTED, directed, itinerary.eta(), leg1,
                nextHandlingEvent(RECEIVE, HONGKONG, DAY1, noVoyage)));

        assertDelivery(null, null, null, null,
                NOT_RECEIVED, notArrived,
                ROUTED, directed, itinerary.eta(), leg1,
                RECEIVE, HONGKONG, DAY1, noVoyage);

        parsedEventData = parsedHandlingEventData(DAY1, DAY1, "ABC", RECEIVE, "CNHKG", null);
        handlingEvent = new RegisterHandlingEvent(parsedEventData).getEvent();


        cargo.delivery().set(delivery(handlingEvent, IN_PORT, notArrived,
                ROUTED, directed, itinerary.eta(), leg1,
                nextHandlingEvent(LOAD, HONGKONG, DAY1, V201)));

        assertDelivery(RECEIVE, HONGKONG, DAY1, noVoyage,  // Handling event has been registered
                IN_PORT, notArrived,
                ROUTED, directed, itinerary.eta(), leg1,
                LOAD, HONGKONG, DAY1, V201);
    }
}