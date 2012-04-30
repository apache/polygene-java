package com.marcgrue.dcisample_b.context.interaction.handling;

import com.marcgrue.dcisample_b.context.interaction.handling.inspection.InspectCargoDeliveryStatus;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.CargoArrivedException;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.InspectionException;
import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.InspectionFailedException;
import com.marcgrue.dcisample_b.context.interaction.handling.parsing.ParseHandlingEventData;
import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import com.marcgrue.dcisample_b.context.interaction.handling.parsing.exception.InvalidHandlingEventDataException;
import com.marcgrue.dcisample_b.context.interaction.handling.registration.RegisterHandlingEvent;
import com.marcgrue.dcisample_b.context.interaction.handling.registration.exception.CannotRegisterHandlingEventException;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process Handling Event (subfunction use case)
 *
 * This is the main subfunction use case for processing incoming handling event registration attempts
 * from incident logging applications.
 *
 * The main responsibility here is to delegate the event processing to (sub)subfunction use cases
 * and to notify relevant parties when certain conditions are met (exceptions thrown). Notifi-
 * cations to the cargo owner are simply presented as red-color key words like "Misrouted" in
 * the booking application UI.
 *
 * A "Handling Manager" in this use case delegates to to following Roles in Contexts
 * (Subfunction Use Cases):
 *
 * 1. Data Parser in {@link ParseHandlingEventData}
 * 2. Event Registrar in {@link RegisterHandlingEvent}
 * 3. Delivery Inspector in {@link com.marcgrue.dcisample_b.context.interaction.handling.inspection.InspectCargoDeliveryStatus}
 *
 * For now we simply (synchronously) invoke each subfunction use case with a method call. But
 * we could also implement this asynchronously with JMS or other similar solutions and have
 * message consumers initiate the subfunction use cases instead (as in JmsApplicationEventsImpl
 * in the DDD sample).
 */
@Mixins( ProcessHandlingEvent.SynchronousProcessingStub.class )
public interface ProcessHandlingEvent
      extends TransientComposite
{

    void parse( String completion, String trackingId, String eventType, String unLocode, @Optional String voyage ) throws ProcessHandlingEventException;

    void register( ParsedHandlingEventData parsedHandlingEventData ) throws ProcessHandlingEventException;

    void inspect( HandlingEvent handlingEvent );


    abstract class SynchronousProcessingStub
          implements ProcessHandlingEvent
    {
        Logger logger = LoggerFactory.getLogger( ProcessHandlingEvent.class );

        @Service
        ParseHandlingEventData parser;


        // Step 1 - Parse handling event data

        public void parse( String completion, String trackingId, String eventType, String unLocode, String voyage ) throws ProcessHandlingEventException
        {
            logger.debug( "Received handling event registration attempt " );
            try
            {
                ParsedHandlingEventData parsedData = parser.parse( completion, trackingId, eventType, unLocode, voyage );
                logger.debug( "Parsed handling event data" );

                register( parsedData );
            }
            catch (InvalidHandlingEventDataException e)
            {
                logger.info( e.getMessage() ); // Notify handling authority...
                throw new ProcessHandlingEventException( e.getMessage() );
            }
        }


        // Step 2 - Register handling event

        public void register( ParsedHandlingEventData parsedHandlingEventData ) throws ProcessHandlingEventException
        {
            try
            {
                HandlingEvent handlingEvent = new RegisterHandlingEvent( parsedHandlingEventData ).getEvent();
                logger.debug( "Registered handling event" );

                inspect( handlingEvent );
            }
            catch (CannotRegisterHandlingEventException e)
            {
                logger.info( e.getMessage() ); // Notify handling authority...
                throw new ProcessHandlingEventException( e.getMessage() );
            }
        }


        // Step 3 - Update cargo delivery status

        public void inspect( HandlingEvent handlingEvent )
        {
            try
            {
                new InspectCargoDeliveryStatus( handlingEvent ).update();
                logger.info( "Inspected handled cargo '" + handlingEvent.trackingId().get().id().get() + "'." );
            }
            catch (InspectionFailedException e)
            {
                logger.error( e.getMessage() ); // Unexpected error
            }
            catch (CargoArrivedException e)
            {
                logger.info( e.getMessage() );  // Request Cargo Owner to claim cargo at destination...
            }
            catch (InspectionException e)
            {
                logger.info( e.getMessage() );  // Request Cargo Owner to re-route cargo...
            }
        }
    }
}
