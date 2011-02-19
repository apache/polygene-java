package org.qi4j.samples.cargo.app1.system.factories;

import org.qi4j.samples.cargo.app1.model.cargo.HandlingActivity;
import org.qi4j.samples.cargo.app1.model.handling.HandlingEvent;
import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.model.voyage.Voyage;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;


/**
 *
 */
@Mixins( HandlingActivityFactory.HandlingActivityFactoryMixin.class)
public interface HandlingActivityFactory extends ServiceComposite {

    HandlingActivity create(HandlingEvent.Type claim, Location location);

    HandlingActivity create(HandlingEvent.Type load, Location location, Voyage voyage);

    public static abstract class HandlingActivityFactoryMixin
        implements HandlingActivityFactory
    {
        @Structure
        private ValueBuilderFactory vbf;

        public HandlingActivity create(final HandlingEvent.Type type, final Location location) {
            ValueBuilder<HandlingActivity> builder = vbf.newValueBuilder( HandlingActivity.class );
            HandlingActivity prototype = createPrototype(builder, type, location);
            return builder.newInstance();
        }

        public HandlingActivity create(final HandlingEvent.Type type, final Location location, final Voyage voyage) {
            ValueBuilder<HandlingActivity> builder = vbf.newValueBuilder( HandlingActivity.class );
            HandlingActivity prototype = createPrototype(builder, type, location);
            prototype.voyage().set( voyage );
            return builder.newInstance();
        }

        private HandlingActivity createPrototype(final ValueBuilder<HandlingActivity> builder, final HandlingEvent.Type type, final Location location) {
            HandlingActivity prototype = builder.prototype();
            prototype.locationUnLocodeIdentity().set( location.identity().get() );
            prototype.handlingEventType().set( type );
            return prototype;
        }
    }
}