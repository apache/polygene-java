package org.qi4j.samples.cargo.app1.system.factories;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.samples.cargo.app1.model.cargo.Delivery;
import org.qi4j.samples.cargo.app1.model.cargo.Itinerary;
import org.qi4j.samples.cargo.app1.model.cargo.RouteSpecification;
import org.qi4j.samples.cargo.app1.model.handling.HandlingEvent;
import org.qi4j.samples.cargo.app1.model.handling.HandlingHistory;

/**
 *
 */
@Mixins( DeliveryFactory.DeliveryFactoryMixin.class )
public interface DeliveryFactory extends ServiceComposite
{

    /**
     * Creates a new delivery snapshot based on the complete handling history of a cargo,
     * as well as its route specification and itinerary.
     *
     * @param routeSpecification route specification
     * @param itinerary          itinerary
     * @param handlingHistory    delivery history
     * @return An up to date delivery.
     */
    Delivery derivedFrom( RouteSpecification routeSpecification, Itinerary itinerary, HandlingHistory handlingHistory );

    public abstract class DeliveryFactoryMixin
        implements DeliveryFactory
    {

        @Structure
        private ValueBuilderFactory vbf;

        /**
         * Creates a new delivery snapshot based on the complete handling history of a cargo,
         * as well as its route specification and itinerary.
         *
         * @param routeSpecification route specification
         * @param itinerary          itinerary
         * @param handlingHistory    delivery history
         * @return An up to date delivery.
         */
        public Delivery derivedFrom( RouteSpecification routeSpecification, Itinerary itinerary, HandlingHistory handlingHistory )
        {
            HandlingEvent lastEvent = handlingHistory.mostRecentlyCompletedEvent();
            ValueBuilder<Delivery> builder = vbf.newValueBuilder( Delivery.class );
            final Delivery.State prototype = builder.prototypeFor( Delivery.State.class );
            prototype.lastHandlingEventIdentity().set( lastEvent.identity().get() );
            prototype.routeSpecification().set( routeSpecification );
            prototype.itinerary().set( itinerary );
            return builder.newInstance();
        }
    }
}