/*
 * Copyright 2010 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.cache.ehcache;

import java.util.concurrent.ConcurrentHashMap;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.spi.cache.Cache;

public abstract class EhCachePoolMixin
    implements EhCachePoolService
{

    private final ConcurrentHashMap<String, EhCacheImpl<?>> caches = new ConcurrentHashMap<>();
    @This @Optional
    private Configuration<EhCacheConfiguration> config;
    private CacheManager cacheManager;

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> Cache<T> fetchCache( String cacheId, Class<T> valueType )
    {
        // Note: Small bug in Ehcache; If the cache name is an empty String it will actually work until
        //       you try to remove the Cache instance from the CacheManager, at which point it is silently
        //       ignored but not removed so there is an follow up problem of too much in the CacheManager.
        NullArgumentException.validateNotEmpty( "cacheId", cacheId );
        EhCacheImpl<?> cache = caches.get( cacheId );
        if( cache == null )
        {
            cache = createNewCache( cacheId, valueType );
            caches.put( cacheId, cache );
        }
        cache.incRefCount();
        return (Cache<T>) cache;
    }

    private <T> EhCacheImpl<T> createNewCache( String cacheId, Class<T> valueType )
    {
        CacheConfiguration cc = createCacheConfiguration( cacheId );

        // TODO: We also need all the other Configurations that are possible, like cacheLoaderFactoryConfiguration
        net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache( cc );
        cacheManager.addCache( cache );

        return new EhCacheImpl<>( cacheId, cache, valueType );
    }

    @Override
    public void returnCache( Cache<?> cache )
    {
        EhCacheImpl<?> eh = (EhCacheImpl<?>) cache;
        eh.decRefCount();
        if( eh.isNotUsed() )
        {
            caches.remove( eh.cacheId() );
            cacheManager.removeCache( eh.cacheId() );
        }
    }

    @Override
    public void activateService()
        throws Exception
    {
        net.sf.ehcache.config.Configuration configuration = new net.sf.ehcache.config.Configuration();
        configureEhCache( configuration );
        CacheConfiguration cc = createCacheConfiguration( "qi4j.ehcache.config.default" );
        configuration.setDefaultCacheConfiguration( cc );
        cacheManager = CacheManager.newInstance( configuration );
    }

    @Override
    public void passivateService()
        throws Exception
    {
        cacheManager.shutdown();
    }

    private void configureEhCache( net.sf.ehcache.config.Configuration configuration )
    {
        EhCacheConfiguration conf = config.get();
        Boolean updateCheck = conf.updateCheck().get();
        configuration.setUpdateCheck( updateCheck );
        configuration.setDynamicConfig( true );
        String monitoring = conf.monitoring().get().trim();
        if( monitoring.length() > 0 )
        {
            configuration.setMonitoring( monitoring );
        }
        String name = conf.cacheManagerName().get();
        if( name == null )
        {
            name = "Qi4j Cache Extension";
        }
        configuration.setName( name );
        String diskStorePath = conf.diskStorePath().get();
        if( diskStorePath.length() > 0 )
        {
            configuration.getDiskStoreConfiguration().path( diskStorePath );
        }
    }

    private CacheConfiguration createCacheConfiguration( String cacheId )
    {
        EhCacheConfiguration conf = config.get();
        Integer maxElementsInMemory = conf.maxElementsInMemory().get();
        if( maxElementsInMemory <= 0 )
        {
            maxElementsInMemory = 10000;
        }
        CacheConfiguration cacheConfig = new CacheConfiguration( cacheId, maxElementsInMemory );
        String transactionalMode = conf.transactionalMode().get();
        if( transactionalMode.length() > 0 )
        {
            cacheConfig.transactionalMode( transactionalMode );
        }

        Long timeToLiveSeconds = conf.timeToLiveSeconds().get();
        if( timeToLiveSeconds > 0 )
        {
            cacheConfig.timeToLiveSeconds( timeToLiveSeconds );
        }

        Long timeToIdleSeconds = conf.timeToIdleSeconds().get();
        if( timeToIdleSeconds > 0 )
        {
            cacheConfig.timeToIdleSeconds( timeToIdleSeconds );
        }

        String name = conf.name().get();
        if( name.length() > 0 )
        {
            cacheConfig.name( name );
        }

        String memoryStoreEvictionPolicy = conf.memoryStoreEvictionPolicy().get();
        if( memoryStoreEvictionPolicy.length() > 0 )
        {
            cacheConfig.memoryStoreEvictionPolicy( memoryStoreEvictionPolicy );
        }

        Integer maxElementsOnDisk = conf.maxElementsOnDisk().get();
        if( maxElementsOnDisk > 0 )
        {
            cacheConfig.maxElementsOnDisk( maxElementsOnDisk );
        }

        Boolean loggingEnabled = conf.loggingEnabled().get();
        if( loggingEnabled != null )
        {
            cacheConfig.logging( loggingEnabled );
        }

        Boolean eternal = conf.eternal().get();
        cacheConfig.eternal( eternal );

        Integer diskSpoolBufferSizeMB = conf.diskSpoolBufferSizeMB().get();
        if( diskSpoolBufferSizeMB > 0 )
        {
            cacheConfig.diskSpoolBufferSizeMB( diskSpoolBufferSizeMB );
        }

        Long diskExpiryThreadIntervalSeconds = conf.diskExpiryThreadIntervalSeconds().get();
        if( diskExpiryThreadIntervalSeconds > 0 )
        {
            cacheConfig.diskExpiryThreadIntervalSeconds( diskExpiryThreadIntervalSeconds );
        }

        Integer diskAccessStripes = conf.diskAccessStripes().get();
        if( diskAccessStripes > 0 )
        {
            cacheConfig.diskAccessStripes( diskAccessStripes );
        }
        Boolean clearOnFlush = conf.clearOnFlush().get();
        if( clearOnFlush != null )
        {
            cacheConfig.clearOnFlush( clearOnFlush );
        }

        // Persistence Configuration
        PersistenceConfiguration persistenceConfig = new PersistenceConfiguration();
        Strategy strategy = conf.persistenceStrategy().get();
        if( strategy == null )
        {
            persistenceConfig.strategy( Strategy.NONE );
        }
        else
        {
            persistenceConfig.strategy( strategy );
        }
        cacheConfig.persistence( persistenceConfig );

        return cacheConfig;
    }

}
