package org.qi4j.cache.ehcache;

import net.sf.ehcache.Element;
import org.qi4j.spi.cache.Cache;

public class EhCacheImpl<T>
    implements Cache<T>
{
    private int refCount;
    private final net.sf.ehcache.Cache backingCache;
    private final Class<T> valueType;
    private final String id;

    public EhCacheImpl( String cacheId, net.sf.ehcache.Cache cache, Class<T> valueType )
    {
        this.id = cacheId;
        this.backingCache = cache;
        this.valueType = valueType;
    }

    public T get( String key )
    {
        Element element = backingCache.get( key );
        if( element == null )
        {
            return null;
        }
        return valueType.cast( element.getValue() );
    }

    public T remove( String key )
    {
        T old = valueType.cast( backingCache.get( key ).getValue() );
        backingCache.remove( key );
        return old;
    }

    public void put( String key, T value )
    {
        Element element = new Element( key, value );
        backingCache.put( element );
    }

    public boolean exists( String key )
    {
        return backingCache.isKeyInCache( key );
    }

    synchronized void decRefCount()
    {
        refCount--;
    }

    synchronized void incRefCount()
    {
        refCount++;
    }

    synchronized boolean isNotUsed()
    {
        return refCount == 0;
    }

    public String cacheId()
    {
        return id;
    }
}
