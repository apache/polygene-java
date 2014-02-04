/*
 * Copyright 2014 Paul Merlin.
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
package org.qi4j.cache.memcache;

import java.util.concurrent.atomic.AtomicInteger;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.SerializingTranscoder;
import org.qi4j.spi.cache.Cache;

/**
 * Memcache Implementation.
 * Use Java Serialization under the hood.
 * @param <T> Parameterized Type of cached entries
 */
/* package */ class MemcacheImpl<T>
    implements Cache<T>
{
    private static final AtomicInteger INSTANCES = new AtomicInteger();
    private final MemcachedClient client;
    private final String cacheId;
    private final String cachePrefix;
    private final Class<T> valueType;
    private final int expiration;
    private int refCount;

    /* package */ MemcacheImpl( MemcachedClient client, String cacheId, Class<T> valueType, int expiration )
    {
        this.client = client;
        this.cacheId = cacheId;
        this.cachePrefix = cacheId + "." + INSTANCES.incrementAndGet() + ".";
        this.valueType = valueType;
        this.expiration = expiration;
    }

    @Override
    public T get( String key )
    {
        Object value = client.get( prefix( key ), new SerializingTranscoder() );
        client.touch( prefix( key ), expiration );
        if( value == null )
        {
            return null;
        }
        return valueType.cast( value );
    }

    @Override
    public T remove( String key )
    {
        String prefixedKey = prefix( key );
        Object old = client.get( prefixedKey, new SerializingTranscoder() );
        if( old != null )
        {
            client.delete( prefixedKey );
        }
        return valueType.cast( old );
    }

    @Override
    public void put( String key, T value )
    {
        client.set( prefix( key ), expiration, value, new SerializingTranscoder() );
    }

    @Override
    public boolean exists( String key )
    {
        return client.get( prefix( key ) ) != null;
    }

    private String prefix( String key )
    {
        return cachePrefix + key;
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
        return cacheId;
    }
}
