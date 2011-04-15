package org.qi4j.samples.dddsample.spring.ui.tracking;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.qi4j.api.query.Query;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.DeliveryHistory;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovement;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.springframework.context.MessageSource;

/**
 * View adapter for displaying a cargo in a tracking context.
 */
public final class CargoTrackingViewAdapter
{

    private final Cargo cargo;
    private final MessageSource messageSource;
    private final Locale locale;
    private final List<HandlingEventViewAdapter> events;

    CargoTrackingViewAdapter( Cargo aCargo, MessageSource aMessageSource, Locale aLocale )
    {
        this.messageSource = aMessageSource;
        this.locale = aLocale;
        this.cargo = aCargo;

        Query<HandlingEvent> handlingEvents = ( (DeliveryHistory) aCargo ).eventsOrderedByCompletionTime();
        events = new ArrayList<HandlingEventViewAdapter>( (int) handlingEvents.count() );
        for( HandlingEvent handlingEvent : handlingEvents )
        {
            events.add( new HandlingEventViewAdapter( handlingEvent ) );
        }
    }

    /**
     * @param location a location
     *
     * @return A formatted string for displaying the location.
     */
    private String getDisplayText( Location location )
    {
        return location.unLocode().idString() + " (" + location.name() + ")";
    }

    /**
     * @return An unmodifiable list of handling event view adapters.
     */
    public List<HandlingEventViewAdapter> getEvents()
    {
        return Collections.unmodifiableList( events );
    }

    /**
     * @return A translated string describing the cargo status.
     */
    public String getStatusText()
    {
        final DeliveryHistory deliveryHistory = (DeliveryHistory) cargo;
        final String code = "cargo.status." + deliveryHistory.status().name();

        final Object[] args;
        switch( deliveryHistory.status() )
        {
        case IN_PORT:
            args = new Object[]{ getDisplayText( deliveryHistory.currentLocation() ) };
            break;
        case ONBOARD_CARRIER:
            args = new Object[]{ deliveryHistory.currentCarrierMovement().carrierMovementId().idString() };
            break;
        case CLAIMED:
        case NOT_RECEIVED:
        case UNKNOWN:
        default:
            args = null;
            break;
        }

        return messageSource.getMessage( code, args, "[Unknown status]", locale );
    }

    /**
     * @return Cargo destination location.
     */
    public String getDestination()
    {
        return getDisplayText( cargo.destination() );
    }

    /**
     * @return Cargo osigin location.
     */
    public String getOrigin()
    {
        return getDisplayText( cargo.origin() );
    }

    /**
     * @return Cargo tracking id.
     */
    public String getTrackingId()
    {
        return cargo.trackingId().idString();
    }

    /**
     * @return True if cargo is misdirected.
     */
    public boolean isMisdirected()
    {
        return cargo.isMisdirected();
    }

    /**
     * Handling event view adapter component.
     */
    public final class HandlingEventViewAdapter
    {

        private final HandlingEvent handlingEvent;
        private final String FORMAT = "yyyy-MM-dd hh:mm";

        /**
         * Constructor.
         *
         * @param handlingEvent handling event
         */
        public HandlingEventViewAdapter( HandlingEvent handlingEvent )
        {
            this.handlingEvent = handlingEvent;
        }

        /**
         * @return Location where the event occurred.
         */
        public String getLocation()
        {
            Location location = handlingEvent.location();
            return location.unLocode().idString();
        }

        /**
         * @return Time when the event was completed.
         */
        public String getTime()
        {
            return new SimpleDateFormat( FORMAT ).format( handlingEvent.completionTime() );
        }

        /**
         * @return Type of event.
         */
        public String getType()
        {
            return handlingEvent.eventType().toString();
        }

        /**
         * @return Carrier movement id, or empty string if not applicable.
         */
        public String getCarrierMovement()
        {
            final CarrierMovement cm = handlingEvent.carrierMovement();
            if( cm != null )
            {
                return cm.carrierMovementId().idString();
            }
            else
            {
                return "";
            }
        }

        /**
         * @return True if the event was expected, according to the cargo's itinerary.
         */
        public boolean isExpected()
        {
            return cargo.itinerary().isExpected( handlingEvent );
        }
    }
}