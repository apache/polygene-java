/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.cache.ehcache;

import org.apache.polygene.spi.cache.Cache;

public class EhCacheImpl<T>
    implements Cache<T>
{
    private int refCount;
    private final org.ehcache.Cache backingCache;
    private final Class<T> valueType;
    private final String id;

    public EhCacheImpl( String cacheId, org.ehcache.Cache cache, Class<T> valueType )
    {
        this.id = cacheId;
        this.backingCache = cache;
        this.valueType = valueType;
    }

    @Override
    public T get( String key )
    {
        Object element = backingCache.get( key );
        if( element == null )
        {
            return null;
        }
        return valueType.cast( element );
    }

    @Override
    public T remove( String key )
    {
        T old = valueType.cast( backingCache.get( key ) );
        backingCache.remove( key );
        return old;
    }

    @Override
    public void put( String key, T value )
    {
        backingCache.put( key, value );
    }

    @Override
    public boolean exists( String key )
    {
        return backingCache.containsKey( key );
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
