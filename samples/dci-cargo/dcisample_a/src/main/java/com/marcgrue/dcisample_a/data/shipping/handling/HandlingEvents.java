package com.marcgrue.dcisample_a.data.shipping.handling;

import com.marcgrue.dcisample_a.data.shipping.location.Location;
import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import com.marcgrue.dcisample_a.data.shipping.cargo.TrackingId;
import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import java.util.Date;

/**
 * HandlingEvent "collection" - could have had a many-association to
 * Handling Events if it was part of the domain model.
 */
@Mixins( HandlingEvents.Mixin.class )
public interface HandlingEvents
{
    HandlingEvent createHandlingEvent( Date registrationTime,
                                       Date completionTime,
                                       TrackingId trackingId,
                                       HandlingEventType handlingEventType,
                                       Location location,
                                       @Optional Voyage voyage
    ) throws IllegalArgumentException;

    public abstract class Mixin
          implements HandlingEvents
    {
        @Structure
        UnitOfWorkFactory uowf;

        public HandlingEvent createHandlingEvent( Date registrationTime,
                                                  Date completionTime,
                                                  TrackingId trackingId,
                                                  HandlingEventType handlingEventType,
                                                  Location location,
                                                  Voyage voyage )
              throws IllegalArgumentException
        {
            if (voyage == null && handlingEventType.requiresVoyage())
                throw new IllegalArgumentException( "Voyage is required for handling event type " + handlingEventType );

            else if (voyage != null && handlingEventType.prohibitsVoyage())
                throw new IllegalArgumentException( "Voyage is not allowed with handling event type " + handlingEventType );

            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<HandlingEvent> handlingEventBuilder = uow.newEntityBuilder( HandlingEvent.class );
            handlingEventBuilder.instance().registrationTime().set( registrationTime );
            handlingEventBuilder.instance().completionTime().set( completionTime );
            handlingEventBuilder.instance().trackingId().set( trackingId );
            handlingEventBuilder.instance().handlingEventType().set( handlingEventType );
            handlingEventBuilder.instance().location().set( location );
            handlingEventBuilder.instance().voyage().set( voyage );  // could be null

            // Save and return
            return handlingEventBuilder.newInstance();
        }
    }
}
