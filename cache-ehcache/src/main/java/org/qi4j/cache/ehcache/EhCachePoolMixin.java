package org.qi4j.cache.ehcache;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.TerracottaConfigConfiguration;
import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.spi.cache.Cache;
import org.qi4j.spi.cache.CachePool;

public class EhCachePoolMixin
    implements CachePool, Activatable
{
    private ConcurrentHashMap<String, EhCacheImpl> caches;

    @Optional
    @This
    private Configuration<EhCacheConfiguration> config;

    private CacheManager cacheManager;

    public EhCachePoolMixin()
    {
        caches = new ConcurrentHashMap<String, EhCacheImpl>();
    }

    public <T> Cache<T> fetchCache( String cacheId, Class<T> valueType )
    {
        // Note: Small bug in Ehcache; If the cache name is an empty String it will actually work until
        //       you try to remove the Cache instance from the CacheManager, at which point it is silently
        //       ignored but not removed so there is an follow up problem of too much in the CacheManager.
        NullArgumentException.validateNotEmpty( "cacheId", cacheId );
        EhCacheImpl<T> cache = caches.get( cacheId );
        if( cache == null )
        {
            cache = createNewCache( cacheId, valueType );
            caches.put( cacheId, cache );
        }
        cache.incRefCount();
        return cache;
    }

    private <T> EhCacheImpl<T> createNewCache( String cacheId, Class<T> valueType )
    {
        CacheConfiguration cc = createCacheConfiguration( cacheId );

        // TODO: We also need all the other Configurations that are possible, like cacheLoaderFactoryConfiguration
        net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache( cc );
        cacheManager.addCache( cache );

        return new EhCacheImpl<T>( cacheId, cache, valueType );
    }

    public void returnCache( Cache cache )
    {
        EhCacheImpl eh = (EhCacheImpl) cache;
        eh.decRefCount();
        if( eh.isNotUsed() )
        {
            caches.remove( eh.cacheId() );
            cacheManager.removeCache( eh.cacheId() );
        }
    }

    public void activate()
        throws Exception
    {
        net.sf.ehcache.config.Configuration configuration = new net.sf.ehcache.config.Configuration();
        EhCacheConfiguration conf = config.configuration();
        Boolean updateCheck = conf.updateCheck().get();
        if( updateCheck != null )
        {
            configuration.setUpdateCheck( updateCheck );
        }
        configuration.setDynamicConfig( true );
        String monitoring = conf.monitoring().get();
        if( monitoring != null )
        {
            configuration.setMonitoring( monitoring );
        }
        String name = conf.cacheManagerName().get();
        if( name == null )
        {
            name = "Qi4j Cache Extension";
        }
        configuration.setName( name );
        List<String> terracottaConfigURLs = conf.terracottaConfigURLs().get();
        if( terracottaConfigURLs != null )
        {
            for( String terracottaURL : terracottaConfigURLs )
            {
                TerracottaConfigConfiguration terracottaConfig = new TerracottaConfigConfiguration();
                terracottaConfig.setUrl( terracottaURL );
                configuration.addTerracottaConfig( terracottaConfig );
            }
        }
        CacheConfiguration cc = createCacheConfiguration( "qi4j.ehcache.config.default" );
        configuration.setDefaultCacheConfiguration( cc );
        cacheManager = new CacheManager( configuration );
    }

    public void passivate()
        throws Exception
    {
        cacheManager.shutdown();
    }

    private CacheConfiguration createCacheConfiguration( String cacheId )
    {
        EhCacheConfiguration conf = config.configuration();
        Integer maxElementsInMemory = conf.maxElementsInMemory().get();
        if( maxElementsInMemory == null )
        {
            maxElementsInMemory = 1000;
        }
        CacheConfiguration cc = new CacheConfiguration( cacheId, maxElementsInMemory );
        String transactionalMode = conf.transactionalMode().get();
        if( transactionalMode != null )
        {
            cc.transactionalMode( transactionalMode );
        }

        Long timeToLiveSeconds = conf.timeToLiveSeconds().get();
        if( timeToLiveSeconds != null )
        {
            cc.timeToLiveSeconds( timeToLiveSeconds );
        }

        Long timeToIdleSeconds = conf.timeToIdleSeconds().get();
        if( timeToIdleSeconds != null )
        {
            cc.timeToIdleSeconds( timeToIdleSeconds );
        }

        Boolean overflowToDisk = conf.overflowToDisk().get();
        if( overflowToDisk != null )
        {
            cc.overflowToDisk( overflowToDisk );
        }
        String name = conf.name().get();
        if( name != null )

        {
            cc.name( name );
        }
        String memoryStoreEvictionPolicy = conf.memoryStoreEvictionPolicy().get();
        if( memoryStoreEvictionPolicy != null )
        {
            cc.memoryStoreEvictionPolicy( memoryStoreEvictionPolicy );
        }
        Integer maxElementsOnDisk = conf.maxElementsOnDisk().get();
        if( maxElementsOnDisk != null )
        {
            cc.maxElementsOnDisk( maxElementsOnDisk );
        }
//        Boolean loggingEnabled = conf.loggingEnabled().get();
//        if( loggingEnabled != null )
//        {
//            cc.loggingEnabled( loggingEnabled );
//        }
        Boolean eternal = conf.eternal().get();
        if( eternal != null )
        {
            cc.eternal( eternal );
        }
        String diskStorePath = conf.diskStorePath().get();
        if( diskStorePath != null )
        {
            cc.diskStorePath( diskStorePath );
        }
        Integer diskSpoolBufferSizeMB = conf.diskSpoolBufferSizeMB().get();
        if( diskSpoolBufferSizeMB != null )
        {
            cc.diskSpoolBufferSizeMB( diskSpoolBufferSizeMB );
        }
        Boolean diskPersistent = conf.diskPersistent().get();
        if( diskPersistent != null )
        {
            cc.diskPersistent( diskPersistent );
        }
        Long diskExpiryThreadIntervalSeconds = conf.diskExpiryThreadIntervalSeconds().get();
        if( diskExpiryThreadIntervalSeconds != null )
        {
            cc.diskExpiryThreadIntervalSeconds( diskExpiryThreadIntervalSeconds );
        }
        Integer diskAccessStripes = conf.diskAccessStripes().get();
        if( diskAccessStripes != null )
        {
            cc.diskAccessStripes( diskAccessStripes );
        }
        Boolean clearOnFlush = conf.clearOnFlush().get();
        if( clearOnFlush != null )
        {
            cc.clearOnFlush( clearOnFlush );
        }
        return cc;
    }
}
