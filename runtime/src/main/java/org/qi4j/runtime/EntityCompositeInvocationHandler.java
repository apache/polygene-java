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
import java.lang.reflect.Proxy;
import java.util.Set;
import org.qi4j.CompositeInstantiationException;
import org.qi4j.persistence.EntityComposite;
import org.qi4j.runtime.resolution.MixinResolution;
import org.qi4j.spi.persistence.EntityStateHolder;

public class EntityCompositeInvocationHandler<T extends EntityComposite> extends AbstractCompositeInvocationHandler<T>
{
    protected Object[] mixins;

    private EntityStateHolder<T> holder;

    public EntityCompositeInvocationHandler( CompositeContextImpl<T> aContext )
    {
        super( aContext );

        mixins = new Object[aContext.getCompositeResolution().getResolvedMixinModels().size()];
    }

    public static <T extends EntityComposite> EntityCompositeInvocationHandler<T> getInvocationHandler( T aProxy )
    {
        return (EntityCompositeInvocationHandler<T>) Proxy.getInvocationHandler( aProxy );
    }

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object composite, Method method, Object[] args ) throws Throwable
    {
        MethodDescriptor descriptor = context.getMethodDescriptor( method );
        Object mixin = mixins[ descriptor.getMixinIndex() ];

        if( mixin == null )
        {
            Class mixinType = method.getDeclaringClass();
            if( mixinType.equals( Object.class ) )
            {
                return invokeObject( (T) composite, method, args );
            }
            else
            {
                mixin = holder.getMixin( mixinType );
            }
        }
        // Invoke
        return context.getInvocationInstance( descriptor ).invoke( (T) composite, args, mixin );
    }

    public void setMixins( Object[] mixins )
    {
        Set<MixinResolution> mixinResolutions = context.getCompositeResolution().getResolvedMixinModels();
        int i = 0;
        for( MixinResolution mixinResolution : mixinResolutions )
        {
            Object mixin = mixins[ i ];
            // Verify type
            if( !mixinResolution.getFragmentModel().getModelClass().isInstance( mixin ) )
            {
                throw new CompositeInstantiationException( "Mixin " + mixin.getClass().getName() + " is not of the expected type " + mixinResolution.getFragmentModel().getModelClass().getName() );
            }
            // Copy reference
            this.mixins[ i ] = mixin;
            i++;
        }
    }

    public Object[] getMixins()
    {
        return mixins;
    }

    public void setEntityStateHolder( EntityStateHolder<T> holder )
    {
        this.holder = holder;
    }

    public boolean isReference()
    {
        return holder == null;
    }
}
