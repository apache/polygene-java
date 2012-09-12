package org.qi4j.entitystore.redis;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.library.locking.LockingAbstractComposite;
import org.qi4j.spi.entitystore.ConcurrentModificationCheckConcern;
import org.qi4j.spi.entitystore.EntityStateVersions;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.StateChangeNotificationConcern;
import org.qi4j.spi.entitystore.helpers.MapEntityStoreMixin;
import redis.clients.jedis.JedisPool;

@Concerns( { StateChangeNotificationConcern.class, ConcurrentModificationCheckConcern.class } )
@Mixins( { MapEntityStoreMixin.class, RedisMapEntityStoreMixin.class } )
public interface RedisMapEntityStoreService
        extends EntityStore,
                EntityStateVersions,
                ServiceComposite,
                ServiceActivation,
                LockingAbstractComposite,
                Configuration
{

    JedisPool jedisPool();

}
