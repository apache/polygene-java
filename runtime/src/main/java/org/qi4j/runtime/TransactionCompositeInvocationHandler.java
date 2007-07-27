/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.model.InvalidCompositeException;
import javax.transaction.xa.XAResource;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;

public class TransactionCompositeInvocationHandler extends CompositeInvocationHandler
{
    private WeakHashMap<Thread, ConcurrentHashMap<Class, Object>> mixins;

    public TransactionCompositeInvocationHandler( CompositeContextImpl aContext )
    {
        super( aContext );
    }

    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        Class mixinType = method.getDeclaringClass();
        Object mixin = getMixin( mixinType, proxy );
        if( mixin == null )
        {
            if( mixinType.equals( Object.class ) )
            {
                return invokeObject( proxy, method, args );
            }
            else
            {
                throw new InvalidCompositeException( "Implementation missing for " + mixinType.getName() + " in "
                                                     + context.getCompositeModel().getCompositeClass().getName(),
                                                     context.getCompositeModel().getCompositeClass() );
            }
        }
        // Invoke
        return context.getInvocationInstance( method ).invoke( proxy, method, args, mixin, mixinType );
    }

    protected Object getMixin( Class mixinType, Object aProxy )
    {
        Thread currentThread = Thread.currentThread();
        ConcurrentHashMap<Class, Object> transactionBranch = mixins.get( currentThread );
        Object mixin = transactionBranch.get( mixinType );
        if( mixin == null && !mixinType.equals( Object.class ) )
        {
            mixin = initializeMixin( mixinType, aProxy );
        }
        return mixin;
    }

    protected void putMixin( Class mixinType, Object value )
    {
        Thread currentThread = Thread.currentThread();
        ConcurrentHashMap<Class, Object> transactionBranch = mixins.get( currentThread );
        transactionBranch.put( mixinType, value );
    }

    public Map<Class, Object> getMixins()
    {
        Thread currentThread = Thread.currentThread();
        return mixins.get( currentThread );
    }

    public void setMixins( Map<Class, Object> mixins, boolean keep )
    {
        Thread currentThread = Thread.currentThread();
        if( keep && mixins instanceof ConcurrentHashMap )
        {
            this.mixins.put( currentThread, (ConcurrentHashMap<Class, Object>) mixins );
        }
        else
        {
            ConcurrentHashMap<Class, Object> values = new ConcurrentHashMap<Class, Object>();
            values.putAll( mixins );
            this.mixins.put( currentThread, values );
        }
    }
}
