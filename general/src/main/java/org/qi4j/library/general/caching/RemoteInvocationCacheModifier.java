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
package org.qi4j.library.general.caching;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.util.Arrays;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.Dependency;
import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.Uses;

/**
 * Cache for remote calls. Cache values
 * of remote invocations and if an IOException
 * occurs, try to reuse a previous result.
 *
 * @author rickard
 * @version $Revision: 1.0 $
 */
@AppliesTo( Remote.class)
public class RemoteInvocationCacheModifier
    implements InvocationHandler
{
    // Attributes ----------------------------------------------------
    @Uses InvocationCache cache;
    @Dependency Method method;
    @Modifies InvocationHandler next;

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        String cacheName = method.getName() + Arrays.asList( args );
        try
        {
            Object result = next.invoke( proxy, method, args);
            cache.set( cacheName, result);
            return result;
        } catch ( IOException e)
        {
            Object result = cache.get(cacheName);
            if (result != null)
                return result;

            throw e;
        } catch (Throwable e)
        {
            throw e;
        }
    }
}
