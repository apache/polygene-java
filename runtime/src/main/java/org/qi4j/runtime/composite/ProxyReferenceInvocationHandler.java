/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Collections;
import org.qi4j.composite.ConstraintViolation;
import org.qi4j.composite.InvocationContext;

public final class ProxyReferenceInvocationHandler
    implements InvocationHandler, InvocationContext
{
    private Object proxy;
    private Object mixin;
    private Class mixinType;
    private Collection<ConstraintViolation> constraintViolations;

    public Object composite()
    {
        return proxy;
    }

    public Object mixin()
    {
        return mixin;
    }

    public Class mixinType()
    {
        return mixinType;
    }


    public Collection<ConstraintViolation> constraintViolations()
    {
        if( constraintViolations == null )
        {
            return Collections.emptyList();
        }
        else
        {
            return constraintViolations;
        }
    }

    public void setContext( Object aProxy, Object aMixin, Class mixinType )
    {
        this.mixinType = mixinType;
        proxy = aProxy;
        mixin = aMixin;
    }

    public void setConstraintViolations( Collection<ConstraintViolation> constraintViolations )
    {
        this.constraintViolations = constraintViolations;
    }

    public void clearContext()
    {
        this.mixinType = null;
        proxy = null;
        mixin = null;
        constraintViolations = null;
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        try
        {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler( this.proxy );
            return invocationHandler.invoke( this.proxy, method, args );
        }
        catch( InvocationTargetException e )
        {
            throw e.getTargetException();
        }
        catch( UndeclaredThrowableException e )
        {
            throw e.getUndeclaredThrowable();
        }
    }
}
