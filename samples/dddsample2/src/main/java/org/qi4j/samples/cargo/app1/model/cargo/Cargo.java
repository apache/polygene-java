package org.qi4j.samples.cargo.app1.model.cargo;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.samples.cargo.app1.model.handling.HandlingHistory;
import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.system.factories.DeliveryFactory;

/**
 * A Cargo. This is the central class in the domain model,
 * and it is the root of the Cargo-Itinerary-Leg-Delivery-RouteSpecification aggregate.
 * <p/>
 * A cargo is identified by a unique tracking id, and it always has an origin
 * and a route specification. The life cycle of a cargo begins with the booking procedure,
 * when the tracking id is assigned. During a (short) period of time, between booking
 * and initial routing, the cargo has no itinerary.
 * <p/>
 * The booking clerk requests a list of possible routes, matching the route specification,
 * and assigns the cargo to one route. The route to which a cargo is assigned is described
 * by an itinerary.
 * <p/>
 * A cargo can be re-routed during transport, on demand of the customer, in which case
 * a new route is specified for the cargo and a new route is requested. The old itinerary,
 * being a value object, is discarded and a new one is attached.
 * <p/>
 * It may also happen that a cargo is accidentally misrouted, which should notify the proper
 * personnel and also trigger a re-routing procedure.
 * <p/>
 * When a cargo is handled, the status of the delivery changes. Everything about the delivery
 * of the cargo is contained in the Delivery value object, which is replaced whenever a cargo
 * is handled by an asynchronous event triggered by the registration of the handling event.
 * <p/>
 * The delivery can also be affected by routing changes, i.e. when a the route specification
 * changes, or the cargo is assigned to a new route. In that case, the delivery update is performed
 * synchronously within the cargo aggregate.
 * <p/>
 * The life cycle of a cargo ends when the cargo is claimed by the customer.
 * <p/>
 * The cargo aggregate, and the entre domain model, is built to solve the problem
 * of booking and tracking cargo. All important business rules for determining whether
 * or not a cargo is misdirected, what the current status of the cargo is (on board carrier,
 * in port etc), are captured in this aggregate.
 */
@Mixins( Cargo.CargoMixin.class )
public interface Cargo
    extends EntityComposite
{

    TrackingId trackingId();

    Location origin();

    RouteSpecification routeSpecification();

    Itinerary itinerary();

    Delivery delivery();

    /**
     * Specifies a new route for this cargo.
     *
     * @param routeSpecification route specification.
     */
    void specifyNewRoute( final RouteSpecification routeSpecification );

    /**
     * Attach a new itinerary to this cargo.
     *
     * @param itinerary an itinerary. May not be null.
     */
    void assignToRoute( final Itinerary itinerary );

    /**
     * Updates all aspects of the cargo aggregate status
     * based on the current route specification, itinerary and handling of the cargo.
     * <p/>
     * When either of those three changes, i.e. when a new route is specified for the cargo,
     * the cargo is assigned to a route or when the cargo is handled, the status must be
     * re-calculated.
     * <p/>
     * {@link RouteSpecification} and {@link Itinerary} are both inside the Cargo
     * aggregate, so changes to them cause the status to be updated <b>synchronously</b>,
     * but changes to the delivery history (when a cargo is handled) cause the status update
     * to happen <b>asynchronously</b> since {@link org.qi4j.samples.cargo.app1.model.handling.HandlingEvent}
     * is in a different aggregate.
     *
     * @param handlingHistory handling history
     */
    void deriveDeliveryProgress( final HandlingHistory handlingHistory );

    interface State
    {
        Association<Location> origin();

        Property<RouteSpecification> routeSpecification();

        @Optional
        Property<Itinerary> itinerary();

        @Optional
        Property<Delivery> delivery();
    }

    public abstract class CargoMixin
        implements Cargo
    {

        public static final String TRACKING_ID_PREFIX = "TrackingId:";

        @This
        private State state;

        @Service
        private DeliveryFactory deliveryFactory;

        @Structure
        private ValueBuilderFactory vbf;

        private TrackingId trackingId;

        public TrackingId trackingId()
        {
            if( trackingId == null )
            {
                final String identity = identity().get();
                String id = identity.substring( TRACKING_ID_PREFIX.length() );
                final ValueBuilder<TrackingId> builder = vbf.newValueBuilder( TrackingId.class );
                builder.prototype().id().set( id );
                trackingId = builder.newInstance();
            }
            return trackingId;
        }

        public Location origin()
        {
            return state.origin().get();
        }

        public RouteSpecification routeSpecification()
        {
            return state.routeSpecification().get();
        }

        public Itinerary itinerary()
        {
            return state.itinerary().get();
        }

        public Delivery delivery()
        {
            return state.delivery().get();
        }

        public void specifyNewRoute( final RouteSpecification routeSpecification )
        {
            state.routeSpecification().set( routeSpecification );
            // Handling consistency within the Cargo aggregate synchronously
            state.delivery().set( delivery().updateOnRouting( routeSpecification(), itinerary() ) );
        }

        public void assignToRoute( final Itinerary itinerary )
        {
            state.itinerary().set( itinerary );
            // Handling consistency within the Cargo aggregate synchronously
            state.delivery().set( delivery().updateOnRouting( routeSpecification(), itinerary() ) );
        }

        /**
         * Updates all aspects of the cargo aggregate status
         * based on the current route specification, itinerary and handling of the cargo.
         * <p/>
         * When either of those three changes, i.e. when a new route is specified for the cargo,
         * the cargo is assigned to a route or when the cargo is handled, the status must be
         * re-calculated.
         * <p/>
         * {@link RouteSpecification} and {@link Itinerary} are both inside the Cargo
         * aggregate, so changes to them cause the status to be updated <b>synchronously</b>,
         * but changes to the delivery history (when a cargo is handled) cause the status update
         * to happen <b>asynchronously</b> since
         * {@link org.qi4j.samples.cargo.app1.model.handling.HandlingEvent} is in a different aggregate.
         *
         * @param handlingHistory handling history
         */
        public void deriveDeliveryProgress( final HandlingHistory handlingHistory )
        {
            // TODO filter events on cargo (must be same as this cargo)

            // Delivery is a value object, so we can simply discard the old one
            // and replace it with a new
            state.delivery().set( deliveryFactory.derivedFrom( routeSpecification(), itinerary(), handlingHistory ) );
        }
    }
}