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

import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.property.StateHolder;
import org.qi4j.runtime.structure.ModuleInstance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * InvocationHandler for proxy objects.
 */
public class TransientInstance
    implements CompositeInstance, MixinsInstance
{
    public static TransientInstance getCompositeInstance( Composite composite )
    {
        return (TransientInstance) Proxy.getInvocationHandler( composite );
    }

    private final Composite proxy;
    protected final Object[] mixins;
    protected StateHolder state;
    protected final CompositeModel compositeModel;
    private final ModuleInstance moduleInstance;

    public TransientInstance( CompositeModel compositeModel,
                              ModuleInstance moduleInstance,
                              Object[] mixins,
                              StateHolder state
    )
    {
        this.compositeModel = compositeModel;
        this.moduleInstance = moduleInstance;
        this.mixins = mixins;
        this.state = state;

        proxy = compositeModel.newProxy( this );
    }

    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        return compositeModel.invoke( this, proxy, method, args, moduleInstance );
    }

    public <T> T proxy()
    {
        return (T) proxy;
    }

    public <T> T newProxy( Class<T> mixinType )
        throws IllegalArgumentException
    {
        return compositeModel.newProxy( this, mixinType );
    }

    public Object invokeComposite( Method method, Object[] args )
        throws Throwable
    {
        return compositeModel.invoke( this, proxy, method, args, moduleInstance );
    }

    public CompositeModel descriptor()
    {
        return compositeModel;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return compositeModel.metaInfo( infoType );
    }

    public Class<?> type()
    {
        return compositeModel.type();
    }

    public ModuleInstance module()
    {
        return moduleInstance;
    }

    public StateHolder state()
    {
        return state;
    }

    public Object invoke( Object composite, Object[] params, CompositeMethodInstance methodInstance )
        throws Throwable
    {
        Object mixin = methodInstance.getMixin( mixins );
        return methodInstance.invoke( proxy, params, mixin );
    }

    public Object invokeObject( Object proxy, Object[] args, Method method )
        throws Throwable
    {
        return method.invoke( this, args );
    }

    @Override
    public boolean equals( Object o )
    {
        if( o == null )
        {
            return false;
        }
        if( !Proxy.isProxyClass( o.getClass() ) )
        {
            return false;
        }
        TransientInstance other = (TransientInstance) Qi4j.INSTANCE_FUNCTION.map( (Composite) o );
        if( other.mixins.length != mixins.length )
        {
            return false;
        }

        for( int i = 0; i < mixins.length; i++ )
        {
            if( !mixins[ i ].equals( other.mixins[i] ) )
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hashCode = 0;
        for( Object mixin : mixins )
        {
            hashCode = hashCode * 31 + mixin.hashCode();
        }
        return hashCode;
    }

    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for( Object mixin : mixins )
        {
            try
            {
                Method toStringMethod = mixin.getClass().getMethod( "toString" );
                Class<?> declaringClass = toStringMethod.getDeclaringClass();
                if( !declaringClass.equals( Object.class ) )
                {
                    if( !first )
                    {
                        buffer.append( ", " );
                    }
                    first = false;
                    buffer.append( mixin.toString() );
                }
            }
            catch( NoSuchMethodException e )
            {
                // Can not happen??
                e.printStackTrace();
            }
        }
        if( first )
        {
            return "TransientInstance{" +
                   "mixins=" + ( mixins == null ? null : Arrays.asList( mixins ) ) +
                   ", state=" + state +
                   ", compositeModel=" + compositeModel +
                   ", module=" + moduleInstance +
                   '}';
        }
        return buffer.toString();
    }
}