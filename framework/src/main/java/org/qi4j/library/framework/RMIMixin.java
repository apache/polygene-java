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
package org.qi4j.library.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.qi4j.composite.AppliesTo;

/**
 * Generic mixin that looks up and invokes an object through RMI
 */
@AppliesTo( Remote.class )
public class RMIMixin
    implements InvocationHandler
{
    private Object remote;

    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        if( remote == null )
        {
            Registry registry = LocateRegistry.getRegistry( "localhost" );
            remote = registry.lookup( method.getDeclaringClass().getSimpleName() );
        }

        try
        {
            return method.invoke( remote, args );
        }
        catch( InvocationTargetException e )
        {
            remote = null;
            throw e.getTargetException();
        }
    }
}
