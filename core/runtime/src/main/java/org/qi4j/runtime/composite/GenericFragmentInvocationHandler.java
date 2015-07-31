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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * JAVADOC
 */
public final class GenericFragmentInvocationHandler
    extends FragmentInvocationHandler
{
    // InvocationHandler implementation ------------------------------

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        try
        {
            return ( (InvocationHandler) fragment ).invoke( proxy, method, args );
        }
        catch( InvocationTargetException throwable )
        {
            throw cleanStackTrace( throwable.getTargetException(), proxy, method );
        }
        catch( Throwable throwable )
        {
            throw cleanStackTrace( throwable, proxy, method );
        }
    }
}