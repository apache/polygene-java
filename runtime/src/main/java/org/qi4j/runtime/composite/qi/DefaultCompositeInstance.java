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

package org.qi4j.runtime.composite.qi;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.composite.Composite;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.util.MetaInfo;

/**
 * InvocationHandler for proxy objects.
 */
public final class DefaultCompositeInstance
    implements CompositeInstance, MixinsInstance
{
    public static DefaultCompositeInstance getCompositeInstance( Composite composite )
    {
        return (DefaultCompositeInstance) Proxy.getInvocationHandler( composite );
    }

    private Composite proxy;
    private Object[] mixins;
    private CompositeModel compositeModel;
    private ModuleInstance moduleInstance;

    public DefaultCompositeInstance( CompositeModel compositeModel, ModuleInstance moduleInstance, Object[] mixins )
    {
        this.compositeModel = compositeModel;
        this.moduleInstance = moduleInstance;
        this.mixins = mixins;

        proxy = compositeModel.newProxy( this );
    }

    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        return compositeModel.invoke( this, proxy, method, args, moduleInstance );
    }

    public Composite proxy()
    {
        return proxy;
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

    public CompositeModel compositeModel()
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
                    newMixins[ i ] = oldMixin;
                    break;
                }
            }
        }
    }

    public Object[] getMixins()
    {
        return mixins;
    }

    public Object invoke( Object composite, Object[] params, CompositeMethodInstance methodInstance )
        throws Throwable
    {
        return compositeModel.invoke( composite, params, mixins, methodInstance );
    }

    public Object invokeObject( Object proxy, Object[] args, Method method )
        throws Throwable
    {
        return method.invoke( this, args );
    }

    public String toURI()
    {
        return ""; // "urn:qi4j:composite:" + context.getCompositeModel().getCompositeType().getName();
    }
}