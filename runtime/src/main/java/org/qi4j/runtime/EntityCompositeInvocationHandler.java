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

import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.persistence.EntityComposite;
import org.qi4j.spi.persistence.EntityStateHolder;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

public class EntityCompositeInvocationHandler<T extends EntityComposite> extends AbstractCompositeInvocationHandler<T>
{
    private EntityStateHolder<T> holder;

    public EntityCompositeInvocationHandler( CompositeContextImpl<T> aContext )
    {
        super( aContext );
        mixins = new ConcurrentHashMap<Class, Object>();
    }

    public static <T extends EntityComposite> EntityCompositeInvocationHandler<T> getInvocationHandler( T aProxy )
    {
        return (EntityCompositeInvocationHandler<T>) Proxy.getInvocationHandler( aProxy );
    }

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object composite, Method method, Object[] args ) throws Throwable
    {
        Class mixinType = method.getDeclaringClass();
        Object mixin = getMixin( mixinType, (T) composite );
        if( mixin == null )
        {
            if( mixinType.equals( Object.class ) )
            {
                return invokeObject( (T) composite, method, args );
            }
            else
            {
                throw new InvalidCompositeException("Implementation missing for " + mixinType.getName() + " in "
                                                    + context.getCompositeModel().getCompositeClass().getName(),
                                                    context.getCompositeModel().getCompositeClass() );
            }
        }
        // Invoke
        return context.getInvocationInstance( method ).invoke( (T) composite, method, args, mixin, mixinType );
    }


    public void setEntityStateHolder( EntityStateHolder<T> holder )
    {
        this.holder = holder;
    }

    protected Object initializeMixin( Class mixinType, T proxy )
    {
        return holder.getMixin( mixinType );
    }

    public boolean isReference()
    {
        return holder == null;
    }
}
