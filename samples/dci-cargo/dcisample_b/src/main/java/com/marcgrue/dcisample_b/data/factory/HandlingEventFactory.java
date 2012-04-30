package com.marcgrue.dcisample_b.data.factory;

import com.marcgrue.dcisample_b.data.factory.exception.CannotCreateHandlingEventException;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType;
import com.marcgrue.dcisample_b.data.structure.location.Location;
import com.marcgrue.dcisample_b.data.structure.tracking.TrackingId;
import com.marcgrue.dcisample_b.data.structure.voyage.Voyage;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import java.util.Date;

/**
 * HandlingEventFactory
 *
 * Creates a valid handling event
 *
 * - Verifies if a voyage is mandatory or prohibited for the handling event type
 * - Verifies if a load/unload location is expected by the voyage
 *
 * Validations of TrackingId, Location and Voyage are considered out of this scope.
 */
@Mixins( HandlingEventFactory.Mixin.class )
public interface HandlingEventFactory
{
    HandlingEvent createHandlingEvent( Date registrationTime,
                                       Date completionTime,
                                       TrackingId trackingId,
                                       HandlingEventType handlingEventType,
                                       Location location,
                                       @Optional Voyage voyage
    ) throws CannotCreateHandlingEventException;

    public abstract class Mixin
          implements HandlingEventFactory
    {
        @Structure
        UnitOfWorkFactory uowf;

        public HandlingEvent createHandlingEvent( Date registrationTime,
                                                  Date completionTime,
                                                  TrackingId trackingId,
                                                  HandlingEventType handlingEventType,
                                                  Location location,
                                                   Voyage voyage )
              throws CannotCreateHandlingEventException
        {
            if (voyage == null && handlingEventType.requiresVoyage())
                throw new CannotCreateHandlingEventException( "Voyage is required for handling event type " + handlingEventType );

            else if (voyage != null && handlingEventType.prohibitsVoyage())
                throw new CannotCreateHandlingEventException( "Voyage is not allowed with handling event type " + handlingEventType );

            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<HandlingEvent> handlingEventBuilder = uow.newEntityBuilder( HandlingEvent.class );
            handlingEventBuilder.instance().registrationTime().set( registrationTime );
            handlingEventBuilder.instance().completionTime().set( completionTime );
            handlingEventBuilder.instance().trackingId().set( trackingId );
            handlingEventBuilder.instance().handlingEventType().set( handlingEventType );
            handlingEventBuilder.instance().location().set( location );
            handlingEventBuilder.instance().voyage().set( voyage );

            // Save and return
            return handlingEventBuilder.newInstance();
        }
    }
}
