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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.api.injection.scope.This;

/**
 * Return value of @Cached calls if possible.
 */
@AppliesTo( Cached.class )
public class ReturnCachedValueConcern
    extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    @This
    private InvocationCache cache;
    @Invocation
    private Method method;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        // Try cache
        String cacheName = method.getName();
        if( args != null )
        {
            cacheName += Arrays.asList( args );
        }
        Object result = cache.cachedValue( cacheName );
        if( result != null )
        {
            if( result == Void.TYPE )
            {
                return null;
            }
            else
            {
                return result;
            }
        }
        // No cached value found - call method
        return next.invoke( proxy, method, args );
    }
}
