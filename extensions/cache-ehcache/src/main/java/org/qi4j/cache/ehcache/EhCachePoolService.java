package org.qi4j.cache.ehcache;

import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.spi.cache.CachePool;

@Mixins( EhCachePoolMixin.class )
@Activators( EhCachePoolService.Activator.class )
public interface EhCachePoolService
        extends CachePool, ServiceComposite
{

    void activateCache()
            throws Exception;

    void passivateCache()
            throws Exception;

    class Activator
            extends ActivatorAdapter<ServiceReference<EhCachePoolService>>
    {

        @Override
        public void afterActivation( ServiceReference<EhCachePoolService> activated )
                throws Exception
        {
            activated.get().activateCache();
        }

        @Override
        public void beforePassivation( ServiceReference<EhCachePoolService> passivating )
                throws Exception
        {
            passivating.get().passivateCache();
        }

    }

}
