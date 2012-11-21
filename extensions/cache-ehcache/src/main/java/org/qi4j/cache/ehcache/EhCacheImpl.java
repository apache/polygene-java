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

    @Override
    public T get( String key )
    {
        Element element = backingCache.get( key );
        if( element == null )
        {
            return null;
        }
        return valueType.cast( element.getObjectValue() );
    }

    @Override
    public T remove( String key )
    {
        T old = valueType.cast( backingCache.get( key ).getObjectValue() );
        backingCache.remove( key );
        return old;
    }

    @Override
    public void put( String key, T value )
    {
        Element element = new Element( key, value );
        backingCache.put( element );
    }

    @Override
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
