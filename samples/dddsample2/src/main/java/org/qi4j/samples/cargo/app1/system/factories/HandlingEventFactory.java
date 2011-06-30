package org.qi4j.samples.cargo.app1.system.factories;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.cargo.app1.model.cargo.Cargo;
import org.qi4j.samples.cargo.app1.model.handling.HandlingEvent;
import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.model.voyage.Voyage;

import java.util.Date;

/**
 *
 */
@Mixins( HandlingEventFactory.HandlingEventFactoryMixin.class )
public interface HandlingEventFactory extends ServiceComposite
{

    HandlingEvent create( Cargo cargo, HandlingEvent.Type eventType,
                          Voyage voyage, Location location,
                          Date completionTime, Date registrationTime );

    public abstract class HandlingEventFactoryMixin
        implements HandlingEventFactory
    {

        @Structure
        private UnitOfWorkFactory uowf;

        public HandlingEvent create( Cargo cargo, HandlingEvent.Type eventType,
                                     Voyage voyage, Location location,
                                     Date completionTime, Date registrationTime )
        {
            EntityBuilder<HandlingEvent> builder = uowf.newUnitOfWork().newEntityBuilder( HandlingEvent.class );
            HandlingEvent.State instance = builder.instanceFor( HandlingEvent.State.class );
            instance.cargo().set( cargo );
            instance.eventType().set( eventType );
            instance.voyage().set( voyage );
            instance.location().set( location );
            instance.completionTime().set( completionTime );
            instance.registrationTime().set( registrationTime );
            return builder.newInstance();
        }
    }
}