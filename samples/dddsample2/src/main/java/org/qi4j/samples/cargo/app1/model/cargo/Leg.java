package org.qi4j.samples.cargo.app1.model.cargo;

import java.util.Date;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.model.voyage.Voyage;
import org.qi4j.samples.cargo.app1.system.repositories.LocationRepository;
import org.qi4j.samples.cargo.app1.system.repositories.VoyageRepository;

/**
 *
 */
@Mixins( Leg.LegMixin.class )
public interface Leg extends ValueComposite
{

    Location loadLocation();

    Location unloadLocation();

    Voyage voyage();

    Date loadTime();

    Date unloadTime();

    interface State
    {

        Property<String> loadLocationUnLocodeIdentity();

        Property<String> unloadLocationUnLocodeIdentity();

        Property<String> voyageIdentity();

        Property<Date> loadTime();

        Property<Date> unloadTime();
    }

    public abstract class LegMixin
        implements Leg
    {
        @This
        private State state;

        @Service
        private LocationRepository locationRepository;

        @Service
        private VoyageRepository voyageRepository;

        public Location loadLocation()
        {
            return locationRepository.findLocationByUnLocode( state.loadLocationUnLocodeIdentity().get() );
        }

        public Location unloadLocation()
        {
            return locationRepository.findLocationByUnLocode( state.unloadLocationUnLocodeIdentity().get() );
        }

        public Voyage voyage()
        {
            return voyageRepository.findVoyageByVoyageIdentity( state.voyageIdentity().get() );
        }

        public Date loadTime()
        {
            return state.loadTime().get();
        }

        public Date unloadTime()
        {
            return state.unloadTime().get();
        }
    }
}