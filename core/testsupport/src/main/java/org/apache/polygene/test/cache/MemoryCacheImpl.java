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
package org.apache.polygene.test.cache;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.polygene.spi.cache.Cache;

/**
 * In-Memory Cache implementation based on ConcurrentHashMap.
 */
public class MemoryCacheImpl<T>
    implements Cache<T>
{
    private int refCount;

    private final ConcurrentHashMap<String, Object> backingCache;
    private final Class<T> valueType;
    private final String id;

    private int gets;
    private int removes;
    private int puts;
    private int exists;

    public MemoryCacheImpl( String cacheId, ConcurrentHashMap<String, Object> cache, Class<T> valueType )
    {
        this.id = cacheId;
        this.backingCache = cache;
        this.valueType = valueType;
    }

    @Override
    public T get( String key )
    {
        try
        {
            return valueType.cast( backingCache.get( key ) );
        }
        finally
        {
            gets++;
        }
    }

    @Override
    public T remove( String key )
    {
        try
        {
            return valueType.cast( backingCache.remove( key ) );
        }
        finally
        {
            removes++;
        }
    }

    @Override
    public void put( String key, T value )
    {
        try
        {
            backingCache.put( key, value );
        }
        finally
        {
            puts++;
        }
    }

    @Override
    public boolean exists( String key )
    {
        try
        {
            return backingCache.containsKey( key );
        }
        finally
        {
            exists++;
        }
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

    public int size()
    {
        return backingCache.size();
    }

    public int gets()
    {
        return gets;
    }

    public int removes()
    {
        return removes;
    }

    public int puts()
    {
        return puts;
    }

    public int exists()
    {
        return exists;
    }
}
