package org.qi4j.entitystore.hazelcast;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.entitystore.map.MapEntityStoreMixin;
import org.qi4j.library.locking.LockingAbstractComposite;
import org.qi4j.spi.entitystore.ConcurrentModificationCheckConcern;
import org.qi4j.spi.entitystore.EntityStateVersions;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.StateChangeNotificationConcern;

/**
 * @author Paul Merlin <paul@nosphere.org>
 */
@Concerns( { StateChangeNotificationConcern.class, ConcurrentModificationCheckConcern.class } )
@Mixins( { MapEntityStoreMixin.class, HazelcastEntityStoreMixin.class } )
public interface HazelcastEntityStoreService
    extends EntityStore,
            EntityStateVersions,
            ServiceComposite,
            Activatable,
            LockingAbstractComposite,
            Configuration
{
}
