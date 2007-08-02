/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2007, Alin Dreghiciu. All Rights Reserved.
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

package org.qi4j.runtime;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.Composite;

/**
 * InvocationHandler for proxy objects.
 */
public class CompositeInvocationHandler<T extends Composite> extends AbstractCompositeInvocationHandler<T>
{
    public CompositeInvocationHandler( CompositeContextImpl<T> aContext )
    {
        super( aContext );
        mixins = new ConcurrentHashMap<Class, Object>();
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

    protected Object initializeMixin( Class mixinType, T proxy )
    {
        MixinModel mixinModel = context.getCompositeModel().getMixin( mixinType );
        return context.newFragment( mixinModel, proxy, null );
    }
}
