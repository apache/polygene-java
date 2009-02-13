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
import java.lang.reflect.Proxy;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.StateHolder;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeInstance;

/**
 * InvocationHandler for proxy objects.
 */
public class DefaultCompositeInstance
    implements CompositeInstance, MixinsInstance
{
    public static DefaultCompositeInstance getCompositeInstance( Composite composite )
    {
        return (DefaultCompositeInstance) Proxy.getInvocationHandler( composite );
    }

    private final Composite proxy;
    private final Object[] mixins;
    protected StateHolder state;
    private final AbstractCompositeModel compositeModel;
    private final ModuleInstance moduleInstance;

    public DefaultCompositeInstance( AbstractCompositeModel compositeModel, ModuleInstance moduleInstance, Object[] mixins, StateHolder state )
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

    public MetaInfo metaInfo()
    {
        return compositeModel.metaInfo();
    }

    public Class<? extends Composite> type()
    {
        return compositeModel.type();
    }

    public Object[] mixins()
    {
        return mixins;
    }

    public ModuleInstance module()
    {
        return moduleInstance;
    }

    public AbstractCompositeModel compositeModel()
    {
        return compositeModel;
    }

    public void setMixins( Object[] newMixins )
    {
        // Use any mixins that match the ones we already have
        for( int i = 0; i < mixins.length; i++ )
        {
            Object oldMixin = mixins[ i ];
            for( Object newMixin : newMixins )
            {
                if( oldMixin.getClass().equals( newMixin.getClass() ) )
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

    public StateHolder state()
    {
        return state;
    }

    public Object invoke( Object composite, Object[] params, CompositeMethodInstance methodInstance )
        throws Throwable
    {
        Object mixin = methodInstance.getMixin(mixins);
        return methodInstance.invoke( composite, params, mixin );
    }

    public Object invokeObject( Object proxy, Object[] args, Method method )
        throws Throwable
    {
        return method.invoke( this, args );
    }
}