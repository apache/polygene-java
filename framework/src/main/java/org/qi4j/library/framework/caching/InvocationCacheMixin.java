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
package org.qi4j.library.framework.caching;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.annotation.Assertions;
import org.qi4j.api.annotation.SideEffects;

/**
 * TODO
 */
@Assertions( ReturnInvocationCacheOnExceptionAssertion.class )
@SideEffects( CacheInvocationResultSideEffect.class )
public class InvocationCacheMixin
    implements InvocationCache
{
    // Attributes ----------------------------------------------------
    Map<String, Object> cachedValues = new ConcurrentHashMap<String, Object>();

    // InvocationCache implementation --------------------------------
    public void setCachedValue( String aName, Object aResult )
    {
        cachedValues.put( aName, aResult );
    }

    public Object getCachedValue( String aName )
    {
        return cachedValues.get( aName );
    }

    public Object removeCachedValue( String aName )
    {
        return cachedValues.remove( aName );
    }
}
