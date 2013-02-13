/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_b.context.test.booking;

import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.sample.dcicargo.sample_b.bootstrap.test.TestApplication;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.booking.BookNewCargo;
import org.qi4j.sample.dcicargo.sample_b.data.entity.CargoEntity;
import org.qi4j.sample.dcicargo.sample_b.data.factory.exception.CannotCreateCargoException;
import org.qi4j.sample.dcicargo.sample_b.data.factory.exception.CannotCreateRouteSpecificationException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus.NOT_ROUTED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.NOT_RECEIVED;
import static org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.RECEIVE;

/**
 * {@link BookNewCargo} tests
 *
 * FIXME: Every test method call the one above to allow ordered execution, ie. tests are not indepedants !
 */
public class BookNewCargoTest extends TestApplication
{
    @Test
    public void deviation_2a_OriginAndDestinationSame() throws Exception
    {
        try
        {
            thrown.expect( CannotCreateRouteSpecificationException.class, "Origin location can't be same as destination location." );
            new BookNewCargo( CARGOS, HONGKONG, HONGKONG, DAY24 ).getTrackingId();
        }
        catch( Exception ex )
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Test
    //@Ignore
    public void deviation_2b_DeadlineInThePastNotAccepted() throws Exception
    {
        deviation_2a_OriginAndDestinationSame();
        thrown.expect( CannotCreateRouteSpecificationException.class, "Arrival deadline is in the past or Today." );
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( -1 ) ).getTrackingId();
    }

    @Test
    //@Ignore
    public void deviation_2b_DeadlineTodayIsTooEarly() throws Exception
    {
        deviation_2b_DeadlineInThePastNotAccepted();
        thrown.expect( CannotCreateRouteSpecificationException.class, "Arrival deadline is in the past or Today." );
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, day( 0 ) ).getTrackingId();
    }

    @Test
    //@Ignore
    public void deviation_2b_DeadlineTomorrowIsOkay() throws Exception
    {
        deviation_2b_DeadlineTodayIsTooEarly();
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY1 ).getTrackingId();
    }

    @Test
    //@Ignore
    public void step_2_CanCreateRouteSpecification() throws Exception
    {
        deviation_2b_DeadlineTomorrowIsOkay();
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).getTrackingId();
        cargo = uow.get( CargoEntity.class, trackingId.id().get() );
        assertThat( cargo.routeSpecification().get().origin().get(), is( equalTo( HONGKONG ) ) );
        assertThat( cargo.routeSpecification().get().destination().get(), is( equalTo( STOCKHOLM ) ) );
        assertThat( cargo.routeSpecification().get().arrivalDeadline().get(), is( equalTo( DAY24 ) ) );
    }

    @Test
    //@Ignore
    public void step_3_CanDeriveInitialDeliveryData() throws Exception
    {
        step_2_CanCreateRouteSpecification();
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).getTrackingId();
        cargo = uow.get( CargoEntity.class, trackingId.id().get() );
        assertDelivery( null, null, null, null,
                        NOT_RECEIVED, notArrived,
                        NOT_ROUTED, directed, unknownETA, unknownLeg,
                        RECEIVE, HONGKONG, noSpecificDate, noVoyage );
    }

    @Test
    //@Ignore
    public void deviation_4a_TrackingIdTooShort() throws Exception
    {
        step_3_CanDeriveInitialDeliveryData();
        thrown.expect( ConstraintViolationException.class, "for value 'no'" );
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "no" );
    }

    @Test
    //@Ignore
    public void deviation_4a_TrackingIdNotTooShort() throws Exception
    {
        deviation_4a_TrackingIdTooShort();
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "yes" );
        cargo = uow.get( CargoEntity.class, trackingId.id().get() );
        assertThat( cargo.trackingId().get().id().get(), is( equalTo( "yes" ) ) );
    }

    @Test
    //@Ignore
    public void deviation_4a_TrackingIdTooLong() throws Exception
    {
        deviation_4a_TrackingIdNotTooShort();
        thrown.expect( ConstraintViolationException.class, "for value '1234567890123456789012345678901'" );
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "1234567890123456789012345678901" );
    }

    @Test
    //@Ignore
    public void deviation_4a_TrackingIdNotTooLong() throws Exception
    {
        deviation_4a_TrackingIdTooLong();
        trackingId = new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "123456789012345678901234567890" );
        cargo = uow.get( CargoEntity.class, trackingId.id().get() );
        assertThat( cargo.trackingId().get().id().get(), is( equalTo( "123456789012345678901234567890" ) ) );
    }

    @Test
    //@Ignore
    public void deviation_4a_TrackingIdWithWrongCharacter() throws Exception
    {
        deviation_4a_TrackingIdNotTooLong();
        thrown.expect( ConstraintViolationException.class, "for value 'Göteborg1234'" );
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "Göteborg1234" );
    }

    @Test
    //@Ignore
    public void deviation_4b_TrackingIdNotUnique() throws Exception
    {
        deviation_4a_TrackingIdWithWrongCharacter();
        thrown.expect( CannotCreateCargoException.class, "Tracking id 'yes' is not unique." );
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "yes" );
    }

    @Test
    //@Ignore
    public void step_4_CanAutoCreateTrackingIdFromEmptyString() throws Exception
    {
        deviation_4b_TrackingIdNotUnique();
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( "" );
    }

    @Test
    //@Ignore
    public void step_4_CanAutoCreateTrackingIdFromNull() throws Exception
    {
        step_4_CanAutoCreateTrackingIdFromEmptyString();
        new BookNewCargo( CARGOS, HONGKONG, STOCKHOLM, DAY24 ).withTrackingId( null );
    }

    @Test
    //@Ignore
    public void success_BookNewCargo() throws Exception
    {
        step_4_CanAutoCreateTrackingIdFromNull();
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
