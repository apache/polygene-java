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
package org.apache.polygene.runtime.composite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * JAVADOC
 */
public final class TypedModifierInvocationHandler
    extends FragmentInvocationHandler
{
    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        try
        {
            return this.method.invoke( fragment, args );
        }
        catch( InvocationTargetException e )
        {
            Throwable targetException = e.getTargetException();
            if( targetException instanceof IllegalAccessError )
            {
                // We get here if any of the return types or parameters are not public. This is probably due to
                // the _Stub class ends up in a different classpace than the original mixin. We intend to fix this in
                // 3.1 or 3.2
                if( !Modifier.isPublic( method.getReturnType().getModifiers() ) )
                {
                    String message = "Return types must be public: " + method.getReturnType().getName();
                    IllegalAccessException illegalAccessException = new IllegalAccessException( message );
                    illegalAccessException.initCause( e.getTargetException() );
                    throw cleanStackTrace( illegalAccessException, proxy, method );
                }
            }
            throw cleanStackTrace( targetException, proxy, method );
        }
        catch( Throwable e )
        {
            throw cleanStackTrace( e, proxy, method );
        }
    }
}