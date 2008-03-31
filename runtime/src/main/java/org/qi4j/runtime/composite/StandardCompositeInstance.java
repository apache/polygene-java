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

package org.qi4j.runtime.composite;

import java.lang.reflect.Method;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.InvalidCompositeException;

/**
 * InvocationHandler for proxy objects.
 */
public final class StandardCompositeInstance extends AbstractCompositeInstance
    implements CompositeInstance
{
    private Object[] mixins;

    public StandardCompositeInstance( CompositeContext aContext, ModuleInstance moduleInstance )
    {
        super( aContext, moduleInstance );
    }

    public Object invoke( Object composite, Method method, Object[] args )
        throws Throwable
    {
        MethodDescriptor descriptor = context.getMethodDescriptor( method );
        if( descriptor == null )
        {
            return invokeObject( composite, method, args );
        }

        Object mixin = mixins[ descriptor.getMixinIndex() ];

        if( mixin == null )
        {
            throw new InvalidCompositeException( "Implementation missing for method " + method.getName() + "() ",
                                                 context.getCompositeModel().getCompositeType() );
        }
        // Invoke
        CompositeMethodInstance compositeMethodInstance = context.getMethodInstance( descriptor, moduleInstance );
        return compositeMethodInstance.invoke( composite, args, mixin );
    }

    public void setMixins( Object[] newMixins )
    {
        this.mixins = newMixins;
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
