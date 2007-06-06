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
package org.qi4j.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * TODO
 *
 * @author rickard
 * @version $Revision: 1.0 $
 */
public class FragmentInvocationHandler
    implements InvocationHandler
{
    // Attributes ----------------------------------------------------
    Object fragment;

    // Constructors --------------------------------------------------
    public FragmentInvocationHandler()
    {
    }

    public FragmentInvocationHandler( Object aFragment )
    {
        this.fragment= aFragment;
    }

    public void setFragment( Object fragment )
    {
        this.fragment = fragment;
    }

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        if (fragment instanceof InvocationHandler)
            return ((InvocationHandler)fragment).invoke( proxy, method, args);
        else
        {
            try
            {
                return method.invoke( fragment, args);
            }
            catch( InvocationTargetException e )
            {
                throw e.getTargetException();
            }
        }
    }
}
