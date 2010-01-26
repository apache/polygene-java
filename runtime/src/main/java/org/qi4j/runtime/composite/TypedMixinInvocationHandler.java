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
package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.sf.cglib.proxy.MethodProxy;
import org.qi4j.spi.composite.InvalidCompositeException;

/**
 * JAVADOC
 */
public final class TypedMixinInvocationHandler
    extends FragmentInvocationHandler
{
    private MethodProxy methodProxy;

    public TypedMixinInvocationHandler( MethodProxy methodProxy )
    {
        if( methodProxy == null )
        {
            throw new NullPointerException( "MethodProxy must not be null." );
        }
        this.methodProxy = methodProxy;
    }

    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        try
        {
            return methodProxy.invokeSuper( fragment, args );
        }
        catch( InvocationTargetException e )
        {
            throw cleanStackTrace( e.getTargetException(), proxy, method );
        }
        catch( Throwable e )
        {
            if( fragment == null )
            {
                throw new InvalidCompositeException();
            }
            throw cleanStackTrace( e, proxy, method );
        }
    }
}