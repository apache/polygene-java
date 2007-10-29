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
import org.qi4j.Composite;
import org.qi4j.model.InvalidCompositeException;

/**
 * InvocationHandler for proxy objects.
 */
public class CompositeInvocationHandler<T extends Composite> extends AbstractCompositeInvocationHandler<T>
{
    final private Object[] mixins;

    public CompositeInvocationHandler( CompositeContextImpl<T> aContext )
    {
        super( aContext );

        mixins = new Object[aContext.getCompositeResolution().getResolvedMixinModels().size()];
    }

    public Object invoke( Object composite, Method method, Object[] args ) throws Throwable
    {
        MethodDescriptor descriptor = context.getMethodDescriptor( method );
        if( descriptor == null )
        {
            return invokeObject( composite, method, args );
        }

        Object mixin = mixins[ descriptor.getMixinIndex() ];

        if( mixin == null )
        {
            throw new InvalidCompositeException( "Implementation missing for method " + method.getName() + " in "
                                                 + context.getCompositeModel().getCompositeClass().getName(),
                                                 context.getCompositeModel().getCompositeClass() );
        }
        // Invoke
        return context.getInvocationInstance( descriptor ).invoke( composite, args, mixin );
    }

    public void setMixins( Object[] newMixins )
    {
        // Use any mixins that match the ones we already have
        for( int i = 0; i < mixins.length; i++ )
        {
            Object mixin = mixins[ i ];
            for( Object newMixin : newMixins )
            {
                if( mixin.getClass().equals( newMixin.getClass() ) )
                {
                    mixins[ i ] = newMixin;
                    break;
                }
            }
        }
    }

    public Object[] getMixins()
    {
        return mixins;
    }


    @Override public String toString()
    {
        return context.getCompositeResolution().toString();
    }
}
