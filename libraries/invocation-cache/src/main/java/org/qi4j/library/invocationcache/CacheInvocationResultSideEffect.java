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
package org.qi4j.library.invocationcache;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.sideeffect.GenericSideEffect;

/**
 * Cache result of @Cached method calls.
 */
@AppliesTo( Cached.class )
public class CacheInvocationResultSideEffect
    extends GenericSideEffect
{
    @This
    private InvocationCache cache;
    @Invocation
    private Method method;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        // Get value
        // if an exception is thrown, don't do anything
        Object res = result.invoke( proxy, method, args );
        if( res == null )
        {
            res = Void.TYPE;
        }
        String cacheName = method.getName();
        if( args != null )
        {
            cacheName += Arrays.asList( args );
        }
        Object oldResult = cache.cachedValue( cacheName );
        if( oldResult == null || !oldResult.equals( result ) )
        {
            cache.setCachedValue( cacheName, result );
        }
        return result;
    }
}
