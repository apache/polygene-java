/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling;

import org.apache.zest.api.common.Optional;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.inspection.InspectCargoDeliveryStatus;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.CargoArrivedException;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.InspectionException;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.InspectionFailedException;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.parsing.ParseHandlingEventData;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.parsing.exception.InvalidHandlingEventDataException;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.registration.RegisterHandlingEvent;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.registration.exception.CannotRegisterHandlingEventException;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;
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
 * 3. Delivery Inspector in {@link org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.inspection.InspectCargoDeliveryStatus}
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

    void parse( String completion, String trackingId, String eventType, String unLocode, @Optional String voyage )
        throws ProcessHandlingEventException;

    void register( ParsedHandlingEventData parsedHandlingEventData )
        throws ProcessHandlingEventException;

    void inspect( HandlingEvent handlingEvent );

    abstract class SynchronousProcessingStub
        implements ProcessHandlingEvent
    {
        Logger logger = LoggerFactory.getLogger( ProcessHandlingEvent.class );

        @Service
        ParseHandlingEventData parser;

        // Step 1 - Parse handling event data

        public void parse( String completion, String trackingId, String eventType, String unLocode, String voyage )
            throws ProcessHandlingEventException
        {
            logger.debug( "Received handling event registration attempt " );
            try
            {
                ParsedHandlingEventData parsedData = parser.parse( completion, trackingId, eventType, unLocode, voyage );
                logger.debug( "Parsed handling event data" );

                register( parsedData );
            }
            catch( InvalidHandlingEventDataException e )
            {
                logger.info( e.getMessage() ); // Notify handling authority...
                throw new ProcessHandlingEventException( e.getMessage() );
            }
        }

        // Step 2 - Register handling event

        public void register( ParsedHandlingEventData parsedHandlingEventData )
            throws ProcessHandlingEventException
        {
            try
            {
                HandlingEvent handlingEvent = new RegisterHandlingEvent( parsedHandlingEventData ).getEvent();
                logger.debug( "Registered handling event" );

                inspect( handlingEvent );
            }
            catch( CannotRegisterHandlingEventException e )
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
            catch( InspectionFailedException e )
            {
                logger.error( e.getMessage() ); // Unexpected error
            }
            catch( CargoArrivedException e )
            {
                logger.info( e.getMessage() );  // Request Cargo Owner to claim cargo at destination...
            }
            catch( InspectionException e )
            {
                logger.info( e.getMessage() );  // Request Cargo Owner to re-route cargo...
            }
        }
    }
}
