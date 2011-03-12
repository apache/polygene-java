package org.qi4j.cache.ehcache;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.cache.CachePool;

@Mixins( EhCachePoolMixin.class )
public interface EhCachePoolService
    extends CachePool, ServiceComposite
{
}
