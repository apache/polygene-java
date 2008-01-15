/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.runtime.context;

import org.qi4j.composite.Composite;
import org.qi4j.runtime.structure.ModuleContext;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.composite.AbstractCompositeInstance;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.MethodDescriptor;
import org.qi4j.runtime.composite.CompositeMethodInstance;
import org.qi4j.spi.composite.InvalidCompositeException;
import java.lang.reflect.Method;

/**
 * InvocationHandler for proxy objects.
 */
public final class ContextCompositeInstance extends AbstractCompositeInstance
{
    final private ThreadLocal<Object[]> mixins;
    private Composite proxy;

    public ContextCompositeInstance( CompositeContext aContext, ModuleInstance moduleInstance )
    {
        super( aContext, moduleInstance );
        mixins = new ThreadLocal<Object[]>();
        mixins.set( new Object[aContext.getCompositeResolution().getMixinCount()] );
    }

    public Object invoke( Object composite, Method method, Object[] args ) throws Throwable
    {
        MethodDescriptor descriptor = context.getMethodDescriptor( method );
        if( descriptor == null )
        {
            return invokeObject( composite, method, args );
        }

        Object mixin = mixins.get()[ descriptor.getMixinIndex() ];

        if( mixin == null )
        {
            throw new InvalidCompositeException( "Implementation missing for method " + method.getName() + "() ",
                                                 context.getCompositeModel().getCompositeClass() );
        }
        // Invoke
        CompositeMethodInstance compositeMethodInstance = context.getMethodInstance( descriptor, moduleInstance );
        return compositeMethodInstance.invoke( composite, args, mixin );
    }

    public void setMixins( Object[] newMixins )
    {
        // Use any mixins that match the ones we already have
        for( int i = 0; i < mixins.get().length; i++ )
        {
            Object mixin = mixins.get()[ i ];
            for( Object newMixin : newMixins )
            {
                if( mixin.getClass().equals( newMixin.getClass() ) )
                {
                    mixins.get()[ i ] = newMixin;
                    break;
                }
            }
        }
    }

    public Object[] getMixins()
    {
        return mixins.get();
    }


    @Override public String toString()
    {
        return context.getCompositeResolution().toString();
    }

    public Composite getProxy()
    {
        return proxy;
    }

    public void setProxy( Composite proxy )
    {
        this.proxy = proxy;
    }
}
