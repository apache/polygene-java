/*
 * Copyright 2007 Rickard Öberg.
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
package org.apache.zest.library.invocationcache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple implementation of InvocationCache.
 * <p>
 * This {@link InvocationCache} should typically not be used at all, and only serves as an
 * example. The @{code cachedValues} member is never emptied, so it constitutes a memory leak
 * if the method arguments keep changing.
 * </p>
 * <p>
 * <b>IMPORTANT: Only use this is you know that only a small set of arguments are used into your
 * method(s).</b>
 * </p>
 */
public class SimpleInvocationCacheMixin
    implements InvocationCache
{
    private final Map<String, Object> cachedValues = new ConcurrentHashMap<>();

    @Override
    public Object setCachedValue( String name, Object aResult )
    {
        return cachedValues.put( name, aResult );
    }

    @Override
    public Object cachedValue( String name )
    {
        return cachedValues.get( name );
    }

    @Override
    public Object removeCachedValue( String name )
    {
        return cachedValues.remove( name );
    }

    @Override
    public void clearCachedValues()
    {
        cachedValues.clear();
    }

    @Override
    public int currentCacheSize()
    {
        return cachedValues.size();
    }
}
