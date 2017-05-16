/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime.composite;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.composite.CompositeInstance;
import org.apache.polygene.api.property.StateHolder;
import org.apache.polygene.api.structure.LayerDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;

/**
 * InvocationHandler for proxy objects.
 */
public class TransientInstance
    implements CompositeInstance, MixinsInstance
{
    private final Composite proxy;
    protected final Object[] mixins;
    protected StateHolder state;
    protected final CompositeModel compositeModel;

    public TransientInstance( CompositeModel compositeModel,
                              Object[] mixins,
                              StateHolder state
    )
    {
        this.compositeModel = compositeModel;
        this.mixins = mixins;
        this.state = state;
        proxy = compositeModel.newProxy( this );
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        return compositeModel.invoke( this, proxy, method, args );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> T proxy()
    {
        return (T) proxy;
    }

    @Override
    public <T> T newProxy( Class<T> mixinType )
        throws IllegalArgumentException
    {
        return compositeModel.newProxy( this, mixinType );
    }

    @Override
    public Object invokeComposite( Method method, Object[] args )
        throws Throwable
    {
        return compositeModel.invoke( this, proxy, method, args );
    }

    @Override
    public CompositeModel descriptor()
    {
        return compositeModel;
    }

    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return compositeModel.metaInfo( infoType );
    }

    @Override
    public Stream<Class<?>> types()
    {
        return compositeModel.types();
    }

    @Override
    public ModuleDescriptor module()
    {
        return compositeModel.module();
    }

    public LayerDescriptor layer()
    {
        return compositeModel.module().layer();
    }

    @Override
    public StateHolder state()
    {
        return state;
    }

    @Override
    public Object invoke( Object composite, Object[] params, CompositeMethodInstance methodInstance )
        throws Throwable
    {
        Object mixin = methodInstance.getMixinFrom( mixins );
        return methodInstance.invoke( proxy, params, mixin );
    }

    @Override
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
        TransientInstance other = (TransientInstance) PolygeneAPI.FUNCTION_COMPOSITE_INSTANCE_OF.apply( (Composite) o );
        if( other.mixins.length != mixins.length )
        {
            return false;
        }

        for( int i = 0; i < mixins.length; i++ )
        {
            if( !mixins[ i ].equals( other.mixins[ i ] ) )
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
        StringBuilder buffer = new StringBuilder();
        boolean first = true;
        for( Object mixin : mixins )
        {
            try
            {
                if( mixin != null )  // Can happen during construction of incorrect composites, during exception creation.
                {
                    Class<?> type = mixin.getClass();
                    Method toStringMethod = type.getMethod( "toString" );
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
            }
            catch( NoSuchMethodException e )
            {
                // Can not happen??
                e.printStackTrace();
            }
        }
        if( first )
        {
            String modelTypeName = compositeModel.getClass().getSimpleName();
            String metaTypeModel = modelTypeName.substring( 0, modelTypeName.length() - 5 );
            return metaTypeModel + "Instance{" +
                   "mixins=" + Arrays.asList( mixins ) +
                   ", state=" + state +
                   ", compositeModel=" + compositeModel +
                   ", module=" + module() +
                   '}';
        }
        return buffer.toString();
    }
}