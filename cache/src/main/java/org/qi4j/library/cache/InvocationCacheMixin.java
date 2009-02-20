/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.library.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JAVADOC
 */
public class InvocationCacheMixin
    implements InvocationCache
{
    Map<String, Object> cachedValues = new ConcurrentHashMap<String, Object>();

    public Object setCachedValue( String name, Object aResult )
    {
        return cachedValues.put( name, aResult );
    }

    public Object getCachedValue( String name )
    {
        return cachedValues.get( name );
    }

    public Object removeCachedValue( String name )
    {
        return cachedValues.remove( name );
    }

    public void clearCachedValues()
    {
        cachedValues.clear();
    }

    public int getCacheSize()
    {
        return cachedValues.size();
    }
}
