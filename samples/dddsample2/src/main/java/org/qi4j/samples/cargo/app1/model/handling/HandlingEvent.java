package org.qi4j.samples.cargo.app1.model.handling;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.samples.cargo.app1.model.cargo.Cargo;
import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.model.voyage.Voyage;
import org.qi4j.samples.cargo.app1.system.types.DomainEvent;

import java.util.Date;

/**
 *
 */
@Mixins( HandlingEvent.HandlingEventMixin.class )
public interface HandlingEvent extends DomainEvent
{

    Type eventType();

    Voyage voyage();

    Location location();

    Date completionTime();

    Date registrationTime();

    Cargo cargo();

    interface State
    {
        Property<Type> eventType();

        Association<Voyage> voyage();

        Property<Location> location();

        Property<Date> completionTime();

        Property<Date> registrationTime();

        Association<Cargo> cargo();
    }

    /**
     * Handling event type. Either requires or prohibits a carrier movement
     * association, it's never optional.
     */
    public enum Type
    {
        LOAD( true ),
        UNLOAD( true ),
        RECEIVE( false ),
        CLAIM( false ),
        CUSTOMS( false );

        private final boolean voyageRequired;

        /**
         * Private enum constructor.
         *
         * @param voyageRequired whether or not a voyage is associated with this event type
         */
        private Type( final boolean voyageRequired )
        {
            this.voyageRequired = voyageRequired;
        }

        /**
         * @return True if a voyage association is required for this event type.
         */
        public boolean requiresVoyage()
        {
            return voyageRequired;
        }

        /**
         * @return True if a voyage association is prohibited for this event type.
         */
        public boolean prohibitsVoyage()
        {
            return !requiresVoyage();
        }
    }

    public static abstract class HandlingEventMixin
        implements HandlingEvent
    {
        @This
        private State state;

        public Type eventType()
        {
            return state.eventType().get();
        }

        public Voyage voyage()
        {
            return state.voyage().get();
        }

        public Location location()
        {
            return state.location().get();
        }

        public Date completionTime()
        {
            return state.completionTime().get();
        }

        public Date registrationTime()
        {
            return state.registrationTime().get();
        }

        public Cargo cargo()
        {
            return state.cargo().get();
        }
    }
}