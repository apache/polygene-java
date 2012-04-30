package com.marcgrue.dcisample_a.context.shipping.handling;

import com.marcgrue.dcisample_a.context.support.ApplicationEvents;
import com.marcgrue.dcisample_a.context.support.RegisterHandlingEventAttemptDTO;
import com.marcgrue.dcisample_a.data.entity.HandlingEventsEntity;
import com.marcgrue.dcisample_a.data.shipping.cargo.Cargo;
import com.marcgrue.dcisample_a.data.shipping.cargo.TrackingId;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEvent;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEventType;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEvents;
import com.marcgrue.dcisample_a.data.shipping.location.Location;
import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import com.marcgrue.dcisample_a.infrastructure.dci.Context;
import com.marcgrue.dcisample_a.infrastructure.dci.RoleMixin;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;

import java.util.Arrays;
import java.util.Date;

import static com.marcgrue.dcisample_a.data.entity.HandlingEventsEntity.HANDLING_EVENTS_ID;

/**
 * Register new handling event use case.
 *
 * The Cargo is updated synchronously in this implementation.
 */
public class RegisterHandlingEvent extends Context
{
    // ROLES ---------------------------------------------------------------------

    private HandlingEventFactoryRole handlingEventFactory;

    private Date registrationTime;
    private Date completionTime;
    private String trackingIdString;
    private String eventTypeString;
    private String unLocodeString;
    private String voyageNumberString;


    // CONTEXT CONSTRUCTORS ------------------------------------------------------

    public RegisterHandlingEvent( Date registrationTime,
                                  Date completionTime,
                                  String trackingIdString,
                                  String eventTypeString,
                                  String unLocodeString,
                                  String voyageNumberString )
    {
        handlingEventFactory = rolePlayer( HandlingEventFactoryRole.class, HandlingEventsEntity.class, HANDLING_EVENTS_ID );

        this.registrationTime = registrationTime;
        this.completionTime = completionTime;
        this.trackingIdString = trackingIdString;
        this.eventTypeString = eventTypeString;
        this.unLocodeString = unLocodeString;
        this.voyageNumberString = voyageNumberString;
    }


    // INTERACTIONS --------------------------------------------------------------

    public void register()
    {
        handlingEventFactory.registerHandlingEvent();
    }


    // METHODFUL ROLE IMPLEMENTATIONS --------------------------------------------

    @Mixins( HandlingEventFactoryRole.Mixin.class )
    public interface HandlingEventFactoryRole
    {
        void setContext( RegisterHandlingEvent context );

        void registerHandlingEvent();

        class Mixin
              extends RoleMixin<RegisterHandlingEvent>
              implements HandlingEventFactoryRole
        {
            @Service
            ApplicationEvents applicationEvents;

            @This
            HandlingEvents handlingEvents;

            // Handling event properties
            TrackingId trackingId;
            HandlingEventType handlingEventType;
            Location location;
            Voyage voyage;


            public void registerHandlingEvent()
            {
                RegisterHandlingEventAttemptDTO registrationAttempt = buildRegistrationAttempt();

                // Step 1: Publish event stating that registration attempt has been received.
                applicationEvents.receivedHandlingEventRegistrationAttempt( registrationAttempt );

                HandlingEvent handlingEvent = null;
                try
                {
                    // Step 2: Verify existing data
                    verifyExistingData();

                    // Step 3: Verify that received data can represent a valid handling event
                    verifyValidData();

                    // Step 4: Create and save handling event
                    handlingEvent = handlingEvents.createHandlingEvent(
                          context.registrationTime, context.completionTime, trackingId, handlingEventType, location, voyage );
                }
                catch (IllegalArgumentException e)
                {
                    // Deviation 2-5a: Publish event if registration is unsuccessful
                    applicationEvents.unsuccessfulHandlingEventRegistration( registrationAttempt );
                    throw e;
                }

                // Step 5: Inspect cargo
                new InspectCargo( handlingEvent ).inspect();

                // Step 6: Publish notification that cargo was successfully handled
                applicationEvents.cargoWasHandled( handlingEvent );
            }

            private RegisterHandlingEventAttemptDTO buildRegistrationAttempt()
            {
                ValueBuilder<RegisterHandlingEventAttemptDTO> builder =
                      vbf.newValueBuilder( RegisterHandlingEventAttemptDTO.class );
                builder.prototype().registrationTime().set( context.registrationTime );
                builder.prototype().completionTime().set( context.completionTime );
                builder.prototype().trackingIdString().set( context.trackingIdString );
                builder.prototype().eventTypeString().set( context.eventTypeString );
                builder.prototype().unLocodeString().set( context.unLocodeString );
                builder.prototype().voyageNumberString().set( context.voyageNumberString );
                return builder.newInstance();
            }

            private void verifyExistingData()
            {
                if (context.registrationTime == null)
                    throw new IllegalArgumentException( "Registration time was null. All parameters have to be passed." );
                if (context.completionTime == null)
                    throw new IllegalArgumentException( "Completion time was null. All parameters have to be passed." );

                context.trackingIdString = getClean( "Tracking id", context.trackingIdString );
                context.eventTypeString = getClean( "Event type", context.eventTypeString );
                context.unLocodeString = getClean( "UnLocode", context.unLocodeString );

                // Voyage is optional
            }

            private String getClean( String parameter, String value )
            {
                if (value == null)
                    throw new IllegalArgumentException( parameter + " was null. All parameters have to be passed." );

                if (value.trim().length() == 0)
                    throw new IllegalArgumentException( parameter + " cannot be empty." );

                return value.trim();
            }

            private void verifyValidData()
            {
                // Deviation 3a
                try
                {
                    handlingEventType = HandlingEventType.valueOf( context.eventTypeString );
                }
                catch (Exception e)
                {
                    throw new IllegalArgumentException(
                          "'" + context.eventTypeString + "' is not a valid handling event type. Valid types are: "
                                + Arrays.toString( HandlingEventType.values() ) );
                }

                // Verifications against data store
                UnitOfWork uow = uowf.currentUnitOfWork();

                // Deviation 3b
                try
                {
                    // Will throw NoSuchEntityException if not found in store
                    Cargo cargo = uow.get( Cargo.class, context.trackingIdString );
                    trackingId = cargo.trackingId().get();

                    // Deviation 3c
                    if (cargo.itinerary().get() == null)
                        throw new IllegalArgumentException( "Can't create handling event for non-routed cargo '"
                                                                  + context.trackingIdString + "'." );
                }
                catch (NoSuchEntityException e)
                {
                    throw new IllegalArgumentException( "Found no cargo with tracking id '" + context.trackingIdString + "'." );
                }

                // Deviation 3d
                try
                {
                    location = uow.get( Location.class, context.unLocodeString );
                }
                catch (NoSuchEntityException e)
                {
                    throw new IllegalArgumentException( "Unknown location: " + context.unLocodeString );
                }

                // Deviation 3e
                if (handlingEventType.requiresVoyage())
                {
                    // Deviation 3e1a
                    if (context.voyageNumberString == null)
                        throw new IllegalArgumentException( "Handling event " + handlingEventType.toString() +
                                                                  " requires a voyage. No voyage number submitted." );

                    // Deviation 3e1b
                    try
                    {
                        voyage = uow.get( Voyage.class, context.voyageNumberString );
                    }
                    catch (NoSuchEntityException e)
                    {
                        throw new IllegalArgumentException(
                              "Found no voyage with voyage number '" + context.voyageNumberString + "'." );
                    }
                }
                else
                {
                    // Deviation 3f
                    voyage = null;
                }
            }
        }
    }
}
