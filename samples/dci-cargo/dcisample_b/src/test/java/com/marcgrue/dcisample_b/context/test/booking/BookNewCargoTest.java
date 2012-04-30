package com.marcgrue.dcisample_b.context.test.booking;

import com.marcgrue.dcisample_b.bootstrap.test.TestApplication;
import com.marcgrue.dcisample_b.context.interaction.booking.BookNewCargo;
import com.marcgrue.dcisample_b.data.entity.CargoEntity;
import com.marcgrue.dcisample_b.data.factory.exception.CannotCreateCargoException;
import com.marcgrue.dcisample_b.data.factory.exception.CannotCreateRouteSpecificationException;
import org.junit.Test;
import org.qi4j.api.constraint.ConstraintViolationException;

import static com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus.NOT_ROUTED;
import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.NOT_RECEIVED;
import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.RECEIVE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link BookNewCargo} tests
 */
public class BookNewCargoTest extends TestApplication
{
    @Test
    public void deviation_2a_OriginAndDestinationSame() throws Exception
    {
        thrown.expect( CannotCreateRouteSpecificationException.class, "Origin location can't be same as destination location." );
        new BookNewCargo( CARGOS, HONGKONG, HONGKONG, DAY24 ).getTrackingId();
    }

    @Test
    public void deviation_2b_DeadlineInThePastNotAccepted() throws Exception
    {
        thrown.expect( CannotCreateRouteSpecificationException.class, "Arrival deadline is in the past or Today." );
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( -1 ) ).getTrackingId();
    }

    @Test
    public void deviation_2b_DeadlineTodayIsTooEarly() throws Exception
    {
        thrown.expect( CannotCreateRouteSpecificationException.class, "Arrival deadline is in the past or Today." );
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( 0 ) ).getTrackingId();
    }

    @Test
    public void deviation_2b_DeadlineTomorrowIsOkay() throws Exception
    {
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY1 ).getTrackingId();
    }

    @Test
    public void step_2_CanCreateRouteSpecification() throws Exception
    {
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).getTrackingId();
        cargo = uow.get( CargoEntity.class, trackingId.id().get() );
        assertThat( cargo.routeSpecification().get().origin().get(), is( equalTo( HONGKONG ) ) );
        assertThat( cargo.routeSpecification().get().destination().get(), is( equalTo( STOCKHOLM ) ) );
        assertThat( cargo.routeSpecification().get().arrivalDeadline().get(), is( equalTo( DAY24 ) ) );
    }

    @Test
    public void step_3_CanDeriveInitialDeliveryData() throws Exception
    {
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).getTrackingId();
        cargo = uow.get( CargoEntity.class, trackingId.id().get() );
        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        NOT_ROUTED, directed, unknownETA, unknownLeg,
                        RECEIVE, HONGKONG, noSpecificDate, noVoyage );
    }

    @Test
    public void deviation_4a_TrackingIdTooShort() throws Exception
    {
        thrown.expect( ConstraintViolationException.class, "for value 'no'" );
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "no" );
    }

    @Test
    public void deviation_4a_TrackingIdNotTooShort() throws Exception
    {
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "yes" );
        cargo = uow.get( CargoEntity.class, trackingId.id().get() );
        assertThat( cargo.trackingId().get().id().get(), is( equalTo( "yes" ) ) );
    }

    @Test
    public void deviation_4a_TrackingIdTooLong() throws Exception
    {
        thrown.expect( ConstraintViolationException.class, "for value '1234567890123456789012345678901'" );
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "1234567890123456789012345678901" );
    }

    @Test
    public void deviation_4a_TrackingIdNotTooLong() throws Exception
    {
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "123456789012345678901234567890" );
        cargo = uow.get( CargoEntity.class, trackingId.id().get() );
        assertThat( cargo.trackingId().get().id().get(), is( equalTo( "123456789012345678901234567890" ) ) );
    }

    @Test
    public void deviation_4a_TrackingIdWithWrongCharacter() throws Exception
    {
        thrown.expect( ConstraintViolationException.class, "for value 'Gšteborg1234'" );
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "Gšteborg1234" );
    }

    @Test
    public void deviation_4b_TrackingIdNotUnique() throws Exception
    {
        thrown.expect( CannotCreateCargoException.class, "Tracking id 'yes' is not unique." );
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "yes" );
    }

    @Test
    public void step_4_CanAutoCreateTrackingIdFromEmptyString() throws Exception
    {
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "" );
    }

    @Test
    public void step_4_CanAutoCreateTrackingIdFromNull() throws Exception
    {
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( null );
    }

    @Test
    public void success_BookNewCargo() throws Exception
    {
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "ABC" );
        cargo = uow.get( CargoEntity.class, trackingId.id().get() );

        assertThat( cargo.trackingId().get(), is( equalTo( trackingId ) ) );
        assertThat( cargo.trackingId().get().id().get(), is( equalTo( "ABC" ) ) );
        assertThat( cargo.origin().get(), is( equalTo( HONGKONG ) ) );

        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        NOT_ROUTED, directed, unknownETA, unknownLeg,
                        RECEIVE, HONGKONG, noSpecificDate, noVoyage );
    }
}
