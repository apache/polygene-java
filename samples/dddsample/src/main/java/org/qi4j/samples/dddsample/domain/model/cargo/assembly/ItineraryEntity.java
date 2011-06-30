package org.qi4j.samples.dddsample.domain.model.cargo.assembly;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.Itinerary;
import org.qi4j.samples.dddsample.domain.model.cargo.Leg;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent;

import java.util.List;

/**
 * An itinerary.
 */
@Mixins( ItineraryEntity.ItineraryMixin.class )
interface ItineraryEntity
    extends Itinerary, EntityComposite
{
    void detachCargo();

    void changeCargo( Cargo newCargo );

    abstract class ItineraryMixin
        implements ItineraryEntity
    {
        @This
        private ItineraryState state;

        public Cargo cargo()
        {
            return state.cargo().get();
        }

        public List<Leg> legs()
        {
            return state.legs().toList();
        }

        public boolean isExpected( HandlingEvent event )
        {
            if( legs().size() == 0 )
            {
                return true;
            }

            if( event.eventType() == HandlingEvent.Type.RECEIVE )
            {
                //Check that the first leg's origin is the event's location
                final Leg leg = legs().get( 0 );
                return ( leg.from().equals( event.location() ) );
            }

            if( event.eventType() == HandlingEvent.Type.LOAD )
            {
                //Check that the there is one leg with same from location and carrier movement
                for( Leg leg : legs() )
                {
                    if( leg.from().equals( event.location() )
                        && leg.carrierMovement().equals( event.carrierMovement() ) )
                    {
                        return true;
                    }
                }
                return false;
            }

            if( event.eventType() == HandlingEvent.Type.UNLOAD )
            {
                //Check that the there is one leg with same to loc and carrier movement
                for( Leg leg : legs() )
                {
                    if( leg.to().equals( event.location() )
                        && leg.carrierMovement().equals( event.carrierMovement() ) )
                    {
                        return true;
                    }
                }
                return false;
            }

            if( event.eventType() == HandlingEvent.Type.CLAIM )
            {
                //Check that the last leg's destination is from the event's location
                final Leg leg = legs().get( legs().size() - 1 );
                return leg.to().equals( event.location() );
            }

            //HandlingEvent.Type.CUSTOMS;
            return true;
        }

        public boolean sameValueAs( Itinerary other )
        {
            return legs().equals( other.legs() );
        }

        public void detachCargo()
        {
            state.cargo().set( null );
        }

        public void changeCargo( Cargo newCargo )
        {
            state.cargo().set( newCargo );
        }
    }
}