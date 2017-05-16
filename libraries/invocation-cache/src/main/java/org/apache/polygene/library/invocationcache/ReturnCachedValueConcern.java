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
package org.apache.polygene.library.invocationcache;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.apache.polygene.api.common.AppliesTo;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.injection.scope.This;

/**
 * Return value of @Cached calls if possible.
 */
@AppliesTo( Cached.class )
public class ReturnCachedValueConcern
    extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    @This @Optional
    private InvocationCache cache;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        boolean voidReturnType = method.getReturnType().equals( Void.TYPE );
        if( cache != null || voidReturnType )
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
                return result;
            }
        }
        // No cached value found or no InvocationCache defined - call method
        return next.invoke( proxy, method, args );
    }
}
