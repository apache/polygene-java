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
package org.qi4j.runtime.composite;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.composite.CompositeInstantiationException;
import org.qi4j.entity.EntityComposite;
import org.qi4j.runtime.MethodDescriptor;
import org.qi4j.runtime.structure.ModuleContext;
import org.qi4j.spi.composite.MixinResolution;
import org.qi4j.spi.persistence.EntityStateHolder;

public class EntityCompositeInstance extends AbstractCompositeInstance
{
    protected Object[] mixins;

    private EntityStateHolder holder;

    public EntityCompositeInstance( CompositeContext aContext, ModuleContext moduleContext )
    {
        super( aContext, moduleContext );

        mixins = new Object[aContext.getCompositeResolution().getMixinCount()];
    }

    public static <T extends EntityComposite> EntityCompositeInstance getEntityCompositeInstance( T aProxy )
    {
        return (EntityCompositeInstance) Proxy.getInvocationHandler( aProxy );
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
                return invokeObject( composite, method, args );
            }
            else
            {
                mixin = holder.getMixin( mixinType );
            }
        }
        // Invoke
        return context.getMethodInstance( descriptor, moduleContext ).invoke( composite, args, mixin );
    }

    public void setMixins( Object[] mixins )
    {
        Iterable<MixinResolution> mixinResolutions = context.getCompositeResolution().getMixinResolutions();
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

    public void setEntityStateHolder( EntityStateHolder holder )
    {
        this.holder = holder;
    }

    public boolean isReference()
    {
        return holder == null;
    }
}
