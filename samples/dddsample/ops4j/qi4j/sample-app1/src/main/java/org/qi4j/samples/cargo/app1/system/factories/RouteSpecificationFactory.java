package org.qi4j.samples.cargo.app1.system.factories;

import org.qi4j.samples.cargo.app1.model.cargo.RouteSpecification;
import org.qi4j.samples.cargo.app1.model.location.Location;
import java.util.Date;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;


/**
 *
 */
@Mixins( RouteSpecificationFactory.RouteSpecificationFactoryMixin.class )
public interface RouteSpecificationFactory extends ServiceComposite {

    RouteSpecification create(Location origin, Location destination, Date arrivalDeadline);

    public static abstract class RouteSpecificationFactoryMixin
            implements RouteSpecificationFactory {
        @Structure
        private ValueBuilderFactory vbf;

        public RouteSpecification create(final Location origin, final Location destination, final Date arrivalDeadline) {
            ValueBuilder<RouteSpecification> builder = vbf.newValueBuilder(RouteSpecification.class);
            RouteSpecification.State prototypeState = builder.prototypeFor(RouteSpecification.State.class);
            prototypeState.arrivalDeadline().set(arrivalDeadline);
            prototypeState.originUnLocodeIdentity().set(origin.identity().get());
            prototypeState.destinationUnLocodeIdentity().set(destination.identity().get());
            return builder.newInstance();
        }
    }
}