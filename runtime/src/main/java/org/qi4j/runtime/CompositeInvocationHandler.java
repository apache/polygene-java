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
import java.util.Set;
import java.util.HashMap;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MixinResolution;

/**
 * InvocationHandler for proxy objects.
 */
public class CompositeInvocationHandler<T extends Composite> extends AbstractCompositeInvocationHandler<T>
{
    final private Object[] mixins;

    public CompositeInvocationHandler( CompositeContextImpl<T> aContext)
    {
        super( aContext );

        mixins = new Object[aContext.getCompositeResolution().getUsedMixinModels().size()];
    }

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object composite, Method method, Object[] args ) throws Throwable
    {
        MethodDescriptor descriptor = context.getMethodDescriptor( method );
        Object mixin = mixins[descriptor.getMixinIndex()];

        if( mixin == null )
        {
            Class mixinType = method.getDeclaringClass();
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
        return context.getInvocationInstance( descriptor).invoke( (T) composite, args, mixin);
    }

    public void setMixins(Object[] mixins)
    {
        Set<MixinResolution> mixinResolutions = context.getCompositeResolution().getUsedMixinModels();
        int i = 0;
        for( MixinResolution mixinResolution : mixinResolutions )
        {
            Object mixin = mixins[i];
            // Verify type
            if (!mixinResolution.getFragmentModel().getFragmentClass().isInstance( mixin))
            {
                throw new CompositeInstantiationException("Mixin "+mixin.getClass().getName()+" is not of the expected type "+mixinResolution.getFragmentModel().getFragmentClass().getName());
            }
            // Copy reference
            this.mixins[i] = mixin;
            i++;
        }
    }

    public Map<MixinResolution, Object> getMixins()
    {
        Map<MixinResolution, Object> mixinMap = new HashMap<MixinResolution, Object>( );
        Set<MixinResolution> mixinResolutions = context.getCompositeResolution().getUsedMixinModels();
        int i = 0;
        for( MixinResolution mixinResolution : mixinResolutions )
        {
            Object mixin = mixins[i++];
            mixinMap.put( mixinResolution, mixin);
        }
        return mixinMap;
    }

}
