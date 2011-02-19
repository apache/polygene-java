package org.qi4j.samples.cargo.app1.system.factories;

import org.qi4j.samples.cargo.app1.model.cargo.Leg;
import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.model.voyage.Voyage;
import java.util.Date;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;


/**
 *
 */
@Mixins(LegFactory.LegFactoryMixin.class)
public interface LegFactory extends ServiceComposite {

    Leg create(Voyage voyageByVoyageNumber, Location loadLocation, Location unloadLocation, Date loadDate, Date unloadDate);

    public static abstract class LegFactoryMixin
        implements LegFactory{

        @Structure
        private ValueBuilderFactory vbf;

        public Leg create(Voyage voyage,
                          Location loadLocation, Location unloadLocation,
                          Date loadDate, Date unloadDate) {
            ValueBuilder<Leg> builder = vbf.newValueBuilder(Leg.class);
            Leg.State prototype = builder.prototypeFor(Leg.State.class);
            prototype.voyageIdentity().set(voyage.voyageNumber().get().number().get());
            prototype.loadLocationUnLocodeIdentity().set(loadLocation.identity().get());
            prototype.unloadLocationUnLocodeIdentity().set(unloadLocation.identity().get());
            prototype.loadTime().set(loadDate);
            prototype.unloadTime().set(unloadDate);
            return builder.newInstance();
        }
    }
}